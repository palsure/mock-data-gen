package com.aimockdata.gen.ai.learning;

import com.aimockdata.gen.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache for storing and retrieving successful data generation patterns.
 * Helps improve generation quality by learning from previous successful generations.
 */
public class PatternCache {
    private static final Logger logger = LoggerFactory.getLogger(PatternCache.class);
    
    private final Map<String, List<PatternEntry>> patternCache;
    private final int maxCacheSize;
    private final boolean enabled;
    
    public PatternCache() {
        this(true, 100);
    }
    
    public PatternCache(boolean enabled, int maxCacheSize) {
        this.enabled = enabled;
        this.maxCacheSize = maxCacheSize;
        this.patternCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Stores a successful generation pattern.
     * 
     * @param schemaHash Hash of the schema
     * @param domain Domain context
     * @param example Generated example data
     */
    public void storePattern(String schemaHash, String domain, String example) {
        if (!enabled) {
            return;
        }
        
        try {
            String key = buildKey(schemaHash, domain);
            PatternEntry entry = new PatternEntry(example, System.currentTimeMillis());
            
            patternCache.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
            
            // Limit cache size per key
            List<PatternEntry> entries = patternCache.get(key);
            if (entries.size() > maxCacheSize) {
                entries.remove(0); // Remove oldest entry
            }
            
            logger.debug("Stored pattern for key: {}", key);
        } catch (Exception e) {
            logger.warn("Failed to store pattern", e);
        }
    }
    
    /**
     * Retrieves relevant patterns for a schema and domain.
     * 
     * @param schemaHash Hash of the schema
     * @param domain Domain context
     * @param limit Maximum number of patterns to return
     * @return List of example patterns
     */
    public List<String> getPatterns(String schemaHash, String domain, int limit) {
        if (!enabled) {
            return Collections.emptyList();
        }
        
        String key = buildKey(schemaHash, domain);
        List<PatternEntry> entries = patternCache.get(key);
        
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Return most recent patterns
        List<String> patterns = new ArrayList<>();
        int start = Math.max(0, entries.size() - limit);
        for (int i = entries.size() - 1; i >= start && patterns.size() < limit; i--) {
            patterns.add(entries.get(i).example);
        }
        
        return patterns;
    }
    
    /**
     * Gets patterns for similar schemas (fuzzy matching).
     * 
     * @param schemaHash Hash of the schema
     * @param domain Domain context
     * @param limit Maximum number of patterns to return
     * @return List of example patterns
     */
    public List<String> getSimilarPatterns(String schemaHash, String domain, int limit) {
        if (!enabled) {
            return Collections.emptyList();
        }
        
        List<String> allPatterns = new ArrayList<>();
        
        // Get patterns for exact match
        allPatterns.addAll(getPatterns(schemaHash, domain, limit));
        
        // Get patterns for same domain but different schema
        if (allPatterns.size() < limit) {
            for (Map.Entry<String, List<PatternEntry>> entry : patternCache.entrySet()) {
                if (entry.getKey().endsWith(":" + domain) && !entry.getKey().equals(buildKey(schemaHash, domain))) {
                    for (PatternEntry pattern : entry.getValue()) {
                        if (allPatterns.size() >= limit) break;
                        allPatterns.add(pattern.example);
                    }
                }
            }
        }
        
        return allPatterns.subList(0, Math.min(allPatterns.size(), limit));
    }
    
    /**
     * Clears all cached patterns.
     */
    public void clear() {
        patternCache.clear();
        logger.info("Pattern cache cleared");
    }
    
    /**
     * Clears patterns for a specific domain.
     * 
     * @param domain Domain to clear
     */
    public void clearDomain(String domain) {
        patternCache.entrySet().removeIf(entry -> entry.getKey().endsWith(":" + domain));
        logger.info("Cleared patterns for domain: {}", domain);
    }
    
    /**
     * Gets cache statistics.
     * 
     * @return Cache statistics
     */
    public CacheStats getStats() {
        int totalPatterns = patternCache.values().stream()
            .mapToInt(List::size)
            .sum();
        
        return new CacheStats(patternCache.size(), totalPatterns, enabled);
    }
    
    private String buildKey(String schemaHash, String domain) {
        return (schemaHash != null ? schemaHash : "default") + ":" + (domain != null ? domain : "default");
    }
    
    private static class PatternEntry {
        final String example;
        final long timestamp;
        
        PatternEntry(String example, long timestamp) {
            this.example = example;
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Statistics about the pattern cache.
     */
    public static class CacheStats {
        private final int keyCount;
        private final int totalPatterns;
        private final boolean enabled;
        
        /**
         * Creates cache statistics.
         * 
         * @param keyCount Number of cache keys
         * @param totalPatterns Total number of cached patterns
         * @param enabled Whether caching is enabled
         */
        CacheStats(int keyCount, int totalPatterns, boolean enabled) {
            this.keyCount = keyCount;
            this.totalPatterns = totalPatterns;
            this.enabled = enabled;
        }
        
        /**
         * Gets the number of cache keys.
         * 
         * @return Number of cache keys
         */
        public int getKeyCount() {
            return keyCount;
        }
        
        /**
         * Gets the total number of cached patterns.
         * 
         * @return Total number of patterns
         */
        public int getTotalPatterns() {
            return totalPatterns;
        }
        
        /**
         * Checks if caching is enabled.
         * 
         * @return true if caching is enabled
         */
        public boolean isEnabled() {
            return enabled;
        }
    }
}

