package trace;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class to programmatically generate memory trace files for testing.
 *
 * Provides three generation patterns:
 *   1. Random    — uniformly distributed addresses (baseline testing)
 *   2. Sequential— linear stride pattern (tests spatial locality)
 *   3. Looping   — repeated accesses over a small range (tests temporal locality,
 *                  useful for comparing LRU vs FIFO eviction behavior)
 *
 * Supports two execution modes:
 *   - Synchronous  — individual static methods (generate, generateSequential, generateLooping)
 *   - Multithreaded — batch generation via generateAllParallel() using a fixed thread pool
 *     (ExecutorService). Each trace file is generated on a separate thread concurrently.
 *
 * Thread Safety:
 *   - Each thread creates its own Random instance (avoids contention on shared state)
 *   - Each thread writes to its own file (no shared file handles)
 *   - AtomicInteger is used for thread-safe error counting
 *   - ExecutorService.awaitTermination() ensures all threads complete before returning
 *
 * All generated files use the same format as TraceLoader expects:
 *   R 0x0000A4F0
 *   W 0x00003C10
 */
public class TraceGenerator {

    /**
     * Represents a single trace generation task that can be submitted
     * to a thread pool for concurrent execution.
     *
     * Implements Runnable so each task can run on its own thread from
     * the ExecutorService pool.
     */
    public static class TraceTask implements Runnable {

        private final String filePath;
        private final String type;        // "random", "sequential", or "looping"

        // Random params
        private int count;
        private int addressRangeBits;
        private double writeRatio;

        // Sequential params
        private int startAddress;
        private int stride;
        private boolean isWrite;

        // Looping params
        private int baseAddress;
        private int loopSize;
        private int iterations;

        // Thread-safe error tracking (shared across all tasks in a batch)
        private final AtomicInteger errorCount;
        private volatile IOException error;

        // Private constructor — use factory methods below
        private TraceTask(String filePath, String type, AtomicInteger errorCount) {
            this.filePath = filePath;
            this.type = type;
            this.errorCount = errorCount;
        }

        /**
         * Factory method: create a random trace generation task.
         */
        public static TraceTask random(String filePath, int count,
                                        int addressRangeBits, double writeRatio,
                                        AtomicInteger errorCount) {
            TraceTask task = new TraceTask(filePath, "random", errorCount);
            task.count = count;
            task.addressRangeBits = addressRangeBits;
            task.writeRatio = writeRatio;
            return task;
        }

        /**
         * Factory method: create a sequential trace generation task.
         */
        public static TraceTask sequential(String filePath, int startAddress,
                                            int count, int stride, boolean isWrite,
                                            AtomicInteger errorCount) {
            TraceTask task = new TraceTask(filePath, "sequential", errorCount);
            task.startAddress = startAddress;
            task.count = count;
            task.stride = stride;
            task.isWrite = isWrite;
            return task;
        }

        /**
         * Factory method: create a looping trace generation task.
         */
        public static TraceTask looping(String filePath, int baseAddress,
                                         int loopSize, int iterations, boolean isWrite,
                                         AtomicInteger errorCount) {
            TraceTask task = new TraceTask(filePath, "looping", errorCount);
            task.baseAddress = baseAddress;
            task.loopSize = loopSize;
            task.iterations = iterations;
            task.isWrite = isWrite;
            return task;
        }

        /**
         * Executed by the thread pool. Delegates to the appropriate
         * synchronous generation method.
         *
         * Thread.currentThread().getName() identifies which pool thread
         * is executing this task — demonstrates concurrent execution.
         */
        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.printf("  [%s] Starting %s trace → '%s'%n", threadName, type, filePath);
            long startTime = System.nanoTime();

            try {
                switch (type) {
                    case "random":
                        generate(filePath, count, addressRangeBits, writeRatio);
                        break;
                    case "sequential":
                        generateSequential(filePath, startAddress, count, stride, isWrite);
                        break;
                    case "looping":
                        generateLooping(filePath, baseAddress, loopSize, iterations, isWrite);
                        break;
                }

                long elapsed = (System.nanoTime() - startTime) / 1_000_000;
                System.out.printf("  [%s] ✓ Completed %s trace in %d ms%n",
                        threadName, type, elapsed);
            } catch (IOException e) {
                this.error = e;
                errorCount.incrementAndGet();
                System.err.printf("  [%s] ✗ Failed %s trace: %s%n",
                        threadName, type, e.getMessage());
            }
        }

        public IOException getError() { return error; }
        public String getFilePath()   { return filePath; }
    }

    // -------------------------------------------------------------------------
    // Multithreaded batch generation
    // -------------------------------------------------------------------------

    /**
     * Generates multiple trace files concurrently using a fixed thread pool.
     *
     * Flow:
     *   1. Creates an ExecutorService with the specified pool size
     *   2. Submits all TraceTask objects to the pool (they start immediately
     *      on available threads)
     *   3. Calls executor.shutdown() to reject new tasks
     *   4. Calls executor.awaitTermination() to block until all tasks finish
     *   5. Checks for errors — throws IOException if any task failed
     *
     * @param tasks          List of TraceTask objects to execute concurrently
     * @param threadPoolSize Number of threads in the pool (typically = tasks.size())
     * @throws IOException   If any task encountered an I/O error
     */
    public static void generateAllParallel(List<TraceTask> tasks, int threadPoolSize)
            throws IOException {

        if (tasks.isEmpty()) return;

        System.out.printf("[TraceGenerator] Launching %d tasks across %d threads...%n",
                tasks.size(), threadPoolSize);
        long batchStart = System.nanoTime();

        // Create a fixed-size thread pool
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        // Submit all tasks — each runs on a pool thread concurrently
        for (TraceTask task : tasks) {
            executor.submit(task);
        }

        // Initiate orderly shutdown: no new tasks accepted
        executor.shutdown();

        // Block until all tasks complete (or timeout after 60 seconds)
        try {
            boolean finished = executor.awaitTermination(60, TimeUnit.SECONDS);
            if (!finished) {
                executor.shutdownNow();
                throw new IOException("Trace generation timed out after 60 seconds");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            throw new IOException("Trace generation interrupted", e);
        }

        long batchElapsed = (System.nanoTime() - batchStart) / 1_000_000;

        // Count errors from AtomicInteger (shared across tasks)
        int errors = 0;
        for (TraceTask task : tasks) {
            if (task.getError() != null) errors++;
        }

        System.out.printf("[TraceGenerator] All %d tasks completed in %d ms (%d errors)%n",
                tasks.size(), batchElapsed, errors);

        // If any task failed, throw the first error
        if (errors > 0) {
            for (TraceTask task : tasks) {
                if (task.getError() != null) {
                    throw new IOException("Failed to generate '" + task.getFilePath()
                            + "': " + task.getError().getMessage(), task.getError());
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Random trace generation (synchronous — also called by threads internally)
    // -------------------------------------------------------------------------

    /**
     * Generates a trace file with random memory accesses.
     *
     * @param filePath         Path to the output trace file
     * @param count            Number of memory accesses to generate
     * @param addressRangeBits Addresses will be in [0, 2^addressRangeBits - 1]
     *                         (e.g. 16 → addresses from 0x0000 to 0xFFFF)
     * @param writeRatio       Fraction of accesses that are writes (0.0 to 1.0)
     * @throws IOException If the file cannot be written
     */
    public static void generate(String filePath, int count,
                                int addressRangeBits, double writeRatio)
            throws IOException {

        // Thread-local Random — avoids contention when called from multiple threads
        Random rng = new Random();
        long addressRange = 1L << addressRangeBits;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("# Auto-generated random trace");
            writer.newLine();
            writer.write("# Count: " + count + " | Address bits: " + addressRangeBits
                         + " | Write ratio: " + writeRatio);
            writer.newLine();
            writer.write("# Generated by: " + Thread.currentThread().getName());
            writer.newLine();
            writer.newLine();

            for (int i = 0; i < count; i++) {
                long address = Math.abs(rng.nextLong()) % addressRange;
                // Align to 4-byte boundary (word-aligned, realistic for 32-bit CPU)
                address = (address >> 2) << 2;
                boolean isWrite = rng.nextDouble() < writeRatio;

                String op = isWrite ? "W" : "R";
                writer.write(String.format("%s 0x%08X", op, address));
                writer.newLine();
            }
        }

        System.out.printf("[TraceGenerator] Generated %d random accesses → '%s'%n",
                count, filePath);
    }

    // -------------------------------------------------------------------------
    // Sequential trace generation (synchronous)
    // -------------------------------------------------------------------------

    /**
     * Generates a trace file with sequential (strided) memory accesses.
     * Useful for testing spatial locality and sequential prefetching.
     *
     * @param filePath     Path to the output trace file
     * @param startAddress Starting memory address
     * @param count        Number of accesses to generate
     * @param stride       Address increment between consecutive accesses (in bytes)
     * @param isWrite      true to generate writes, false for reads
     * @throws IOException If the file cannot be written
     */
    public static void generateSequential(String filePath, int startAddress,
                                          int count, int stride, boolean isWrite)
            throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("# Auto-generated sequential trace");
            writer.newLine();
            writer.write("# Start: 0x" + Integer.toHexString(startAddress).toUpperCase()
                         + " | Count: " + count + " | Stride: " + stride);
            writer.newLine();
            writer.write("# Generated by: " + Thread.currentThread().getName());
            writer.newLine();
            writer.newLine();

            String op = isWrite ? "W" : "R";
            long address = Integer.toUnsignedLong(startAddress);

            for (int i = 0; i < count; i++) {
                writer.write(String.format("%s 0x%08X", op, address & 0xFFFFFFFFL));
                writer.newLine();
                address += stride;
            }
        }

        System.out.printf("[TraceGenerator] Generated %d sequential accesses → '%s'%n",
                count, filePath);
    }

    // -------------------------------------------------------------------------
    // Looping trace generation (synchronous)
    // -------------------------------------------------------------------------

    /**
     * Generates a trace file with a looping access pattern — repeatedly
     * accesses a small set of addresses over multiple iterations.
     *
     * This pattern is ideal for testing:
     *   - Temporal locality (LRU should keep recently used blocks)
     *   - FIFO vs LRU behavior (FIFO may evict still-needed blocks)
     *   - Cache thrashing when loopSize > cache capacity
     *
     * @param filePath    Path to the output trace file
     * @param baseAddress Starting address of the loop
     * @param loopSize    Number of distinct addresses in the loop
     * @param iterations  How many times to repeat the loop
     * @param isWrite     true to generate writes, false for reads
     * @throws IOException If the file cannot be written
     */
    public static void generateLooping(String filePath, int baseAddress,
                                       int loopSize, int iterations, boolean isWrite)
            throws IOException {

        int blockStride = 64; // Typical cache block size — ensures each iteration hits a different block

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("# Auto-generated looping trace");
            writer.newLine();
            writer.write("# Base: 0x" + Integer.toHexString(baseAddress).toUpperCase()
                         + " | Loop size: " + loopSize + " | Iterations: " + iterations);
            writer.newLine();
            writer.write("# Generated by: " + Thread.currentThread().getName());
            writer.newLine();
            writer.newLine();

            String op = isWrite ? "W" : "R";

            for (int iter = 0; iter < iterations; iter++) {
                for (int i = 0; i < loopSize; i++) {
                    long address = Integer.toUnsignedLong(baseAddress) + ((long) i * blockStride);
                    writer.write(String.format("%s 0x%08X", op, address & 0xFFFFFFFFL));
                    writer.newLine();
                }
            }
        }

        int totalAccesses = loopSize * iterations;
        System.out.printf("[TraceGenerator] Generated %d looping accesses → '%s'%n",
                totalAccesses, filePath);
    }
}
