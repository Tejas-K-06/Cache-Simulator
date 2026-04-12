package write;

import cache.CacheBlock;
import stats.SimulationStats;

/**
 * Write-Back policy: writes go only to the cache block (marked dirty).
 * Memory is updated only when the dirty block is evicted.
 *
 * Pros: fewer memory writes, lower bus traffic.
 * Cons: cache and memory may be temporarily inconsistent.
 */
public class WriteBack implements WritePolicy {

    /**
     * Write hit — mark block dirty. Memory is not updated yet.
     */
    @Override
    public void onHit(CacheBlock block, SimulationStats stats) {
        block.markDirty();
    }

    /**
     * Write miss — block is already loaded; mark it dirty (Write-Allocate).
     * Memory flush is deferred until eviction.
     */
    @Override
    public void onMiss(CacheBlock block, SimulationStats stats) {
        block.markDirty();
    }

    /**
     * Eviction handler — flushes dirty blocks to memory and invalidates the block.
     * Call this from the cache's eviction path when the victim block is replaced.
     *
     * @param victim The block being evicted.
     * @param stats  Stats to record eviction/write-back events (may be null).
     */
    public void onEvict(CacheBlock victim, SimulationStats stats) {
        if (stats != null) {
            if (victim.isDirty()) {
                stats.recordWriteBack(); // dirty block flushed to next level / RAM
            }
            stats.recordEviction();
        }
        victim.invalidate();
    }

    @Override
    public String toString() {
        return "WriteBack";
    }
}
