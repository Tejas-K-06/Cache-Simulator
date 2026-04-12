package policy;

import cache.CacheBlock;

/**
 * FIFO (First-In, First-Out) replacement policy.
 * Evicts the block with the smallest insertOrder — the one loaded earliest.
 *
 * Stateless: insertOrder is stamped onto CacheBlock at load time via
 * CacheBlock.load(tag, insertCounter).
 */
public class FIFOPolicy implements ReplacementPolicy {

    /**
     * Returns the victim block to evict.
     *
     * Prefers invalid (empty) blocks to avoid unnecessary evictions.
     * Among valid blocks, returns the one with the smallest insertOrder.
     *
     * @param candidates Non-null, non-empty array of CacheBlocks.
     * @return The oldest-loaded CacheBlock. Never null.
     * @throws IllegalArgumentException if candidates is null or empty.
     */
    @Override
    public CacheBlock evict(CacheBlock[] candidates) {
        if (candidates == null || candidates.length == 0) {
            throw new IllegalArgumentException(
                "FIFOPolicy.evict(): candidates must not be null or empty."
            );
        }

        // Prefer any invalid (empty) slot — no real eviction needed.
        for (CacheBlock block : candidates) {
            if (!block.isValid()) {
                return block;
            }
        }

        // All blocks valid — return the one loaded first (smallest insertOrder).
        CacheBlock oldest = candidates[0];
        for (int i = 1; i < candidates.length; i++) {
            if (candidates[i].getInsertOrder() < oldest.getInsertOrder()) {
                oldest = candidates[i];
            }
        }
        return oldest;
    }
}
