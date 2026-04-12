package main;

import config.ConfigLoader;
import config.CacheFactory;
import exception.CacheConfigException;
import exception.InvalidAddressException;
import multilevel.CacheHierarchy;
import stats.SimulationStats;
import trace.MemoryAccess;
import trace.TraceGenerator;
import trace.TraceLoader;

import java.io.IOException;
import java.util.List;

/**
 * Main entry point for the Cache Simulator.
 *
 * Combines all team members' work into a complete end-to-end simulation:
 *   - Config loading and hierarchy construction (Person 1 — Tejas / Person 2 — Jibran)
 *   - Replacement & write policies (Person 3 — Kanak)
 *   - Trace loading, trace generation, simulation stats, exception handling (Person 4 — Varad)
 *
 * Flow:
 *   1. Load cache configuration from JSON
 *   2. Build the cache hierarchy (L1 → L2 → Main Memory)
 *   3. Load a memory trace file
 *   4. Feed every memory access through the hierarchy (actual simulation)
 *   5. Print the per-level statistics report
 */
public class Main {

    public static void main(String[] args) {

        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║            CACHE SIMULATOR v1.0                    ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
        System.out.println();

        // ==================================================================
        // STEP 1: Load Configuration
        // ==================================================================
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  STEP 1: Loading Cache Configuration");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        String configFile = "config/cache_config.json";
        ConfigLoader loader = new ConfigLoader(configFile);
        CacheHierarchy hierarchy;

        try {
            loader.load();
            System.out.println("  ✓ Configuration loaded from: " + configFile);
            System.out.println("  Main Memory Size: " + loader.getMainMemorySize() + " bytes");

            // Build the cache hierarchy from config
            hierarchy = CacheFactory.createHierarchy(loader);
            System.out.println("  ✓ Cache hierarchy created successfully.");
            System.out.println();
            System.out.println(hierarchy.toString());

        } catch (IOException e) {
            System.err.println("  ✗ Failed to load configuration file: " + e.getMessage());
            return;
        } catch (CacheConfigException e) {
            System.err.println("  ✗ Invalid cache configuration: " + e.getMessage());
            return;
        } catch (Exception e) {
            System.err.println("  ✗ Error initializing cache hierarchy: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // ==================================================================
        // STEP 2: Load Memory Trace
        // ==================================================================
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  STEP 2: Loading Memory Trace");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        String traceFile = "traces/sample_trace.txt";
        List<MemoryAccess> trace;

        try {
            trace = TraceLoader.load(traceFile);

            // Count reads vs writes
            long reads = trace.stream().filter(a -> !a.isWrite()).count();
            long writes = trace.stream().filter(MemoryAccess::isWrite).count();
            System.out.printf("  Total: %d accesses (%d reads, %d writes)%n%n",
                    trace.size(), reads, writes);

        } catch (InvalidAddressException e) {
            System.err.println("  ✗ Bad address in trace file: " + e.getMessage());
            return;
        } catch (IOException e) {
            System.err.println("  ✗ Cannot read trace file: " + e.getMessage());
            return;
        }

        // ==================================================================
        // STEP 3: Run Simulation
        // ==================================================================
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  STEP 3: Running Simulation");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();

        int accessNum = 0;
        for (MemoryAccess access : trace) {
            accessNum++;
            System.out.printf("  [%3d] %s%n", accessNum, access);
            hierarchy.access(access);
        }

        System.out.println();

        // ==================================================================
        // STEP 4: Print Statistics Report
        // ==================================================================
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  STEP 4: Simulation Results");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        hierarchy.printReport();

        // Also print detailed per-level stats using SimulationStats.getSummary()
        for (int i = 0; i < hierarchy.getLevelCount(); i++) {
            SimulationStats stats = hierarchy.getLevel(i).getStats();
            System.out.printf("%n  ┌─── L%d Detailed Stats ───┐%n", i + 1);
            System.out.println(stats.getSummary());
            System.out.printf("  └──────────────────────────┘%n");
        }

        System.out.println();

        // ==================================================================
        // STEP 5: Trace Generation Utility (bonus)
        // ==================================================================
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  STEP 5: Trace Generation Utility");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        try {
            // Generate different trace patterns for testing
            TraceGenerator.generate("traces/generated_random.txt", 50, 16, 0.3);
            TraceGenerator.generateSequential("traces/generated_sequential.txt",
                    0x1000, 30, 64, false);
            TraceGenerator.generateLooping("traces/generated_looping.txt",
                    0x5000, 4, 10, false);

            System.out.println("  ✓ Generated trace files can be loaded with:");
            System.out.println("    TraceLoader.load(\"traces/generated_random.txt\")");

        } catch (IOException e) {
            System.err.println("  ✗ Error generating traces: " + e.getMessage());
        }

        // ==================================================================
        // Done
        // ==================================================================
        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║          Simulation completed successfully!        ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
    }
}
