package cache;

import policy.ReplacementPolicy;
import write.WritePolicy;
import stats.SimulationStats;

/**
 * Set Associative Cache — blocks are grouped into sets of N ways.
 *
 * An address maps to exactly one set (via index bits), then any way
 * within that set can hold it. Eviction within a set is delegated
 * to the ReplacementPolicy.
 *
 * Index bits = log2(numberOfSets), where numberOfSets = numberOfBlocks / associativity
 */
public class SetAssociativeCache extends Cache {

    private CacheBlock[][] sets;
    private int associativity;
    private int numberOfSets;

    private long accessCounter = 0;   // increment on every access — used by LRU
    private long insertCounter = 0;   // increment on every block load — used by FIFO

    /**
     * @param cacheSize         Total size of cache in bytes
     * @param blockSize         Size of each block in bytes
     * @param associativity     Number of ways per set (e.g. 2, 4, 8)
     * @param replacementPolicy LRU or FIFO policy instance for within-set eviction
     * @param writePolicy       WriteBack or WriteThrough policy instance
     * @param stats             Shared stats object to record hits/misses
     */
    public SetAssociativeCache(int cacheSize, int blockSize,
                                int associativity,
                                ReplacementPolicy replacementPolicy,
                                WritePolicy writePolicy,
                                SimulationStats stats) {
        super(cacheSize, blockSize, replacementPolicy, writePolicy, stats);

        this.associativity = associativity;
        this.numberOfSets  = numberOfBlocks / associativity;

        // Recompute index/tag bits now that associativity is known
        this.indexBits = computeIndexBits();
        this.tagBits   = 32 - indexBits - offsetBits;

        sets = new CacheBlock[numberOfSets][associativity];
        for (int i = 0; i < numberOfSets; i++) {
            for (int j = 0; j < associativity; j++) {
                sets[i][j] = new CacheBlock();
            }
        }
    }

    /**
     * Set Associative uses log2(numberOfSets) index bits.
     */
    @Override
    protected int computeIndexBits() {
        return log2(numberOfSets);
    }

    /**
     * Process a single memory access.
     *
     * 1. Decompose address into index (which set) and tag.
     * 2. Search all ways in that set for a matching tag.
     * 3. HIT  → record hit; update lastUsed for LRU; on write delegate to writePolicy.
     * 4. MISS → record miss; ask replacementPolicy for victim within set; load new tag; on write delegate.
     */
    @Override
    public void access(int address, boolean isWrite) {
        accessCounter++;

        int index = getIndex(address);
        int tag   = getTag(address);

        CacheBlock[] currentSet = sets[index];

        // Search all ways in the set for a hit
        for (CacheBlock block : currentSet) {
            if (block.matches(tag)) {
                // -------- HIT --------
                stats.recordHit();
                block.setLastUsed(accessCounter);
                if (isWrite) {
                    writePolicy.onHit(block, stats);
                }
                return;
            }
        }

        // -------- MISS --------
        stats.recordMiss();
        CacheBlock victim = replacementPolicy.evict(currentSet);
        victim.load(tag, insertCounter++);
        if (isWrite) {
            writePolicy.onMiss(victim, stats);
        }
    }
}
