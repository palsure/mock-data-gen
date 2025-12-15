package com.aimockdata.gen.ai.learning;

import com.aimockdata.gen.config.DomainConfig;
import com.aimockdata.gen.schema.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.util.List;

/**
 * Service for learning from data generation patterns and improving quality.
 * Provides feedback mechanisms and pattern learning capabilities.
 */
public class LearningService {
    private static final Logger logger = LoggerFactory.getLogger(LearningService.class);
    
    private final PatternCache patternCache;
    private final boolean learningEnabled;
    
    /**
     * Creates a new LearningService with learning enabled and default cache size.
     */
    public LearningService() {
        this(true);
    }
    
    /**
     * Creates a new LearningService with specified learning enabled state.
     * 
     * @param learningEnabled Whether learning is enabled
     */
    public LearningService(boolean learningEnabled) {
        this.learningEnabled = learningEnabled;
        this.patternCache = new PatternCache(learningEnabled, 100);
    }
    
    /**
     * Creates a new LearningService with a custom pattern cache.
     * 
     * @param patternCache Custom pattern cache instance
     */
    public LearningService(PatternCache patternCache) {
        this.patternCache = patternCache;
        this.learningEnabled = patternCache.getStats().isEnabled();
    }
    
    /**
     * Learns from a successful generation by storing the pattern.
     * 
     * @param schema The schema used
     * @param domainConfig Domain configuration
     * @param generatedData Successfully generated data
     */
    public void learnFromSuccess(Schema schema, DomainConfig domainConfig, String generatedData) {
        if (!learningEnabled) {
            return;
        }
        
        try {
            String schemaHash = hashSchema(schema);
            String domain = domainConfig != null ? domainConfig.getDomain() : "default";
            patternCache.storePattern(schemaHash, domain, generatedData);
            logger.debug("Learned from successful generation");
        } catch (Exception e) {
            logger.warn("Failed to learn from success", e);
        }
    }
    
    /**
     * Gets learned patterns to improve generation quality.
     * 
     * @param schema The schema to generate for
     * @param domainConfig Domain configuration
     * @param maxExamples Maximum number of examples to retrieve
     * @return List of example patterns
     */
    public List<String> getLearnedPatterns(Schema schema, DomainConfig domainConfig, int maxExamples) {
        if (!learningEnabled) {
            return List.of();
        }
        
        try {
            String schemaHash = hashSchema(schema);
            String domain = domainConfig != null ? domainConfig.getDomain() : "default";
            return patternCache.getSimilarPatterns(schemaHash, domain, maxExamples);
        } catch (Exception e) {
            logger.warn("Failed to retrieve learned patterns", e);
            return List.of();
        }
    }
    
    /**
     * Provides feedback on generated data quality.
     * 
     * @param schema The schema used
     * @param domainConfig Domain configuration
     * @param generatedData Generated data
     * @param qualityScore Quality score (0.0 to 1.0)
     * @param feedback Optional feedback message
     */
    public void provideFeedback(Schema schema, DomainConfig domainConfig, String generatedData, 
                                double qualityScore, String feedback) {
        if (!learningEnabled) {
            return;
        }
        
        // Only learn from high-quality generations
        if (qualityScore >= 0.7) {
            learnFromSuccess(schema, domainConfig, generatedData);
            logger.debug("Learned from feedback with score: {}", qualityScore);
        } else {
            logger.debug("Skipped learning from low-quality generation (score: {})", qualityScore);
        }
    }
    
    /**
     * Clears all learned patterns.
     */
    public void clearLearning() {
        patternCache.clear();
        logger.info("Cleared all learned patterns");
    }
    
    /**
     * Gets learning statistics.
     * 
     * @return Learning statistics
     */
    public PatternCache.CacheStats getStats() {
        return patternCache.getStats();
    }
    
    /**
     * Generates a hash for the schema to use as a cache key.
     * 
     * @param schema Schema to hash
     * @return Hash string
     */
    private String hashSchema(Schema schema) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(schema.toJson().getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            logger.warn("Failed to hash schema", e);
            return "default";
        }
    }
}

