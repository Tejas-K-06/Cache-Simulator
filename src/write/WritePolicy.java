package write;

import cache.CacheBlock;
import stats.SimulationStats;

/**
 * Strategy interface for cache write handling.
 *
 * Implementations:
 *   - WriteBack    : writes only to cache (dirty bit); flushes to memory on eviction.
 *   - WriteThrough : writes to cache and memory simultaneously (block stays clean).
 *
 * The cache calls onHit() on a write hit and onMiss() after loading a block on a write miss.
 * Implementations should be stateless and not mutate anything beyond the supplied block/stats.
 */
public interface WritePolicy {

    /**
     * Called on a write hit. Block is valid and tag-matched.
     *
     * @param block The hit block.
     * @param stats Cache-level stats (may be null for backward compatibility).
     */
    void onHit(CacheBlock block, SimulationStats stats);

    /**
     * Called on a write miss, after the cache has already loaded the new block.
     *
     * @param block The newly loaded block.
     * @param stats Cache-level stats (may be null for backward compatibility).
     */
    void onMiss(CacheBlock block, SimulationStats stats);

    /**
     * Backward-compatible overload — delegates to onHit(block, null).
     */
    default void onHit(CacheBlock block) {
        onHit(block, null);
    }

    /**
     * Backward-compatible overload — delegates to onMiss(block, null).
     */
    default void onMiss(CacheBlock block) {
        onMiss(block, null);
    }

    /**
     * Called when a block is actively evicted to make room for a new allocation.
     *
     * @param victim The block being evicted.
     * @param stats Cache-level stats.
     */
    default void onEvict(CacheBlock victim, SimulationStats stats) {
        victim.invalidate();
    }
}
