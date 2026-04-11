package memory;

/**
 * Represents the Main Memory in the cache hierarchy.
 * Tracks the total number of accesses to RAM when cache levels miss.
 */
public class MainMemory {
    private long accessCount;

    public MainMemory() {
        this.accessCount = 0;
    }

    /**
     * Simulates an access to main memory.
     * @param address the memory address being accessed
     * @param isWrite true if the operation is a write, false for a read
     */
    public void access(int address, boolean isWrite) {
        accessCount++;
        // In a more complex simulation, you could add memory latency delays
        // or an actual byte array backing the memory here.
    }

    /**
     * Gets the total number of times the main memory was accessed.
     */
    public long getAccessCount() {
        return accessCount;
    }

    @Override
    public String toString() {
        return "MainMemory [total access count = " + accessCount + "]";
    }
}
