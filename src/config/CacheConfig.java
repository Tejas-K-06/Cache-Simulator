package config;

/**
 * POJO for holding parsed cache configuration properties.
 */
public class CacheConfig {
    private String level;
    private String type;         // DirectMapped, SetAssociative, FullyAssociative
    private int size;
    private int blockSize;
    private int associativity;   // 0 if DirectMapped or FullyAssociative
    private String replacementPolicy;
    private String writePolicy;

    public CacheConfig() {
        this.associativity = 0; // default
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(int blockSize) {
        this.blockSize = blockSize;
    }

    public int getAssociativity() {
        return associativity;
    }

    public void setAssociativity(int associativity) {
        this.associativity = associativity;
    }

    public String getReplacementPolicy() {
        return replacementPolicy;
    }

    public void setReplacementPolicy(String replacementPolicy) {
        this.replacementPolicy = replacementPolicy;
    }

    public String getWritePolicy() {
        return writePolicy;
    }

    public void setWritePolicy(String writePolicy) {
        this.writePolicy = writePolicy;
    }

    @Override
    public String toString() {
        return "CacheConfig{" +
                "level='" + level + '\'' +
                ", type='" + type + '\'' +
                ", size=" + size +
                ", blockSize=" + blockSize +
                ", associativity=" + associativity +
                ", replacementPolicy='" + replacementPolicy + '\'' +
                ", writePolicy='" + writePolicy + '\'' +
                '}';
    }
}
