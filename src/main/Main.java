package main;

import config.ConfigLoader;
import config.CacheFactory;
import multilevel.CacheHierarchy;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Cache Simulator...");

        String configFile = "config/cache_config.json";

        ConfigLoader loader = new ConfigLoader(configFile);
        try {
            loader.load();
            System.out.println("Configuration loaded successfully.");
            System.out.println("Main Memory Size: " + loader.getMainMemorySize());

            CacheHierarchy hierarchy = CacheFactory.createHierarchy(loader);

            System.out.println("Cache Hierarchy instanced successfully.");
            System.out.println(hierarchy.toString());

        } catch (IOException e) {
            System.err.println("Failed to load configuration file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error initializing cache hierarchy: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
