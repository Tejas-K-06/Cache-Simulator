package write;

import cache.CacheBlock;
import stats.SimulationStats;

/**
 * Write-Through policy: writes go to the cache and memory simultaneously.
 * Blocks are never marked dirty — memory is always up to date.
 *
 * Pros: cache and memory always consistent; eviction is always clean.
 * Cons: every write causes a memory bus transaction (higher bus traffic).
 */
public class WriteThrough implements WritePolicy {

    /**
     * Write hit — propagate write to memory immediately. Block stays clean.
     */
    @Override
    public void onHit(CacheBlock block, SimulationStats stats) {
        // Block is not marked dirty; memory is written through right now.
        if (stats != null) {
            stats.recordWriteBack(); // counts immediate memory writes
        }
    }

    /**
     * Write miss — block is already loaded (Write-Allocate); propagate to memory.
     * Block stays clean since memory is updated in parallel.
     */
    @Override
    public void onMiss(CacheBlock block, SimulationStats stats) {
        if (stats != null) {
            stats.recordWriteBack(); // counts immediate memory writes
        }
    }

    /**
     * Eviction handler — blocks are always clean under Write-Through, so no flush needed.
     * Just records the eviction and invalidates the block.
     *
     * @param victim The block being evicted (always clean).
     * @param stats  Stats to record the eviction event (may be null).
     */
    public void onEvict(CacheBlock victim, SimulationStats stats) {
        if (stats != null) {
            stats.recordEviction(); // no write-back needed; block was never dirty
        }
        victim.invalidate();
    }

    @Override
    public String toString() {
        return "WriteThrough";
    }
}
