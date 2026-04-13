package cache;

import policy.ReplacementPolicy;
import write.WritePolicy;
import stats.SimulationStats;

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
     */
    public DirectMappedCache(int cacheSize, int blockSize,
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
        return log2(numberOfBlocks);
    }

    @Override
    public void access(int address, boolean isWrite) {
        accessCounter++;

        int index = getIndex(address);
        int tag   = getTag(address);

        if (blocks[index].matches(tag)) {
            stats.recordHit();
            if (isWrite) {
                writePolicy.onHit(blocks[index]);
            }
        } else {
            stats.recordMiss();
            blocks[index].load(tag, insertCounter++);
            if (isWrite) {
                writePolicy.onMiss(blocks[index]);
            }
        }
    }
}
