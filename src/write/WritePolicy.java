package write;

import cache.CacheBlock;

/**
 * Strategy interface for cache write handling.
 *
 * Implementations (WriteBack, WriteThrough) define what happens
 * to a block on a write hit vs. a write miss.
 */
public interface WritePolicy {

    /**
     * Called when a write access results in a cache hit.
     *
     * @param block The block that was hit
     */
    void onHit(CacheBlock block);

    /**
     * Called when a write access results in a cache miss
     * (after the new block has been loaded).
     *
     * @param block The newly loaded block
     */
    void onMiss(CacheBlock block);
}
