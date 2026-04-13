package cache;

import policy.ReplacementPolicy;
import write.WritePolicy;
import stats.SimulationStats;

public class FullyAssociativeCache extends Cache {

    private CacheBlock[] blocks;

    private long accessCounter = 0;   // increment on every access — used by LRU
    private long insertCounter = 0;   // increment on every block load — used by FIFO

    /**
     * @param cacheSize         Total size of cache in bytes
     * @param blockSize         Size of each block in bytes
     * @param replacementPolicy LRU or FIFO policy instance for eviction
     * @param writePolicy       WriteBack or WriteThrough policy instance
     * @param stats             Shared stats object to record hits/misses
     */
    public FullyAssociativeCache(int cacheSize, int blockSize,
                                  ReplacementPolicy replacementPolicy,
                                  WritePolicy writePolicy,
                                  SimulationStats stats) {
        super(cacheSize, blockSize, replacementPolicy, writePolicy, stats);

        blocks = new CacheBlock[numberOfBlocks];
        for (int i = 0; i < numberOfBlocks; i++) {
            blocks[i] = new CacheBlock();
        }
    }

    @Override
    protected int computeIndexBits() {
        return 0;
    }

    @Override
    public void access(int address, boolean isWrite) {
        accessCounter++;

        int tag = getTag(address);

        for (CacheBlock block : blocks) {
            if (block.matches(tag)) {
                stats.recordHit();
                block.setLastUsed(accessCounter);
                if (isWrite) {
                    writePolicy.onHit(block);
                }
                return;
            }
        }

        stats.recordMiss();
        CacheBlock victim = replacementPolicy.evict(blocks);
        victim.load(tag, insertCounter++);
        if (isWrite) {
            writePolicy.onMiss(victim);
        }
    }
}
