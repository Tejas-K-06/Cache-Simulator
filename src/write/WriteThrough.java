package write;

import cache.CacheBlock;

public class WriteThrough implements WritePolicy {
    @Override
    public void onHit(CacheBlock block) {
        // Write hits update main memory inline
    }

    @Override
    public void onMiss(CacheBlock block) {
        // Write miss handling
    }
}
