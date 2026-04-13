package cache;

import policy.ReplacementPolicy;
import write.WritePolicy;
import stats.SimulationStats;
import trace.MemoryAccess;


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
     * @param addressBits       Total number of bits in physical address
     */
    public SetAssociativeCache(int cacheSize, int blockSize,
                                int associativity,
                                ReplacementPolicy replacementPolicy,
                                WritePolicy writePolicy,
                                SimulationStats stats,
                                int addressBits) {
        super(cacheSize, blockSize, replacementPolicy, writePolicy, stats, addressBits);

        this.associativity = associativity;
        this.numberOfSets  = numberOfBlocks / associativity;

        this.indexBits = computeIndexBits();
        this.tagBits   = addressBits - indexBits - offsetBits;

        sets = new CacheBlock[numberOfSets][associativity];
        for (int i = 0; i < numberOfSets; i++) {
            for (int j = 0; j < associativity; j++) {
                sets[i][j] = new CacheBlock();
            }
        }
    }

    @Override
    protected int computeIndexBits() {
        return log2(numberOfSets);
    }

    @Override
    public MemoryAccess access(int address, boolean isWrite) {
        int index = getIndex(address);
        int tag   = getTag(address);

        CacheBlock[] currentSet = sets[index];

        for (CacheBlock block : currentSet) {
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

        // Find victim block within the set
        CacheBlock victim = replacementPolicy.evict(currentSet);
        MemoryAccess writeBack = null;

        if (victim.isValid()) {
            if (victim.isDirty()) {
                int victimAddress = (victim.getTag() << (indexBits + offsetBits)) | (index << offsetBits);
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
