package com.aimockdata.gen;

import com.aimockdata.gen.config.DomainConfig;
import com.aimockdata.gen.config.ModelConfig;
import com.aimockdata.gen.exception.MockDataGenerationException;
import com.aimockdata.gen.format.*;
import com.aimockdata.gen.schema.Schema;
import com.aimockdata.gen.ai.AIService;
import com.aimockdata.gen.ai.learning.LearningService;
import com.aimockdata.gen.ai.learning.PatternCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Main API class for generating mock data using AI.
 * This is the primary entry point for the mock data generation tool.
 */
public class MockDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(MockDataGenerator.class);
    
    private final AIService aiService;
    private final DomainConfig domainConfig;
    private final LearningService learningService;
    
    /**
     * Creates a new MockDataGenerator instance.
     * 
     * @param apiKey The API key for the AI service (e.g., OpenAI)
     * @param domainConfig Domain configuration for data generation
     */
    public MockDataGenerator(String apiKey, DomainConfig domainConfig) {
        this(apiKey, domainConfig, null, null);
    }
    
    /**
     * Creates a new MockDataGenerator instance with default domain configuration.
     * 
     * @param apiKey The API key for the AI service (can be null/empty to use fallback generator)
     */
    public MockDataGenerator(String apiKey) {
        this(apiKey, null, null, null);
    }
    
    /**
     * Creates a new MockDataGenerator instance without API key (uses fallback generator).
     * 
     * @param domainConfig Domain configuration for data generation
     */
    public MockDataGenerator(DomainConfig domainConfig) {
        this(null, domainConfig, null, null);
    }
    
    /**
     * Creates a new MockDataGenerator instance without API key (uses fallback generator).
     */
    public MockDataGenerator() {
        this(null, null, null, null);
    }
    
    /**
     * Creates a new MockDataGenerator instance with model and learning configuration.
     * 
     * @param apiKey The API key for the AI service
     * @param domainConfig Domain configuration for data generation
     * @param modelConfig Model configuration for AI parameters
     * @param learningService Learning service for pattern learning
     */
    public MockDataGenerator(String apiKey, DomainConfig domainConfig, ModelConfig modelConfig, 
                            LearningService learningService) {
        this.domainConfig = domainConfig != null ? domainConfig : DomainConfig.defaultConfig();
        this.learningService = learningService != null ? learningService : new LearningService();
        this.aiService = new AIService(apiKey, modelConfig, this.learningService);
    }
    
    /**
     * Creates a new MockDataGenerator instance with model configuration.
     * 
     * @param apiKey The API key for the AI service
     * @param domainConfig Domain configuration
     * @param modelConfig Model configuration
     */
    public MockDataGenerator(String apiKey, DomainConfig domainConfig, ModelConfig modelConfig) {
        this(apiKey, domainConfig, modelConfig, null);
    }
    
    /**
     * Generates mock data based on the provided prompt and schema.
     * 
     * @param request The data generation request containing prompt and schema
     * @return Generated mock data in the requested format
     * @throws MockDataGenerationException if generation fails
     */
    public String generate(MockDataRequest request) throws MockDataGenerationException {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        logger.info("Generating mock data for format: {}", request.getOutputFormat());
        
        try {
            // Parse and validate schema
            Schema schema = Schema.parse(request.getSchema());
            schema.validate();
            
            // Generate data using AI
            String aiGeneratedData = aiService.generateData(
                request.getPrompt(),
                schema,
                request.getCount(),
                domainConfig
            );
            
            // Format the data according to the requested output format
            DataFormatter formatter = getFormatter(request.getOutputFormat());
            return formatter.format(aiGeneratedData, schema, request.getCount());
            
        } catch (Exception e) {
            logger.error("Error generating mock data", e);
            throw new MockDataGenerationException("Failed to generate mock data: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generates mock data with a simple prompt and schema string.
     * 
     * @param prompt The prompt describing what data to generate
     * @param schema The schema definition (JSON string)
     * @param outputFormat The desired output format
     * @param count Number of records to generate
     * @return Generated mock data
     * @throws MockDataGenerationException if generation fails
     */
    public String generate(String prompt, String schema, OutputFormat outputFormat, int count) 
            throws MockDataGenerationException {
        MockDataRequest request = new MockDataRequest.Builder()
            .prompt(prompt)
            .schema(schema)
            .outputFormat(outputFormat)
            .count(count)
            .build();
        
        return generate(request);
    }
    
    private DataFormatter getFormatter(OutputFormat format) {
        switch (format) {
            case JSON:
                return new JsonFormatter();
            case CSV:
                return new CsvFormatter();
            case YAML:
                return new YamlFormatter();
            case SQL:
                return new SqlFormatter();
            case DATA_SERIES:
                return new DataSeriesFormatter();
            case XML:
                return new XmlFormatter();
            default:
                throw new IllegalArgumentException("Unsupported output format: " + format);
        }
    }
    
    /**
     * Provides feedback on generated data quality for learning.
     * 
     * @param schema The schema used for generation
     * @param generatedData The generated data
     * @param qualityScore Quality score from 0.0 to 1.0
     * @param feedback Optional feedback message
     */
    public void provideFeedback(String schema, String generatedData, double qualityScore, String feedback) {
        try {
            Schema schemaObj = Schema.parse(schema);
            learningService.provideFeedback(schemaObj, domainConfig, generatedData, qualityScore, feedback);
        } catch (Exception e) {
            logger.warn("Failed to provide feedback", e);
        }
    }
    
    /**
     * Gets learning statistics.
     * 
     * @return Learning statistics
     */
    public PatternCache.CacheStats getLearningStats() {
        return learningService.getStats();
    }
    
    /**
     * Clears all learned patterns.
     */
    public void clearLearning() {
        learningService.clearLearning();
    }
}

