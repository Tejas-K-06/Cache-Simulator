package policy;

import cache.CacheBlock;

public class FIFOPolicy implements ReplacementPolicy {
    @Override
    public CacheBlock evict(CacheBlock[] candidates) {
        if (candidates == null || candidates.length == 0) return null;
        CacheBlock victim = candidates[0];
        long oldest = Long.MAX_VALUE; // Assuming CacheBlock tracks insert time if using FIFO properly. 
        // For simple stub:
        for (CacheBlock block : candidates) {
             // simplified block picking, logic should be implemented by person 1 or 3
            if (!block.isValid()) return block;
        }
        return victim;
    }
}
