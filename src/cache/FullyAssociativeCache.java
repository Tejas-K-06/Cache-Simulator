package cache;

import policy.ReplacementPolicy;
import write.WritePolicy;
import stats.SimulationStats;
import trace.MemoryAccess;

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
     * @param addressBits       Total number of bits in physical address
     */
    public FullyAssociativeCache(int cacheSize, int blockSize,
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

    @Override
    protected int computeIndexBits() {
        return 0;
    }

    @Override
    public MemoryAccess access(int address, boolean isWrite) {
        int tag = getTag(address);

        for (CacheBlock block : blocks) {
            if (block.matches(tag)) {
                // -------- HIT --------
                stats.recordHit(isWrite);
                block.setLastUsed(++accessCounter);
                if (isWrite) {
                    writePolicy.onHit(block, stats);
                }
                return null;
            }
        }

        // -------- MISS --------
        stats.recordMiss(isWrite);

        // Evict from any block in the cache
        CacheBlock victim = replacementPolicy.evict(blocks);
        MemoryAccess writeBack = null;

        if (victim.isValid()) {
            if (victim.isDirty()) {
                int victimAddress = (victim.getTag() << offsetBits);
                writeBack = new MemoryAccess(victimAddress, true);
            }
            writePolicy.onEvict(victim, stats);
        }

        victim.load(tag, insertCounter++);

        if (isWrite) {
            writePolicy.onMiss(victim, stats);
        }
        
        return writeBack;
    }
}
