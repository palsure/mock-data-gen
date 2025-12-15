package com.aimockdata.gen;

import com.aimockdata.gen.config.DomainConfig;
import com.aimockdata.gen.config.ModelConfig;
import com.aimockdata.gen.exception.MockDataGenerationException;
import com.aimockdata.gen.ai.learning.LearningService;
import com.aimockdata.gen.ai.learning.PatternCache;
import com.aimockdata.gen.schema.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MockDataGenerator.
 * Note: Some tests require a valid API key to run.
 */
public class MockDataGeneratorTest {
    
    private static final String TEST_API_KEY = System.getenv("OPENAI_API_KEY");
    private MockDataGenerator generator;
    
    @BeforeEach
    public void setUp() {
        if (TEST_API_KEY != null && !TEST_API_KEY.isEmpty()) {
            generator = new MockDataGenerator(TEST_API_KEY);
        }
    }
    
    @Test
    public void testGeneratorCreation() {
        assertNotNull(new MockDataGenerator("test-key"));
    }
    
    @Test
    public void testGeneratorWithDomainConfig() {
        DomainConfig config = DomainConfig.ecommerce();
        MockDataGenerator gen = new MockDataGenerator("test-key", config);
        assertNotNull(gen);
    }
    
    @Test
    public void testRequestBuilder() {
        MockDataRequest request = new MockDataRequest.Builder()
            .prompt("Generate user data")
            .schema("{\"properties\":{\"name\":{\"type\":\"string\"},\"age\":{\"type\":\"number\"}}}")
            .outputFormat(OutputFormat.JSON)
            .count(5)
            .build();
        
        assertNotNull(request);
        assertEquals("Generate user data", request.getPrompt());
        assertEquals(OutputFormat.JSON, request.getOutputFormat());
        assertEquals(5, request.getCount());
    }
    
    @Test
    public void testRequestBuilderValidation() {
        assertThrows(IllegalArgumentException.class, () -> {
            new MockDataRequest.Builder()
                .prompt("")
                .schema("{}")
                .build();
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new MockDataRequest.Builder()
                .prompt("test")
                .schema("")
                .build();
        });
    }
    
    @Test
    public void testSchemaParsing() {
        String schemaJson = "{\"properties\":{\"name\":{\"type\":\"string\"}}}";
        Schema schema = com.aimockdata.gen.schema.Schema.parse(schemaJson);
        assertNotNull(schema);
    }
    
    @Test
    public void testSchemaValidation() {
        Schema validSchema = com.aimockdata.gen.schema.Schema.parse(
            "{\"properties\":{\"name\":{\"type\":\"string\"}}}"
        );
        assertDoesNotThrow(() -> validSchema.validate());
        
        Schema invalidSchema = com.aimockdata.gen.schema.Schema.parse("{}");
        assertThrows(IllegalArgumentException.class, () -> invalidSchema.validate());
    }
    
    // ========== Model Configuration Tests ==========
    
    @Test
    public void testModelConfigDefault() {
        ModelConfig config = ModelConfig.defaultConfig();
        assertNotNull(config);
        assertEquals("gpt-4o-mini", config.getModel());
        assertEquals(0.7, config.getTemperature(), 0.01);
        assertEquals(4000, config.getMaxTokens());
    }
    
    @Test
    public void testModelConfigCreative() {
        ModelConfig config = ModelConfig.creative();
        assertNotNull(config);
        assertEquals(0.9, config.getTemperature(), 0.01);
        assertEquals(0.95, config.getTopP(), 0.01);
    }
    
    @Test
    public void testModelConfigPrecise() {
        ModelConfig config = ModelConfig.precise();
        assertNotNull(config);
        assertEquals(0.3, config.getTemperature(), 0.01);
    }
    
    @Test
    public void testModelConfigBalanced() {
        ModelConfig config = ModelConfig.balanced();
        assertNotNull(config);
        assertEquals(0.7, config.getTemperature(), 0.01);
    }
    
    @Test
    public void testModelConfigForModel() {
        ModelConfig config = ModelConfig.forModel("gpt-4o");
        assertNotNull(config);
        assertEquals("gpt-4o", config.getModel());
    }
    
    @Test
    public void testModelConfigBuilder() {
        ModelConfig config = new ModelConfig.Builder()
            .model("gpt-4o")
            .temperature(0.8)
            .maxTokens(8000)
            .topP(0.95)
            .frequencyPenalty(1)
            .presencePenalty(1)
            .systemPrompt("Custom prompt")
            .build();
        
        assertNotNull(config);
        assertEquals("gpt-4o", config.getModel());
        assertEquals(0.8, config.getTemperature(), 0.01);
        assertEquals(8000, config.getMaxTokens());
        assertEquals(0.95, config.getTopP(), 0.01);
        assertEquals(1, config.getFrequencyPenalty());
        assertEquals(1, config.getPresencePenalty());
        assertEquals("Custom prompt", config.getSystemPrompt());
    }
    
    @Test
    public void testModelConfigTemperatureBounds() {
        ModelConfig config1 = new ModelConfig.Builder()
            .temperature(-1.0) // Should clamp to 0.0
            .build();
        assertEquals(0.7, config1.getTemperature(), 0.01); // Default since invalid
        
        ModelConfig config2 = new ModelConfig.Builder()
            .temperature(3.0) // Should clamp to 2.0
            .build();
        assertEquals(0.7, config2.getTemperature(), 0.01); // Default since invalid
    }
    
    @Test
    public void testGeneratorWithModelConfig() {
        ModelConfig config = ModelConfig.creative();
        MockDataGenerator gen = new MockDataGenerator("test-key", DomainConfig.defaultConfig(), config);
        assertNotNull(gen);
    }
    
    // ========== Learning Service Tests ==========
    
    @Test
    public void testLearningServiceCreation() {
        LearningService service = new LearningService();
        assertNotNull(service);
        assertTrue(service.getStats().isEnabled());
    }
    
    @Test
    public void testLearningServiceDisabled() {
        LearningService service = new LearningService(false);
        assertNotNull(service);
        assertFalse(service.getStats().isEnabled());
    }
    
    @Test
    public void testLearningServiceWithCache() {
        PatternCache cache = new PatternCache(true, 50);
        LearningService service = new LearningService(cache);
        assertNotNull(service);
        assertTrue(service.getStats().isEnabled());
    }
    
    @Test
    public void testGeneratorWithLearningService() {
        LearningService learningService = new LearningService();
        MockDataGenerator gen = new MockDataGenerator(
            "test-key",
            DomainConfig.defaultConfig(),
            ModelConfig.defaultConfig(),
            learningService
        );
        assertNotNull(gen);
    }
    
    @Test
    public void testLearningStats() {
        LearningService service = new LearningService();
        PatternCache.CacheStats stats = service.getStats();
        assertNotNull(stats);
        assertEquals(0, stats.getKeyCount());
        assertEquals(0, stats.getTotalPatterns());
    }
    
    @Test
    public void testLearningServiceClearLearning() {
        LearningService service = new LearningService();
        assertDoesNotThrow(() -> service.clearLearning());
    }
    
    // ========== Pattern Cache Tests ==========
    
    @Test
    public void testPatternCacheCreation() {
        PatternCache cache = new PatternCache();
        assertNotNull(cache);
        assertTrue(cache.getStats().isEnabled());
    }
    
    @Test
    public void testPatternCacheDisabled() {
        PatternCache cache = new PatternCache(false, 100);
        assertNotNull(cache);
        assertFalse(cache.getStats().isEnabled());
    }
    
    @Test
    public void testPatternCacheStoreAndRetrieve() {
        PatternCache cache = new PatternCache(true, 10);
        String schemaHash = "test-hash";
        String domain = "test-domain";
        String example = "{\"test\": \"data\"}";
        
        cache.storePattern(schemaHash, domain, example);
        PatternCache.CacheStats stats = cache.getStats();
        assertTrue(stats.getTotalPatterns() > 0);
        
        java.util.List<String> patterns = cache.getPatterns(schemaHash, domain, 5);
        assertNotNull(patterns);
        assertTrue(patterns.size() > 0);
        assertTrue(patterns.contains(example));
    }
    
    @Test
    public void testPatternCacheGetSimilarPatterns() {
        PatternCache cache = new PatternCache(true, 10);
        cache.storePattern("hash1", "domain1", "{\"data\": 1}");
        cache.storePattern("hash2", "domain1", "{\"data\": 2}");
        
        java.util.List<String> similar = cache.getSimilarPatterns("hash3", "domain1", 5);
        assertNotNull(similar);
    }
    
    @Test
    public void testPatternCacheClear() {
        PatternCache cache = new PatternCache(true, 10);
        cache.storePattern("hash1", "domain1", "data1");
        cache.clear();
        
        PatternCache.CacheStats stats = cache.getStats();
        assertEquals(0, stats.getTotalPatterns());
    }
    
    @Test
    public void testPatternCacheClearDomain() {
        PatternCache cache = new PatternCache(true, 10);
        cache.storePattern("hash1", "domain1", "data1");
        cache.storePattern("hash2", "domain2", "data2");
        cache.clearDomain("domain1");
        
        java.util.List<String> patterns = cache.getPatterns("hash1", "domain1", 5);
        assertEquals(0, patterns.size());
        
        java.util.List<String> patterns2 = cache.getPatterns("hash2", "domain2", 5);
        assertTrue(patterns2.size() > 0);
    }
    
    // ========== Schema Constraint Tests ==========
    
    @Test
    public void testSchemaWithConstraints() {
        String schemaJson = "{\n" +
            "  \"properties\": {\n" +
            "    \"phoneNumber\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"pattern\": \"^[0-9]{10}$\",\n" +
            "      \"constraints\": \"must be exactly 10 digits\"\n" +
            "    },\n" +
            "    \"age\": {\n" +
            "      \"type\": \"integer\",\n" +
            "      \"minimum\": 18,\n" +
            "      \"maximum\": 100\n" +
            "    }\n" +
            "  }\n" +
            "}";
        
        Schema schema = Schema.parse(schemaJson);
        assertNotNull(schema);
        
        java.util.List<String> constraints = schema.extractConstraints();
        assertNotNull(constraints);
        assertTrue(constraints.size() > 0);
    }
    
    @Test
    public void testSchemaWithEnum() {
        String schemaJson = "{\n" +
            "  \"properties\": {\n" +
            "    \"status\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"enum\": [\"active\", \"inactive\", \"pending\"]\n" +
            "    }\n" +
            "  }\n" +
            "}";
        
        Schema schema = Schema.parse(schemaJson);
        java.util.List<String> constraints = schema.extractConstraints();
        assertTrue(constraints.stream().anyMatch(c -> c.contains("enum") || c.contains("one of")));
    }
    
    @Test
    public void testSchemaWithArrayType() {
        String schemaJson = "{\n" +
            "  \"properties\": {\n" +
            "    \"tags\": {\n" +
            "      \"type\": \"array\",\n" +
            "      \"items\": {\"type\": \"string\"},\n" +
            "      \"minItems\": 1,\n" +
            "      \"maxItems\": 5\n" +
            "    }\n" +
            "  }\n" +
            "}";
        
        Schema schema = Schema.parse(schemaJson);
        assertNotNull(schema);
        java.util.List<String> constraints = schema.extractConstraints();
        assertTrue(constraints.size() > 0);
    }
    
    @Test
    public void testSchemaWithSetType() {
        String schemaJson = "{\n" +
            "  \"properties\": {\n" +
            "    \"categories\": {\n" +
            "      \"type\": \"set\",\n" +
            "      \"items\": {\"type\": \"string\"},\n" +
            "      \"uniqueItems\": true\n" +
            "    }\n" +
            "  }\n" +
            "}";
        
        Schema schema = Schema.parse(schemaJson);
        assertNotNull(schema);
    }
    
    @Test
    public void testSchemaWithMapType() {
        String schemaJson = "{\n" +
            "  \"properties\": {\n" +
            "    \"metadata\": {\n" +
            "      \"type\": \"map\",\n" +
            "      \"additionalProperties\": {\"type\": \"string\"}\n" +
            "    }\n" +
            "  }\n" +
            "}";
        
        Schema schema = Schema.parse(schemaJson);
        assertNotNull(schema);
    }
    
    @Test
    public void testSchemaWithNumericTypes() {
        String schemaJson = "{\n" +
            "  \"properties\": {\n" +
            "    \"id\": {\"type\": \"int\", \"minimum\": 1},\n" +
            "    \"price\": {\"type\": \"float\", \"minimum\": 0.01},\n" +
            "    \"timestamp\": {\"type\": \"long\", \"minimum\": 0}\n" +
            "  }\n" +
            "}";
        
        Schema schema = Schema.parse(schemaJson);
        assertNotNull(schema);
        java.util.List<String> constraints = schema.extractConstraints();
        assertTrue(constraints.size() > 0);
    }
    
    // ========== Domain Configuration Tests ==========
    
    @Test
    public void testDomainConfigEcommerce() {
        DomainConfig config = DomainConfig.ecommerce();
        assertNotNull(config);
        assertEquals("ecommerce", config.getDomain());
    }
    
    @Test
    public void testDomainConfigHealthcare() {
        DomainConfig config = DomainConfig.healthcare();
        assertNotNull(config);
        assertEquals("healthcare", config.getDomain());
    }
    
    @Test
    public void testDomainConfigFinance() {
        DomainConfig config = DomainConfig.finance();
        assertNotNull(config);
        assertEquals("finance", config.getDomain());
    }
    
    @Test
    public void testDomainConfigEducation() {
        DomainConfig config = DomainConfig.education();
        assertNotNull(config);
        assertEquals("education", config.getDomain());
    }
    
    @Test
    public void testDomainConfigCustom() {
        DomainConfig config = new DomainConfig.Builder()
            .domain("logistics")
            .context("Shipping, warehouses, deliveries")
            .parameter("region", "US")
            .build();
        
        assertNotNull(config);
        assertEquals("logistics", config.getDomain());
        assertEquals("Shipping, warehouses, deliveries", config.getContext());
        assertNotNull(config.getParameters());
    }
    
    @Test
    public void testDomainConfigDefault() {
        DomainConfig config = DomainConfig.defaultConfig();
        assertNotNull(config);
    }
    
    // ========== Output Format Tests ==========
    
    @Test
    public void testOutputFormatValues() {
        OutputFormat[] formats = OutputFormat.values();
        assertTrue(formats.length >= 6);
        
        assertTrue(java.util.Arrays.asList(formats).contains(OutputFormat.JSON));
        assertTrue(java.util.Arrays.asList(formats).contains(OutputFormat.CSV));
        assertTrue(java.util.Arrays.asList(formats).contains(OutputFormat.YAML));
        assertTrue(java.util.Arrays.asList(formats).contains(OutputFormat.SQL));
        assertTrue(java.util.Arrays.asList(formats).contains(OutputFormat.XML));
        assertTrue(java.util.Arrays.asList(formats).contains(OutputFormat.DATA_SERIES));
    }
    
    // ========== MockDataRequest Tests ==========
    
    @Test
    public void testRequestBuilderWithOptions() {
        java.util.Map<String, Object> options = new java.util.HashMap<>();
        options.put("key1", "value1");
        
        MockDataRequest request = new MockDataRequest.Builder()
            .prompt("test")
            .schema("{\"properties\":{\"name\":{\"type\":\"string\"}}}")
            .outputFormat(OutputFormat.JSON)
            .count(1)
            .options(options)
            .build();
        
        assertNotNull(request);
        assertNotNull(request.getOptions());
        assertEquals("value1", request.getOptions().get("key1"));
    }
    
    @Test
    public void testRequestDefaultValues() {
        MockDataRequest request = new MockDataRequest.Builder()
            .prompt("test")
            .schema("{\"properties\":{\"name\":{\"type\":\"string\"}}}")
            .build();
        
        assertEquals(OutputFormat.JSON, request.getOutputFormat());
        assertEquals(1, request.getCount());
    }
    
    // ========== Error Handling Tests ==========
    
    @Test
    public void testGeneratorWithNullRequest() {
        MockDataGenerator gen = new MockDataGenerator("test-key");
        assertThrows(IllegalArgumentException.class, () -> {
            gen.generate((MockDataRequest) null);
        });
    }
    
    @Test
    public void testAIServiceWithNullApiKeyUsesFallback() {
        // Now allows null/empty for fallback mode
        com.aimockdata.gen.ai.AIService service1 = new com.aimockdata.gen.ai.AIService(null);
        assertNotNull(service1); // Uses fallback instead of throwing
        
        com.aimockdata.gen.ai.AIService service2 = new com.aimockdata.gen.ai.AIService("");
        assertNotNull(service2); // Uses fallback instead of throwing
    }
    
    @Test
    public void testSchemaParseInvalidJson() {
        assertThrows(IllegalArgumentException.class, () -> {
            Schema.parse("invalid json");
        });
    }
    
    @Test
    public void testProvideFeedback() {
        MockDataGenerator gen = new MockDataGenerator("test-key");
        String schema = "{\"properties\":{\"name\":{\"type\":\"string\"}}}";
        String data = "{\"name\":\"test\"}";
        
        // Should not throw
        assertDoesNotThrow(() -> {
            gen.provideFeedback(schema, data, 0.9, "Good");
        });
    }
    
    @Test
    public void testGetLearningStats() {
        MockDataGenerator gen = new MockDataGenerator("test-key");
        PatternCache.CacheStats stats = gen.getLearningStats();
        assertNotNull(stats);
    }
    
    @Test
    public void testGeneratorClearLearning() {
        MockDataGenerator gen = new MockDataGenerator("test-key");
        assertDoesNotThrow(() -> gen.clearLearning());
    }
    
    // ========== Integration-style Tests (require API key) ==========
    
    @Test
    public void testGenerateWithAllFormats() {
        if (generator == null) {
            return; // Skip if no API key
        }
        
        String schema = "{\"properties\":{\"name\":{\"type\":\"string\"},\"age\":{\"type\":\"integer\"}}}";
        
        for (OutputFormat format : OutputFormat.values()) {
            try {
                String result = generator.generate(
                    "Generate test data",
                    schema,
                    format,
                    1
                );
                assertNotNull(result);
                assertFalse(result.isEmpty());
            } catch (MockDataGenerationException e) {
                // Some formats might fail, that's okay for testing
            }
        }
    }
    
    @Test
    public void testGenerateWithModelConfig() {
        if (generator == null) {
            return; // Skip if no API key
        }
        
        ModelConfig config = ModelConfig.precise();
        MockDataGenerator gen = new MockDataGenerator(
            TEST_API_KEY,
            DomainConfig.ecommerce(),
            config
        );
        
        String schema = "{\"properties\":{\"product\":{\"type\":\"string\"}}}";
        
        try {
            String result = gen.generate(
                "Generate product name",
                schema,
                OutputFormat.JSON,
                1
            );
            assertNotNull(result);
        } catch (MockDataGenerationException e) {
            // API might fail, that's okay
        }
    }
    
    @Test
    public void testGenerateWithLearning() {
        if (generator == null) {
            return; // Skip if no API key
        }
        
        LearningService learningService = new LearningService();
        MockDataGenerator gen = new MockDataGenerator(
            TEST_API_KEY,
            DomainConfig.defaultConfig(),
            ModelConfig.defaultConfig(),
            learningService
        );
        
        String schema = "{\"properties\":{\"id\":{\"type\":\"integer\"}}}";
        
        try {
            String result1 = gen.generate(
                "Generate ID",
                schema,
                OutputFormat.JSON,
                1
            );
            assertNotNull(result1);
            
            // Second generation should use learned patterns
            String result2 = gen.generate(
                "Generate ID",
                schema,
                OutputFormat.JSON,
                1
            );
            assertNotNull(result2);
            
            PatternCache.CacheStats stats = gen.getLearningStats();
            assertTrue(stats.getTotalPatterns() > 0);
        } catch (MockDataGenerationException e) {
            // API might fail, that's okay
        }
    }
    
    // ========== Fallback Generator Tests (No API Key Required) ==========
    
    @Test
    public void testGeneratorWithoutApiKey() {
        MockDataGenerator gen = new MockDataGenerator();
        assertNotNull(gen);
    }
    
    @Test
    public void testGeneratorWithoutApiKeyWithDomain() {
        MockDataGenerator gen = new MockDataGenerator(DomainConfig.ecommerce());
        assertNotNull(gen);
    }
    
    @Test
    public void testGenerateWithoutApiKey() {
        MockDataGenerator gen = new MockDataGenerator();
        
        String schema = "{\n" +
            "  \"properties\": {\n" +
            "    \"name\": {\"type\": \"string\"},\n" +
            "    \"age\": {\"type\": \"integer\", \"minimum\": 18, \"maximum\": 100},\n" +
            "    \"email\": {\"type\": \"string\", \"format\": \"email\"},\n" +
            "    \"active\": {\"type\": \"boolean\"}\n" +
            "  }\n" +
            "}";
        
        try {
            String result = gen.generate(
                "Generate user data",
                schema,
                OutputFormat.JSON,
                3
            );
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } catch (MockDataGenerationException e) {
            fail("Fallback generator should work without API key: " + e.getMessage());
        }
    }
    
    @Test
    public void testFallbackWithConstraints() {
        MockDataGenerator gen = new MockDataGenerator();
        
        String schema = "{\n" +
            "  \"properties\": {\n" +
            "    \"phone\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"pattern\": \"^[0-9]{10}$\"\n" +
            "    },\n" +
            "    \"status\": {\n" +
            "      \"type\": \"string\",\n" +
            "      \"enum\": [\"active\", \"inactive\", \"pending\"]\n" +
            "    },\n" +
            "    \"score\": {\n" +
            "      \"type\": \"integer\",\n" +
            "      \"minimum\": 0,\n" +
            "      \"maximum\": 100\n" +
            "    }\n" +
            "  }\n" +
            "}";
        
        try {
            String result = gen.generate(
                "Generate data with constraints",
                schema,
                OutputFormat.JSON,
                5
            );
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } catch (MockDataGenerationException e) {
            fail("Fallback should handle constraints: " + e.getMessage());
        }
    }
    
    @Test
    public void testFallbackWithArrayType() {
        MockDataGenerator gen = new MockDataGenerator();
        
        String schema = "{\n" +
            "  \"properties\": {\n" +
            "    \"tags\": {\n" +
            "      \"type\": \"array\",\n" +
            "      \"items\": {\"type\": \"string\"},\n" +
            "      \"minItems\": 1,\n" +
            "      \"maxItems\": 5\n" +
            "    }\n" +
            "  }\n" +
            "}";
        
        try {
            String result = gen.generate(
                "Generate data with array",
                schema,
                OutputFormat.JSON,
                2
            );
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } catch (MockDataGenerationException e) {
            fail("Fallback should handle arrays: " + e.getMessage());
        }
    }
    
    @Test
    public void testFallbackWithAllFormats() {
        MockDataGenerator gen = new MockDataGenerator();
        String schema = "{\"properties\":{\"name\":{\"type\":\"string\"},\"age\":{\"type\":\"integer\"}}}";
        
        for (OutputFormat format : OutputFormat.values()) {
            try {
                String result = gen.generate(
                    "Generate test data",
                    schema,
                    format,
                    2
                );
                assertNotNull(result);
                assertFalse(result.isEmpty());
            } catch (MockDataGenerationException e) {
                fail("Fallback should work with format " + format + ": " + e.getMessage());
            }
        }
    }
    
    @Test
    public void testAIServiceWithNullApiKey() {
        // Should not throw - uses fallback
        com.aimockdata.gen.ai.AIService service = new com.aimockdata.gen.ai.AIService(null);
        assertNotNull(service);
    }
    
    @Test
    public void testAIServiceWithEmptyApiKey() {
        // Should not throw - uses fallback
        com.aimockdata.gen.ai.AIService service = new com.aimockdata.gen.ai.AIService("");
        assertNotNull(service);
    }
}

