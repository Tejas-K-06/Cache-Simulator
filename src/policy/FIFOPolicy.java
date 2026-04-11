package policy;

import cache.CacheBlock;

/**
 * FIFO (First-In, First-Out) Cache Replacement Policy.
 *
 * Evicts the block that was loaded into the cache earliest —
 * i.e., the one with the smallest {@code insertOrder} value.
 *
 * This is stateless: no counters are stored here because
 * {@code insertOrder} is already stamped onto every {@link CacheBlock}
 * when it is loaded (via {@code CacheBlock.load(tag, insertCounter)}).
 *
 * Usage with the Strategy Pattern:
 * <pre>
 *     ReplacementPolicy policy = new FIFOPolicy();
 *     CacheBlock victim = policy.evict(candidateBlocks);
 * </pre>
 *
 * Tie-breaking: if two blocks share the same {@code insertOrder}
 * (which should not happen in normal operation), the one at the
 * lower array index is chosen.
 */
public class FIFOPolicy implements ReplacementPolicy {

    /**
     * Select the victim block to evict from {@code candidates}.
     *
     * Algorithm:
     * <ol>
     *   <li>If any block is invalid (not yet filled), prefer it — no eviction needed.</li>
     *   <li>Otherwise, return the block with the smallest {@code insertOrder}
     *       — it is the one that was loaded first (FIFO).</li>
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
                "FIFOPolicy.evict(): candidates array must not be null or empty."
            );
        }

        // Prefer any invalid (empty) block — avoids unnecessary eviction
        for (CacheBlock block : candidates) {
            if (!block.isValid()) {
                return block;
            }
        }

        // All blocks are valid — find the one inserted earliest (smallest insertOrder)
        CacheBlock oldest = candidates[0];
        for (int i = 1; i < candidates.length; i++) {
            if (candidates[i].getInsertOrder() < oldest.getInsertOrder()) {
                oldest = candidates[i];
            }
        }
        return oldest;
    }
}
