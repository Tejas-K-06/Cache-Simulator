package config;

import cache.Cache;
import cache.DirectMappedCache;
import cache.FullyAssociativeCache;
import cache.SetAssociativeCache;
import exception.CacheConfigException;
import multilevel.CacheHierarchy;
import policy.FIFOPolicy;
import policy.LRUPolicy;
import policy.ReplacementPolicy;
import stats.SimulationStats;
import write.WriteBack;
import write.WritePolicy;
import write.WriteThrough;
import memory.MainMemory;

import java.util.List;

/**
 * Instantiates the correct cache types based on parsed configuration parameters.
 */
public class CacheFactory {

    /**
     * Builds a full CacheHierarchy from the provided ConfigLoader.
     * @param loader A populated ConfigLoader
     * @return Fully configured CacheHierarchy
     */
    public static CacheHierarchy createHierarchy(ConfigLoader loader) {
        MainMemory mainMemory = new MainMemory(); // assuming MainMemory has a default constructor (or we can inject latency/size later)
        CacheHierarchy hierarchy = new CacheHierarchy(mainMemory);

        long mainMemorySize = loader.getMainMemorySize();
        int addressBits = (int) (Math.log(mainMemorySize) / Math.log(2));

        List<CacheConfig> configs = loader.getCacheConfigs();

        for (CacheConfig config : configs) {
            SimulationStats stats = new SimulationStats();
            ReplacementPolicy repPolicy = createReplacementPolicy(config.getReplacementPolicy());
            WritePolicy writePolicy = createWritePolicy(config.getWritePolicy(), mainMemory);

            Cache cacheLevel;
            switch (config.getType().toLowerCase()) {
                case "directmapped":
                    cacheLevel = new DirectMappedCache(
                            config.getSize(),
                            config.getBlockSize(),
                            repPolicy,
                            writePolicy,
                            stats,
                            addressBits
                    );
                    break;

                case "setassociative":
                    cacheLevel = new SetAssociativeCache(
                            config.getSize(),
                            config.getBlockSize(),
                            config.getAssociativity(),
                            repPolicy,
                            writePolicy,
                            stats,
                            addressBits
                    );
                    break;

                case "fullyassociative":
                    cacheLevel = new FullyAssociativeCache(
                            config.getSize(),
                            config.getBlockSize(),
                            repPolicy,
                            writePolicy,
                            stats,
                            addressBits
                    );
                    break;

                default:
                    throw new CacheConfigException("Unknown cache type: " + config.getType());
            }

            hierarchy.addLevel(cacheLevel);
        }

        return hierarchy;
    }

    private static ReplacementPolicy createReplacementPolicy(String policyName) {
        if (policyName == null) return new LRUPolicy();
        switch (policyName.toLowerCase()) {
            case "fifo": return new FIFOPolicy();
            case "lru":  return new LRUPolicy();
            default:     return new LRUPolicy(); // fallback
        }
    }

    private static WritePolicy createWritePolicy(String policyName, MainMemory memory) {
        if (policyName == null) return new WriteBack();
        switch (policyName.toLowerCase()) {
            case "writethrough": return new WriteThrough();
            case "writeback":    return new WriteBack();
            default:             return new WriteBack();
        }
    }
}
