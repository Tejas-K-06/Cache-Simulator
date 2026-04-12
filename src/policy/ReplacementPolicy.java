package policy;

import cache.CacheBlock;

/**
 * Strategy interface for cache block eviction.
 *
 * Implementations (LRU, FIFO) decide which block to evict
 * when a cache miss occurs and all candidate blocks are full.
 */
public interface ReplacementPolicy {

    /**
     * Select a block to evict from the given array of candidate blocks.
     *
     * For Direct Mapped caches this is never called (only one choice).
     * For Fully Associative caches, candidates = all blocks.
     * For Set Associative caches, candidates = one set (way array).
     *
     * @param candidates Array of CacheBlocks to choose a victim from
     * @return The CacheBlock that should be evicted and replaced
     */
    CacheBlock evict(CacheBlock[] candidates);
}
