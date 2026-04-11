package stats;

/**
 * Tracks simulation-wide statistics: hits, misses, and derived metrics.
 *
 * Shared across all cache instances in a simulation run.
 */
public class SimulationStats {

    private long hits;
    private long misses;

    public SimulationStats() {
        this.hits = 0;
        this.misses = 0;
    }

    /** Record a cache hit. */
    public void recordHit() {
        hits++;
    }

    /** Record a cache miss. */
    public void recordMiss() {
        misses++;
    }

    public long getHits()        { return hits; }
    public long getMisses()      { return misses; }
    public long getTotalAccesses() { return hits + misses; }

    public double getHitRate() {
        long total = getTotalAccesses();
        return total == 0 ? 0.0 : (double) hits / total;
    }

    public double getMissRate() {
        long total = getTotalAccesses();
        return total == 0 ? 0.0 : (double) misses / total;
    }

    @Override
    public String toString() {
        return String.format(
            "SimulationStats[hits=%d, misses=%d, total=%d, hitRate=%.4f]",
            hits, misses, getTotalAccesses(), getHitRate()
        );
    }
}
