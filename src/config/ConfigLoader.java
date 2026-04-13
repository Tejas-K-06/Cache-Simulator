package config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A lightweight, standalone JSON loader specifically designed for the cache_config.json format.
 * Does not use third-party libraries.
 */
public class ConfigLoader {

    private final String filepath;
    private final List<CacheConfig> cacheConfigs;
    private long mainMemorySize;
    private int mainMemoryLatency;

    public ConfigLoader(String filepath) {
        this.filepath = filepath;
        this.cacheConfigs = new ArrayList<>();
    }

    public void load() throws IOException {
        StringBuilder jsonBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonBuilder.append(line.trim());
            }
        }
        
        String json = jsonBuilder.toString();
        
        // Remove spaces outside quotes (simplistic)
        json = json.replaceAll("\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)", "");

        // Find main attributes manually
        mainMemorySize = extractLongProperty(json, "mainMemorySize");
        mainMemoryLatency = extractIntProperty(json, "mainMemoryLatency");

        // Extract "caches" array
        int arrayStart = json.indexOf("\"caches\":[");
        if (arrayStart != -1) {
            arrayStart += "\"caches\":[".length();
            int arrayEnd = json.indexOf("]", arrayStart);
            String arrayContent = json.substring(arrayStart, arrayEnd);

            // Split into individual objects
            int curIndex = 0;
            while (curIndex < arrayContent.length()) {
                int objStart = arrayContent.indexOf("{", curIndex);
                if (objStart == -1) break;
                
                int objEnd = arrayContent.indexOf("}", objStart);
                if (objEnd == -1) break;

                String objContent = arrayContent.substring(objStart + 1, objEnd);
                
                CacheConfig config = new CacheConfig();
                config.setLevel(extractStringProperty(objContent, "level"));
                config.setType(extractStringProperty(objContent, "type"));
                config.setSize(extractIntProperty(objContent, "size"));
                config.setBlockSize(extractIntProperty(objContent, "blockSize"));
                
                int assoc = extractIntProperty(objContent, "associativity");
                if (assoc != -1) {
                    config.setAssociativity(assoc);
                }
                
                config.setReplacementPolicy(extractStringProperty(objContent, "replacementPolicy"));
                config.setWritePolicy(extractStringProperty(objContent, "writePolicy"));
                
                cacheConfigs.add(config);
                curIndex = objEnd + 1;
            }
        }
    }

    private String extractStringProperty(String json, String property) {
        String searchStr = "\"" + property + "\":\"";
        int start = json.indexOf(searchStr);
        if (start == -1) return null;
        start += searchStr.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private int extractIntProperty(String json, String property) {
        String searchStr = "\"" + property + "\":";
        int start = json.indexOf(searchStr);
        if (start == -1) return -1;
        start += searchStr.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (start == end) return -1;
        return Integer.parseInt(json.substring(start, end));
    }

    private long extractLongProperty(String json, String property) {
        String searchStr = "\"" + property + "\":";
        int start = json.indexOf(searchStr);
        if (start == -1) return -1;
        start += searchStr.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (start == end) return -1;
        return Long.parseLong(json.substring(start, end));
    }

    public List<CacheConfig> getCacheConfigs() {
        return cacheConfigs;
    }

    public long getMainMemorySize() {
        return mainMemorySize;
    }

    public int getMainMemoryLatency() {
        return mainMemoryLatency;
    }
}
