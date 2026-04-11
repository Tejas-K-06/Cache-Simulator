package main;

import exception.CacheConfigException;
import exception.InvalidAddressException;
import stats.SimulationStats;
import trace.MemoryAccess;
import trace.TraceGenerator;
import trace.TraceLoader;

import java.io.IOException;
import java.util.List;

/**
 * Main entry point for the Cache Simulator — Person 4 (Varad) demo.
 *
 * Demonstrates the Simulation Engine & Stats components:
 *   1. Loading traces from files (TraceLoader)
 *   2. Generating synthetic traces (TraceGenerator)
 *   3. Recording and reporting simulation statistics (SimulationStats)
 *   4. Exception handling (InvalidAddressException, CacheConfigException)
 *
 * NOTE: Full end-to-end simulation with actual caches requires Person 1's
 * cache implementations and Person 3's policies. This demo validates the
 * infrastructure that feeds into and reports from those components.
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║        CACHE SIMULATOR — Simulation Engine        ║");
        System.out.println("║              Person 4 (Varad) Demo                ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
        System.out.println();

        // ------------------------------------------------------------------
        // DEMO 1: Load sample trace file
        // ------------------------------------------------------------------
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  DEMO 1: Loading Sample Trace File");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        try {
            List<MemoryAccess> sampleTrace = TraceLoader.load("traces/sample_trace.txt");

            System.out.println("\nParsed trace contents:");
            System.out.println("  ┌──────┬───────┬──────────────┐");
            System.out.println("  │  #   │ Type  │   Address    │");
            System.out.println("  ├──────┼───────┼──────────────┤");

            int index = 1;
            for (MemoryAccess access : sampleTrace) {
                System.out.printf("  │ %4d │ %s │ 0x%08X   │%n",
                        index++,
                        access.isWrite() ? "WRITE" : "READ ",
                        access.getAddress());
            }
            System.out.println("  └──────┴───────┴──────────────┘");
            System.out.printf("  Total accesses loaded: %d%n%n", sampleTrace.size());

        } catch (InvalidAddressException e) {
            System.err.println("ERROR: Bad address in sample trace → " + e.getMessage());
        } catch (IOException e) {
            System.err.println("ERROR: Cannot read sample trace file → " + e.getMessage());
        }

        // ------------------------------------------------------------------
        // DEMO 2: Load stress trace file
        // ------------------------------------------------------------------
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  DEMO 2: Loading Stress Trace File");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        try {
            List<MemoryAccess> stressTrace = TraceLoader.load("traces/stress_trace.txt");

            // Show first 10 and last 5 to keep output manageable
            System.out.println("\nFirst 10 accesses:");
            for (int i = 0; i < Math.min(10, stressTrace.size()); i++) {
                System.out.printf("  [%3d] %s%n", i + 1, stressTrace.get(i));
            }
            System.out.println("  ...");

            int total = stressTrace.size();
            System.out.println("Last 5 accesses:");
            for (int i = Math.max(0, total - 5); i < total; i++) {
                System.out.printf("  [%3d] %s%n", i + 1, stressTrace.get(i));
            }

            // Count reads vs writes
            long reads = stressTrace.stream().filter(a -> !a.isWrite()).count();
            long writes = stressTrace.stream().filter(MemoryAccess::isWrite).count();
            System.out.printf("%n  Total: %d accesses (%d reads, %d writes)%n%n",
                    total, reads, writes);

        } catch (InvalidAddressException e) {
            System.err.println("ERROR: Bad address in stress trace → " + e.getMessage());
        } catch (IOException e) {
            System.err.println("ERROR: Cannot read stress trace file → " + e.getMessage());
        }

        // ------------------------------------------------------------------
        // DEMO 3: Generate a new trace file using TraceGenerator
        // ------------------------------------------------------------------
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  DEMO 3: Generating Synthetic Traces");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        try {
            // 3a: Random trace — 50 accesses, 16-bit address space, 30% writes
            TraceGenerator.generate("traces/generated_random.txt", 50, 16, 0.3);

            // 3b: Sequential trace — 30 accesses starting at 0x1000, stride of 64 bytes
            TraceGenerator.generateSequential("traces/generated_sequential.txt",
                    0x1000, 30, 64, false);

            // 3c: Looping trace — 4 blocks, 10 iterations (tests LRU vs FIFO)
            TraceGenerator.generateLooping("traces/generated_looping.txt",
                    0x5000, 4, 10, false);

            // Verify: load the generated random trace back
            System.out.println("\nVerifying generated random trace can be re-loaded...");
            List<MemoryAccess> reloaded = TraceLoader.load("traces/generated_random.txt");
            System.out.printf("  ✓ Successfully re-loaded %d accesses from generated file%n%n",
                    reloaded.size());

        } catch (InvalidAddressException e) {
            System.err.println("ERROR: Generated trace has bad format → " + e.getMessage());
        } catch (IOException e) {
            System.err.println("ERROR: File I/O error → " + e.getMessage());
        }

        // ------------------------------------------------------------------
        // DEMO 4: SimulationStats usage
        // ------------------------------------------------------------------
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  DEMO 4: Simulation Statistics");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        SimulationStats stats = new SimulationStats();

        // Simulate some cache behavior
        stats.recordHit(false);   // read hit
        stats.recordHit(false);   // read hit
        stats.recordHit(false);   // read hit
        stats.recordMiss(false);  // read miss
        stats.recordHit(true);    // write hit
        stats.recordMiss(true);   // write miss
        stats.recordMiss(true);   // write miss
        stats.recordHit(false);   // read hit
        stats.recordHit(true);    // write hit
        stats.recordMiss(false);  // read miss
        stats.recordEviction();
        stats.recordEviction();
        stats.recordEviction();
        stats.recordWriteBack();

        System.out.println("\nSimulated 10 accesses (6 hits, 4 misses):");
        System.out.println(stats.getSummary());
        System.out.println();

        // Test reset
        System.out.println("After reset:");
        stats.reset();
        System.out.println(stats.getSummary());
        System.out.println();

        // ------------------------------------------------------------------
        // DEMO 5: Exception handling
        // ------------------------------------------------------------------
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  DEMO 5: Exception Handling");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // 5a: InvalidAddressException
        System.out.println("\n--- InvalidAddressException ---");
        try {
            throw new InvalidAddressException(
                "Address is not valid hexadecimal",
                "0xZZZZ",
                42
            );
        } catch (InvalidAddressException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  Bad address: " + e.getBadAddress());
            System.out.println("  At line: " + e.getLineNumber());
        }

        // 5b: CacheConfigException
        System.out.println("\n--- CacheConfigException ---");
        try {
            int cacheSize = 100; // Not a power of 2!
            if ((cacheSize & (cacheSize - 1)) != 0) {
                throw new CacheConfigException(
                    "Cache size must be a power of 2",
                    "cacheSize",
                    cacheSize
                );
            }
        } catch (CacheConfigException e) {
            System.out.println("  Caught: " + e.getMessage());
            System.out.println("  Param name: " + e.getParamName());
            System.out.println("  Param value: " + e.getParamValue());
        }

        // ------------------------------------------------------------------
        // Done
        // ------------------------------------------------------------------
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║            All demos completed successfully!      ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
    }
}
