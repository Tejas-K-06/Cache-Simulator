package cache;

/**
 * Represents a single block (line) inside the cache.
 *
 * Each block tracks:
 *   - valid bit  : whether this block holds real data
 *   - dirty bit  : whether this block has been written to (for WriteBack)
 *   - tag        : the tag bits of the address stored here
 *   - lastUsed   : timestamp for LRU eviction
 *   - insertOrder: insertion counter for FIFO eviction
 */
public class CacheBlock {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private boolean valid;       // Is this block holding valid data?
    private boolean dirty;       // Has this block been written to (dirty for WriteBack)?
    private int tag;             // Tag of the address currently stored

    private long lastUsed;       // Timestamp of last access — used by LRU
    private long insertOrder;    // Insertion counter — used by FIFO


    /**
     * A freshly created block is invalid, clean, with tag = -1.
     */
    public CacheBlock() {
        this.valid = false;
        this.dirty = false;
        this.tag = -1;
        this.lastUsed = 0;
        this.insertOrder = 0;
    }

    // -------------------------------------------------------------------------
    // Core operations
    // -------------------------------------------------------------------------

    /**
     * Load a new address tag into this block.
     * Marks it valid and clean. Sets insertion order for FIFO.
     *
     * @param tag         Tag bits of the incoming address
     * @param insertOrder Global insertion counter at time of load
     */
    public void load(int tag, long insertOrder) {
        this.tag = tag;
        this.valid = true;
        this.dirty = false;
        this.insertOrder = insertOrder;
    }

    /**
     * Mark this block as dirty (used by WriteBack on a write hit/miss).
     */
    public void markDirty() {
        this.dirty = true;
    }

    /**
     * Invalidate this block (used on eviction or reset).
     */
    public void invalidate() {
        this.valid = false;
        this.dirty = false;
        this.tag = -1;
    }

    /**
     * Check if this block matches a given tag and is valid.
     *
     * @param tag Tag to check against
     * @return true if this block is a hit for the given tag
     */
    public boolean matches(int tag) {
        return this.valid && this.tag == tag;
    }


    public boolean isValid()               { return valid; }
    public boolean isDirty()               { return dirty; }
    public int getTag()                    { return tag; }

    public long getLastUsed()              { return lastUsed; }
    public void setLastUsed(long time)     { this.lastUsed = time; }

    public long getInsertOrder()           { return insertOrder; }

    @Override
    public String toString() {
        return String.format(
            "CacheBlock[valid=%b, dirty=%b, tag=0x%X, lastUsed=%d, insertOrder=%d]",
            valid, dirty, tag, lastUsed, insertOrder
        );
    }
}