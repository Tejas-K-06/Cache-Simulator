package trace;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

/**
 * Utility class to programmatically generate memory trace files for testing.
 *
 * Provides three generation patterns:
 *   1. Random    — uniformly distributed addresses (baseline testing)
 *   2. Sequential— linear stride pattern (tests spatial locality)
 *   3. Looping   — repeated accesses over a small range (tests temporal locality,
 *                  useful for comparing LRU vs FIFO eviction behavior)
 *
 * All generated files use the same format as TraceLoader expects:
 *   R 0x0000A4F0
 *   W 0x00003C10
 */
public class TraceGenerator {

    private static final Random random = new Random();

    // -------------------------------------------------------------------------
    // Random trace generation
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

        long addressRange = 1L << addressRangeBits;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("# Auto-generated random trace");
            writer.newLine();
            writer.write("# Count: " + count + " | Address bits: " + addressRangeBits
                         + " | Write ratio: " + writeRatio);
            writer.newLine();
            writer.newLine();

            for (int i = 0; i < count; i++) {
                long address = Math.abs(random.nextLong()) % addressRange;
                // Align to 4-byte boundary (word-aligned, realistic for 32-bit CPU)
                address = (address >> 2) << 2;
                boolean isWrite = random.nextDouble() < writeRatio;

                String op = isWrite ? "W" : "R";
                writer.write(String.format("%s 0x%08X", op, address));
                writer.newLine();
            }
        }

        System.out.printf("[TraceGenerator] Generated %d random accesses → '%s'%n",
                count, filePath);
    }

    // -------------------------------------------------------------------------
    // Sequential trace generation
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
    // Looping trace generation
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
