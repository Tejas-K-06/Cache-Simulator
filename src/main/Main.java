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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    public static void main(String[] args) {

        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║            CACHE SIMULATOR v1.0                    ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
        System.out.println();

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

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  STEP 2: Loading Memory Trace");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        String traceFile = "traces/sample_trace.txt";
        List<MemoryAccess> trace;

        try {
            trace = TraceLoader.load(traceFile);

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

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  STEP 4: Simulation Results");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        hierarchy.printReport();

        for (int i = 0; i < hierarchy.getLevelCount(); i++) {
            SimulationStats stats = hierarchy.getLevel(i).getStats();
            System.out.printf("%n  ┌─── L%d Detailed Stats ───┐%n", i + 1);
            System.out.println(stats.getSummary());
            System.out.printf("  └──────────────────────────┘%n");
        }

        System.out.println();

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("  STEP 5: Multithreaded Trace Generation");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        try {
            AtomicInteger errorCount = new AtomicInteger(0);

            List<TraceGenerator.TraceTask> tasks = new ArrayList<>();
            tasks.add(TraceGenerator.TraceTask.random(
                    "traces/generated_random.txt", 50, 16, 0.3, errorCount));
            tasks.add(TraceGenerator.TraceTask.sequential(
                    "traces/generated_sequential.txt", 0x1000, 30, 64, false, errorCount));
            tasks.add(TraceGenerator.TraceTask.looping(
                    "traces/generated_looping.txt", 0x5000, 4, 10, false, errorCount));

            TraceGenerator.generateAllParallel(tasks, 3);

            System.out.println("  ✓ All trace files generated concurrently.");
            System.out.println("    Load with: TraceLoader.load(\"traces/generated_random.txt\")");

        } catch (IOException e) {
            System.err.println("  ✗ Error generating traces: " + e.getMessage());
        }

        System.out.println();
        System.out.println("╔════════════════════════════════════════════════════╗");
        System.out.println("║          Simulation completed successfully!        ║");
        System.out.println("╚════════════════════════════════════════════════════╝");
    }
}
