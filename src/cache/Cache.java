package cache;

import policy.ReplacementPolicy;
import write.WritePolicy;
import stats.SimulationStats;
import trace.MemoryAccess;

public abstract class Cache {

    protected int cacheSize;        // Total cache size in bytes
    protected int blockSize;        // Size of each cache block in bytes
    protected int numberOfBlocks;   // Total number of blocks = cacheSize / blockSize

    protected int offsetBits;       // Number of bits for block offset
    protected int indexBits;        // Number of bits for index (0 for fully associative)
    protected int tagBits;          // Number of bits for tag
    protected int addressBits;      // Total address bits computed from MainMemorySize

    protected ReplacementPolicy replacementPolicy;
    protected WritePolicy writePolicy;

    protected SimulationStats stats;

    /**
     * @param cacheSize         Total size of cache in bytes
     * @param blockSize         Size of each block in bytes
     * @param replacementPolicy LRU or FIFO policy instance
     * @param writePolicy       WriteBack or WriteThrough policy instance
     * @param stats             Shared stats object to record hits/misses
     * @param addressBits       Total number of bits in physical address
     */
    
    public Cache(int cacheSize, int blockSize,
                ReplacementPolicy replacementPolicy,
                WritePolicy writePolicy,
                SimulationStats stats,
                int addressBits) {

        this.cacheSize = cacheSize;
        this.blockSize = blockSize;
        this.numberOfBlocks = cacheSize / blockSize;
        this.replacementPolicy = replacementPolicy;
        this.writePolicy = writePolicy;
        this.stats = stats;
        this.addressBits = addressBits;

        this.offsetBits = log2(blockSize);
        this.indexBits = computeIndexBits();
        this.tagBits = addressBits - indexBits - offsetBits;
    }

    /**
     * Process a single memory access (read or write).
     * Must update stats on hit or miss.
     *
     * @param address 32-bit physical memory address
     * @param isWrite true if writing, false if reading
     * @return MemoryAccess if a write-back generated, null otherwise
     */
    public abstract MemoryAccess access(int address, boolean isWrite);

    /**
     * Compute how many index bits this cache type uses.
     * Direct Mapped and Set Associative have index bits.
     * Fully Associative has 0 index bits.
     */
    protected abstract int computeIndexBits();

    protected int getOffset(int address) {
        return address & ((1 << offsetBits) - 1);
    }

    protected int getIndex(int address) {
        if (indexBits == 0) return 0;
        return (address >> offsetBits) & ((1 << indexBits) - 1);
    }

    protected int getTag(int address) {
        return (address >> (offsetBits + indexBits));
    }

    protected int log2(int value) {
        return (int) (Math.log(value) / Math.log(2));
    }

    public int getCacheSize()     { return cacheSize; }
    public int getBlockSize()     { return blockSize; }
    public int getNumberOfBlocks(){ return numberOfBlocks; }
    public int getOffsetBits()    { return offsetBits; }
    public int getIndexBits()     { return indexBits; }
    public int getTagBits()       { return tagBits; }
    public SimulationStats getStats() { return stats; }

    @Override
    public String toString() {
        return String.format(
            "[%s] Size=%d B | BlockSize=%d B | Blocks=%d | tagBits=%d | indexBits=%d | offsetBits=%d",
            getClass().getSimpleName(), cacheSize, blockSize, numberOfBlocks, tagBits, indexBits, offsetBits
        );
    }
}