package trace;

/**
 * Represents a single memory access request (either from a trace file or CPU simulator).
 */
public class MemoryAccess {
    private final int address;
    private final boolean isWrite;

    /**
     * @param address The 32-bit memory address being accessed.
     * @param isWrite true if writing to memory, false if reading.
     */
    public MemoryAccess(int address, boolean isWrite) {
        this.address = address;
        this.isWrite = isWrite;
    }

    public int getAddress() {
        return address;
    }

    public boolean isWrite() {
        return isWrite;
    }

    @Override
    public String toString() {
        return String.format("%s @ address 0x%08X", isWrite ? "WRITE" : "READ ", address);
    }
}
