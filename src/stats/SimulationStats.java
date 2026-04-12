package stats;

/**
 * Tracks all simulation statistics for a single cache level.
 *
 * Maintains counters for:
 *   - Total hits and misses (backward-compatible with existing callers)
 *   - Read hits, read misses, write hits, write misses (split by operation type)
 *   - Eviction count (how many blocks were replaced)
 *   - Write-back count (dirty evictions written to the next level)
 *
 * Provides computed metrics:
 *   - Hit rate, miss rate (as percentages)
 *   - Total, read, and write access counts
 *   - A formatted summary report string
 */
public class SimulationStats {

    // -------------------------------------------------------------------------
    // Counters
    // -------------------------------------------------------------------------

    private long hits = 0;
    private long misses = 0;

    private long readHits = 0;
    private long readMisses = 0;
    private long writeHits = 0;
    private long writeMisses = 0;

    private long evictionCount = 0;
    private long writeBackCount = 0;

    // -------------------------------------------------------------------------
    // Recording methods — backward-compatible overloads
    // -------------------------------------------------------------------------

    /**
     * Record a generic hit (no read/write distinction).
     * Preserved for backward compatibility with Person 1's cache classes.
     */
    public void recordHit() {
        hits++;
    }

    /**
     * Record a generic miss (no read/write distinction).
     * Preserved for backward compatibility with Person 1's cache classes.
     */
    public void recordMiss() {
        misses++;
    }

    /**
     * Record a hit with read/write distinction.
     * Also increments the total hits counter.
     *
     * @param isWrite true if the hit was on a write operation
     */
    public void recordHit(boolean isWrite) {
        hits++;
        if (isWrite) {
            writeHits++;
        } else {
            readHits++;
        }
    }

    /**
     * Record a miss with read/write distinction.
     * Also increments the total misses counter.
     *
     * @param isWrite true if the miss was on a write operation
     */
    public void recordMiss(boolean isWrite) {
        misses++;
        if (isWrite) {
            writeMisses++;
        } else {
            readMisses++;
        }
    }

    /**
     * Record a block eviction.
     */
    public void recordEviction() {
        evictionCount++;
    }

    /**
     * Record a dirty block being written back to the next cache level or memory.
     */
    public void recordWriteBack() {
        writeBackCount++;
    }

    // -------------------------------------------------------------------------
    // Getters — raw counters
    // -------------------------------------------------------------------------

    public long getHits()          { return hits; }
    public long getMisses()        { return misses; }

    public long getReadHits()      { return readHits; }
    public long getReadMisses()    { return readMisses; }
    public long getWriteHits()     { return writeHits; }
    public long getWriteMisses()   { return writeMisses; }

    public long getEvictionCount() { return evictionCount; }
    public long getWriteBackCount(){ return writeBackCount; }

    // -------------------------------------------------------------------------
    // Computed metrics
    // -------------------------------------------------------------------------

    /**
     * @return Total number of accesses (hits + misses)
     */
    public long getTotalAccesses() {
        return hits + misses;
    }

    /**
     * @return Total number of read accesses
     */
    public long getReadAccesses() {
        return readHits + readMisses;
    }

    /**
     * @return Total number of write accesses
     */
    public long getWriteAccesses() {
        return writeHits + writeMisses;
    }

    /**
     * @return Hit rate as a percentage (0.0 to 100.0), or 0.0 if no accesses
     */
    public double getHitRate() {
        long total = getTotalAccesses();
        if (total == 0) return 0.0;
        return (hits * 100.0) / total;
    }

    /**
     * @return Miss rate as a percentage (0.0 to 100.0), or 0.0 if no accesses
     */
    public double getMissRate() {
        long total = getTotalAccesses();
        if (total == 0) return 0.0;
        return (misses * 100.0) / total;
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    /**
     * Reset all counters to zero. Useful for re-running simulations.
     */
    public void reset() {
        hits = 0;
        misses = 0;
        readHits = 0;
        readMisses = 0;
        writeHits = 0;
        writeMisses = 0;
        evictionCount = 0;
        writeBackCount = 0;
    }

    // -------------------------------------------------------------------------
    // Reporting
    // -------------------------------------------------------------------------

    /**
     * Returns a formatted multi-line summary of all statistics.
     * Used by CacheHierarchy.printReport() to display per-level stats.
     *
     * @return Formatted statistics report
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("  Total Accesses   : %d%n", getTotalAccesses()));
        sb.append(String.format("  Hits             : %d%n", hits));
        sb.append(String.format("  Misses           : %d%n", misses));
        sb.append(String.format("  Hit Rate         : %.2f%%%n", getHitRate()));
        sb.append(String.format("  Miss Rate        : %.2f%%%n", getMissRate()));
        sb.append(String.format("  ---- By Operation ----%n"));
        sb.append(String.format("  Read Hits        : %d%n", readHits));
        sb.append(String.format("  Read Misses      : %d%n", readMisses));
        sb.append(String.format("  Write Hits       : %d%n", writeHits));
        sb.append(String.format("  Write Misses     : %d%n", writeMisses));
        sb.append(String.format("  ---- Eviction Stats ----%n"));
        sb.append(String.format("  Evictions        : %d%n", evictionCount));
        sb.append(String.format("  Write-Backs      : %d", writeBackCount));

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format(
            "SimulationStats[hits=%d, misses=%d, hitRate=%.2f%%, evictions=%d, writeBacks=%d]",
            hits, misses, getHitRate(), evictionCount, writeBackCount
        );
    }
}
