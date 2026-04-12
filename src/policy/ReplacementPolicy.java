package policy;

import cache.CacheBlock;

/**
 * Strategy interface for cache block eviction.
 *
 * Implementations:
 *   - LRUPolicy  : evicts the block with the smallest lastUsed timestamp.
 *   - FIFOPolicy : evicts the block with the smallest insertOrder timestamp.
 *
 * Contract for implementors:
 *   1. Prefer invalid (empty) blocks — return them before evicting a valid block.
 *   2. Never return null — always return an element from the candidates array.
 *   3. Do not mutate the block — selection only; loading is the cache's job.
 *   4. Be stateless — all tracking data lives on CacheBlock itself.
 */
public interface ReplacementPolicy {

    /**
     * Select and return the block to evict from the given candidates.
     *
     * Caller context:
     *   - Direct-Mapped   : never called (only one candidate slot).
     *   - Set-Associative : candidates = way array for the matched set.
     *   - Fully-Associative: candidates = all blocks.
     *
     * @param candidates Non-null, non-empty array of CacheBlocks.
     * @return The CacheBlock to evict and overwrite. Never null.
     * @throws IllegalArgumentException if candidates is null or empty.
     */
    CacheBlock evict(CacheBlock[] candidates);
}
