package multilevel;

import cache.Cache;
import memory.MainMemory;
import stats.SimulationStats;
import trace.MemoryAccess;

import java.util.ArrayList;
import java.util.List;

/**
 * Chains multiple Cache levels into an L1 → L2 → L3 → MainMemory hierarchy.
 *
 * Uses the Chain of Responsibility pattern:
 * - On a hit at level N → record hit, return immediately.
 * - On a miss at level N → check level N+1.
 * - After a miss is resolved from a deeper level, the block is loaded
 * back into all shallower levels (inclusive cache policy).
 * - If all cache levels miss → fetch from MainMemory (Singleton).
 */
public class CacheHierarchy {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final List<Cache> levels; // Ordered list: index 0 = L1, 1 = L2, 2 = L3
    private final MainMemory mainMemory;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * @param mainMemory The singleton main memory instance shared across the
     *                   hierarchy.
     */
    public CacheHierarchy(MainMemory mainMemory) {
        this.levels = new ArrayList<>();
        this.mainMemory = mainMemory;
    }

    // -------------------------------------------------------------------------
    // Building the hierarchy
    // -------------------------------------------------------------------------

    /**
     * Add a cache level to the hierarchy in order (call L1 first, then L2, then
     * L3).
     *
     * @param cache A configured Cache instance (Direct Mapped, Set Associative,
     *              etc.)
     */
    public void addLevel(Cache cache) {
        levels.add(cache);
    }

    // -------------------------------------------------------------------------
    // Core access — Chain of Responsibility
    // -------------------------------------------------------------------------

    /**
     * Process a single memory access request through the full cache hierarchy.
     *
     * Flow:
     * 1. Try L1. If hit → done.
     * 2. On L1 miss → try L2.
     * 3. On L2 miss → try L3.
     * 4. On L3 miss → fetch from MainMemory.
     * 5. On the way back up, load the block into every level that missed
     * (inclusive policy: all levels hold a copy after a miss is resolved).
     *
     * @param request A MemoryAccess object containing the address and read/write
     *                flag.
     */
    public void access(MemoryAccess request) {
        if (levels.isEmpty()) {
            throw new IllegalStateException("Cache hierarchy has no levels configured.");
        }

        // Start the recursive chain from level 0 (L1)
        accessLevel(0, request);
    }

    /**
     * Recursive helper that walks down the hierarchy on a miss.
     * On the way back up, loads the block into the current level.
     *
     * @param levelIndex Index into `levels` list (0 = L1, 1 = L2, ...)
     * @param request    The memory access being processed
     * @return true if the data was found at this level or below (used internally)
     */
    private boolean accessLevel(int levelIndex, MemoryAccess request) {

        // ----- Base case: we've exhausted all cache levels → go to RAM -----
        if (levelIndex >= levels.size()) {
            mainMemory.access(request.getAddress(), request.isWrite());
            return true; // RAM always has the data
        }

        Cache currentCache = levels.get(levelIndex);
        String levelName = "L" + (levelIndex + 1);

        // Try accessing the current cache level
        boolean hit = tryAccess(currentCache, request);

        if (hit) {
            // Cache hit at this level — stats already recorded inside cache.access()
            System.out.printf("[%s] HIT  @ address 0x%08X%n", levelName, request.getAddress());
            return true;
        }

        // Cache miss — go deeper
        System.out.printf("[%s] MISS @ address 0x%08X → checking %s%n",
                levelName, request.getAddress(),
                levelIndex + 1 < levels.size() ? "L" + (levelIndex + 2) : "Main Memory");

        MemoryAccess fetchRequest = new MemoryAccess(request.getAddress(), false);
        accessLevel(levelIndex + 1, fetchRequest);

        return true;
    }

    // -------------------------------------------------------------------------
    // Internal helper
    // -------------------------------------------------------------------------

    /**
     * Attempts an access on the given cache level.
     *
     * This delegates directly to cache.access(), which internally records
     * hits/misses to its SimulationStats object. We rely on the cache returning
     * its SimulationStats hit count to detect whether a hit occurred.
     *
     * Note: the cache subclasses are responsible for recording their own stats.
     * CacheHierarchy only controls the flow of the chain.
     *
     * @param cache   The cache level to access
     * @param request The memory access request
     * @return true if it was a hit, false if a miss
     */
    private boolean tryAccess(Cache cache, MemoryAccess request) {
        long hitsBefore = cache.getStats().getHits();
        cache.access(request.getAddress(), request.isWrite());
        return cache.getStats().getHits() > hitsBefore;
    }

    // -------------------------------------------------------------------------
    // Reporting
    // -------------------------------------------------------------------------

    /**
     * Print a summary report for all cache levels.
     */
    public void printReport() {
        System.out.println("============================");
        System.out.println("  Cache Simulation Report");
        System.out.println("============================");

        for (int i = 0; i < levels.size(); i++) {
            Cache cache = levels.get(i);
            SimulationStats stats = cache.getStats();
            System.out.printf("%n--- L%d Cache ---%n", i + 1);
            // System.out.println(stats.getSummary());
        }

        System.out.printf("%n--- Overall ---%n");
        System.out.printf("Total RAM Accesses : %d%n", mainMemory.getAccessCount());
        System.out.println("============================");
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public List<Cache> getLevels() {
        return levels;
    }

    public int getLevelCount() {
        return levels.size();
    }

    public Cache getLevel(int index) {
        return levels.get(index);
    }

    public MainMemory getMainMemory() {
        return mainMemory;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CacheHierarchy[\n");
        for (int i = 0; i < levels.size(); i++) {
            sb.append(String.format("  L%d: %s%n", i + 1, levels.get(i)));
        }
        sb.append("  RAM: ").append(mainMemory).append("\n]");
        return sb.toString();
    }
}