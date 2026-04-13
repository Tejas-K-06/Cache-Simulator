package cache;

import policy.ReplacementPolicy;
import write.WritePolicy;
import stats.SimulationStats;
import trace.MemoryAccess;

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

    @Override
    protected int computeIndexBits() {
        return log2(numberOfBlocks);
    }

    @Override
    public MemoryAccess access(int address, boolean isWrite) {
        accessCounter++;

        int index = getIndex(address);
        int tag   = getTag(address);

        if (blocks[index].matches(tag)) {
            // -------- HIT --------
            stats.recordHit(isWrite);
            blocks[index].setLastUsed(++accessCounter);
            if (isWrite) {
                writePolicy.onHit(blocks[index], stats);
            }
            return null;
        } else {
            // -------- MISS --------
            stats.recordMiss(isWrite);

            CacheBlock victim = blocks[index];
            MemoryAccess writeBack = null;

            if (victim.isValid()) {
                if (victim.isDirty()) {
                    int victimAddress = (victim.getTag() << (indexBits + offsetBits)) | (index << offsetBits);
                    writeBack = new MemoryAccess(victimAddress, true);
                }
                writePolicy.onEvict(victim, stats);
            }

            blocks[index].load(tag, insertCounter++);

            if (isWrite) {
                writePolicy.onMiss(blocks[index], stats);
            }
            
            return writeBack;
        }
    }
}
