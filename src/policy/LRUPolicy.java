package policy;

import cache.CacheBlock;

public class LRUPolicy implements ReplacementPolicy {
    @Override
    public CacheBlock evict(CacheBlock[] candidates) {
        if (candidates == null || candidates.length == 0) return null;
        CacheBlock victim = candidates[0];
        // simplified block picking stub:
        for (CacheBlock block : candidates) {
            if (!block.isValid()) return block;
        }
        return victim;
    }
}
