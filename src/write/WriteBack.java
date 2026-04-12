package write;

import cache.CacheBlock;

public class WriteBack implements WritePolicy {
    @Override
    public void onHit(CacheBlock block) {
        block.markDirty();
    }

    @Override
    public void onMiss(CacheBlock block) {
        block.markDirty();
    }
}
