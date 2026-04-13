package cache;

import policy.ReplacementPolicy;
import write.WritePolicy;
import stats.SimulationStats;

/**
 * Direct Mapped Cache — every address maps to exactly one block slot.
 *
 * No eviction policy is needed: on a miss, you simply overwrite the
 * single block at the computed index.
 *
 * Index bits = log2(numberOfBlocks)
 */
public class DirectMappedCache extends Cache {

    private CacheBlock[] blocks;

    private long accessCounter = 0;   // increment on every access — used by LRU
    private long insertCounter = 0;   // increment on every block load — used by FIFO

    /**
     * @param cacheSize         Total size of cache in bytes
     * @param blockSize         Size of each block in bytes
     * @param replacementPolicy Unused in Direct Mapped (only one candidate slot)
     * @param writePolicy       WriteBack or WriteThrough policy instance
     * @param stats             Shared stats object to record hits/misses
     * @param addressBits       Total number of bits in physical address
     */
    public DirectMappedCache(int cacheSize, int blockSize,
                             ReplacementPolicy replacementPolicy,
                             WritePolicy writePolicy,
                             SimulationStats stats,
                             int addressBits) {
        super(cacheSize, blockSize, replacementPolicy, writePolicy, stats, addressBits);

        blocks = new CacheBlock[numberOfBlocks];
        for (int i = 0; i < numberOfBlocks; i++) {
            blocks[i] = new CacheBlock();
        }
    }

    /**
     * Direct Mapped uses log2(numberOfBlocks) index bits to select
     * the unique block slot for any address.
     */
    @Override
    protected int computeIndexBits() {
        return log2(numberOfBlocks);
    }

    /**
     * Process a single memory access.
     *
     * 1. Decompose address into index and tag.
     * 2. Check the single block at that index for a tag match.
     * 3. HIT  → record hit; on write delegate to writePolicy.
     * 4. MISS → record miss; overwrite block with new tag; on write delegate to writePolicy.
     */
    @Override
    public void access(int address, boolean isWrite) {
        accessCounter++;

        int index = getIndex(address);
        int tag   = getTag(address);

        if (blocks[index].matches(tag)) {
            // -------- HIT --------
            stats.recordHit(isWrite);
            if (isWrite) {
                writePolicy.onHit(blocks[index], stats);
            }
        } else {
            // -------- MISS --------
            stats.recordMiss(isWrite);
            blocks[index].load(tag, insertCounter++);
            if (isWrite) {
                writePolicy.onMiss(blocks[index], stats);
            }
        }
    }
}
