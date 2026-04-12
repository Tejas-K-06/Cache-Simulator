package policy;

import cache.CacheBlock;

/**
 * LRU (Least-Recently-Used) replacement policy.
 * Evicts the block with the smallest lastUsed timestamp — the one accessed longest ago.
 *
 * Stateless: lastUsed is updated on every hit via CacheBlock.setLastUsed(accessCounter).
 */
public class LRUPolicy implements ReplacementPolicy {

    /**
     * Returns the victim block to evict.
     *
     * Prefers invalid (empty) blocks to avoid unnecessary evictions.
     * Among valid blocks, returns the one with the smallest lastUsed value.
     *
     * @param candidates Non-null, non-empty array of CacheBlocks.
     * @return The least-recently-used CacheBlock. Never null.
     * @throws IllegalArgumentException if candidates is null or empty.
     */
    @Override
    public CacheBlock evict(CacheBlock[] candidates) {
        if (candidates == null || candidates.length == 0) {
            throw new IllegalArgumentException(
                "LRUPolicy.evict(): candidates must not be null or empty."
            );
        }

        // Prefer any invalid (empty) slot — no real eviction needed.
        for (CacheBlock block : candidates) {
            if (!block.isValid()) {
                return block;
            }
        }

        // All blocks valid — return the one accessed least recently (smallest lastUsed).
        CacheBlock lru = candidates[0];
        for (int i = 1; i < candidates.length; i++) {
            if (candidates[i].getLastUsed() < lru.getLastUsed()) {
                lru = candidates[i];
            }
        }
        return lru;
    }
}
