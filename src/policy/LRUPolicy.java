package policy;

import cache.CacheBlock;

/**
 * LRU (Least-Recently-Used) Cache Replacement Policy.
 *
 * Evicts the block that was accessed least recently —
 * i.e., the one with the smallest {@code lastUsed} timestamp.
 *
 * This is stateless: no counters are stored here because
 * {@code lastUsed} is already stamped onto every {@link CacheBlock}
 * on every hit (via {@code block.setLastUsed(accessCounter)}) inside
 * the cache's {@code access()} method.
 *
 * Usage with the Strategy Pattern:
 * <pre>
 *     ReplacementPolicy policy = new LRUPolicy();
 *     CacheBlock victim = policy.evict(candidateBlocks);
 * </pre>
 *
 * Tie-breaking: if two blocks share the same {@code lastUsed} timestamp
 * (e.g., both were never accessed after being loaded), the one at the
 * lower array index is chosen.
 */
public class LRUPolicy implements ReplacementPolicy {

    /**
     * Select the victim block to evict from {@code candidates}.
     *
     * Algorithm:
     * <ol>
     *   <li>If any block is invalid (not yet filled), prefer it — no eviction needed.</li>
     *   <li>Otherwise, return the block with the smallest {@code lastUsed} timestamp
     *       — it is the one that was accessed least recently (LRU).</li>
     * </ol>
     *
     * @param candidates Array of {@link CacheBlock}s to search for a victim.
     *                   For Set-Associative caches this is one set (way array).
     *                   For Fully-Associative caches this is all blocks.
     * @return The {@link CacheBlock} that should be evicted and overwritten.
     * @throws IllegalArgumentException if {@code candidates} is null or empty.
     */
    @Override
    public CacheBlock evict(CacheBlock[] candidates) {
        if (candidates == null || candidates.length == 0) {
            throw new IllegalArgumentException(
                "LRUPolicy.evict(): candidates array must not be null or empty."
            );
        }

        // Prefer any invalid (empty) block — avoids unnecessary eviction
        for (CacheBlock block : candidates) {
            if (!block.isValid()) {
                return block;
            }
        }

        // All blocks are valid — find the one accessed least recently (smallest lastUsed)
        CacheBlock lru = candidates[0];
        for (int i = 1; i < candidates.length; i++) {
            if (candidates[i].getLastUsed() < lru.getLastUsed()) {
                lru = candidates[i];
            }
        }
        return lru;
    }
}
