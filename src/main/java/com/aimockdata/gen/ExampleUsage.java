package com.aimockdata.gen;

import com.aimockdata.gen.config.DomainConfig;
import com.aimockdata.gen.config.ModelConfig;
import com.aimockdata.gen.exception.MockDataGenerationException;
import com.aimockdata.gen.ai.learning.LearningService;
import com.aimockdata.gen.ai.learning.PatternCache;

/**
 * Example usage of the AI Mock Data Generator.
 * Demonstrates various features and use cases of the library.
 */
public class ExampleUsage {
    
    /**
     * Main method demonstrating various usage examples.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Replace with your actual API key
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Please set OPENAI_API_KEY environment variable");
            return;
        }
        
        // Create generator with default configuration
        MockDataGenerator generator = new MockDataGenerator(apiKey);
        
        // Example 1: Generate JSON data
        try {
            String schema = "{\n" +
                "  \"properties\": {\n" +
                "    \"name\": {\"type\": \"string\"},\n" +
                "    \"email\": {\"type\": \"string\"},\n" +
                "    \"age\": {\"type\": \"number\"},\n" +
                "    \"active\": {\"type\": \"boolean\"}\n" +
                "  }\n" +
                "}";
            
            String jsonData = generator.generate(
                "Generate realistic user profiles",
                schema,
                OutputFormat.JSON,
                3
            );
            
            System.out.println("=== JSON Output ===");
            System.out.println(jsonData);
            System.out.println();
            
        } catch (MockDataGenerationException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Example 2: Generate CSV data with domain configuration
        try {
            MockDataGenerator ecommerceGenerator = new MockDataGenerator(apiKey, DomainConfig.ecommerce());
            
            String productSchema = "{\n" +
                "  \"properties\": {\n" +
                "    \"productId\": {\"type\": \"string\"},\n" +
                "    \"name\": {\"type\": \"string\"},\n" +
                "    \"price\": {\"type\": \"number\"},\n" +
                "    \"category\": {\"type\": \"string\"},\n" +
                "    \"inStock\": {\"type\": \"boolean\"}\n" +
                "  }\n" +
                "}";
            
            String csvData = ecommerceGenerator.generate(
                "Generate e-commerce product data",
                productSchema,
                OutputFormat.CSV,
                5
            );
            
            System.out.println("=== CSV Output ===");
            System.out.println(csvData);
            System.out.println();
            
        } catch (MockDataGenerationException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Example 3: Generate SQL INSERT statements
        try {
            String orderSchema = "{\n" +
                "  \"tableName\": \"orders\",\n" +
                "  \"properties\": {\n" +
                "    \"orderId\": {\"type\": \"string\"},\n" +
                "    \"customerId\": {\"type\": \"string\"},\n" +
                "    \"total\": {\"type\": \"number\"},\n" +
                "    \"status\": {\"type\": \"string\"}\n" +
                "  }\n" +
                "}";
            
            String sqlData = generator.generate(
                "Generate order records",
                orderSchema,
                OutputFormat.SQL,
                3
            );
            
            System.out.println("=== SQL Output ===");
            System.out.println(sqlData);
            System.out.println();
            
        } catch (MockDataGenerationException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Example 4: Generate YAML data
        try {
            String configSchema = "{\n" +
                "  \"properties\": {\n" +
                "    \"serverName\": {\"type\": \"string\"},\n" +
                "    \"port\": {\"type\": \"number\"},\n" +
                "    \"enabled\": {\"type\": \"boolean\"}\n" +
                "  }\n" +
                "}";
            
            String yamlData = generator.generate(
                "Generate server configuration",
                configSchema,
                OutputFormat.YAML,
                2
            );
            
            System.out.println("=== YAML Output ===");
            System.out.println(yamlData);
            
        } catch (MockDataGenerationException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Example 5: Using MockDataRequest builder
        try {
            MockDataRequest request = new MockDataRequest.Builder()
                .prompt("Generate healthcare patient records")
                .schema("{\n" +
                    "  \"properties\": {\n" +
                    "    \"patientId\": {\"type\": \"string\"},\n" +
                    "    \"name\": {\"type\": \"string\"},\n" +
                    "    \"diagnosis\": {\"type\": \"string\"},\n" +
                    "    \"admissionDate\": {\"type\": \"string\"}\n" +
                    "  }\n" +
                    "}")
                .outputFormat(OutputFormat.JSON)
                .count(2)
                .build();
            
            MockDataGenerator healthcareGenerator = new MockDataGenerator(apiKey, DomainConfig.healthcare());
            String healthcareData = healthcareGenerator.generate(request);
            
            System.out.println("=== Healthcare Data (JSON) ===");
            System.out.println(healthcareData);
            
        } catch (MockDataGenerationException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Example 6: Using value criteria and constraints
        try {
            String schemaWithConstraints = "{\n" +
                "  \"properties\": {\n" +
                "    \"accountNumber\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"pattern\": \"^[0-9]{9}$\",\n" +
                "      \"constraints\": \"must be exactly 9 digits\",\n" +
                "      \"description\": \"Bank account number\"\n" +
                "    },\n" +
                "    \"phoneNumber\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"pattern\": \"^\\\\+1-[0-9]{3}-[0-9]{3}-[0-9]{4}$\",\n" +
                "      \"description\": \"US phone number in format +1-XXX-XXX-XXXX\"\n" +
                "    },\n" +
                "    \"age\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"minimum\": 18,\n" +
                "      \"maximum\": 65\n" +
                "    },\n" +
                "    \"email\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"format\": \"email\"\n" +
                "    },\n" +
                "    \"status\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"enum\": [\"active\", \"inactive\", \"pending\"]\n" +
                "    },\n" +
                "    \"salary\": {\n" +
                "      \"type\": \"number\",\n" +
                "      \"minimum\": 30000,\n" +
                "      \"maximum\": 150000\n" +
                "    }\n" +
                "  }\n" +
                "}";
            
            String constrainedData = generator.generate(
                "Generate employee records with strict validation",
                schemaWithConstraints,
                OutputFormat.JSON,
                3
            );
            
            System.out.println("=== Data with Value Constraints ===");
            System.out.println(constrainedData);
            System.out.println();
            
        } catch (MockDataGenerationException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Example 7: Using various data types (array, list, set, map, numeric types)
        try {
            String schemaWithTypes = "{\n" +
                "  \"properties\": {\n" +
                "    \"id\": {\n" +
                "      \"type\": \"int\",\n" +
                "      \"minimum\": 1,\n" +
                "      \"maximum\": 10000\n" +
                "    },\n" +
                "    \"price\": {\n" +
                "      \"type\": \"float\",\n" +
                "      \"minimum\": 0.01,\n" +
                "      \"maximum\": 999.99\n" +
                "    },\n" +
                "    \"tags\": {\n" +
                "      \"type\": \"array\",\n" +
                "      \"items\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"minItems\": 1,\n" +
                "      \"maxItems\": 5\n" +
                "    },\n" +
                "    \"uniqueCategories\": {\n" +
                "      \"type\": \"set\",\n" +
                "      \"items\": {\n" +
                "        \"type\": \"string\"\n" +
                "      },\n" +
                "      \"uniqueItems\": true,\n" +
                "      \"minItems\": 1,\n" +
                "      \"maxItems\": 3\n" +
                "    },\n" +
                "    \"metadata\": {\n" +
                "      \"type\": \"map\",\n" +
                "      \"additionalProperties\": {\n" +
                "        \"type\": \"string\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"scores\": {\n" +
                "      \"type\": \"list\",\n" +
                "      \"items\": {\n" +
                "        \"type\": \"integer\",\n" +
                "        \"minimum\": 0,\n" +
                "        \"maximum\": 100\n" +
                "      },\n" +
                "      \"minItems\": 3,\n" +
                "      \"maxItems\": 5\n" +
                "    }\n" +
                "  }\n" +
                "}";
            
            String typedData = generator.generate(
                "Generate product data with various data types",
                schemaWithTypes,
                OutputFormat.JSON,
                2
            );
            
            System.out.println("=== Data with Various Types (Array, List, Set, Map) ===");
            System.out.println(typedData);
            System.out.println();
            
        } catch (MockDataGenerationException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Example 8: Using Model Configuration
        try {
            // Create generator with custom model configuration
            ModelConfig creativeConfig = new ModelConfig.Builder()
                .model("gpt-4o")
                .temperature(0.9)
                .maxTokens(8000)
                .topP(0.95)
                .build();
            
            MockDataGenerator creativeGenerator = new MockDataGenerator(
                apiKey,
                DomainConfig.ecommerce(),
                creativeConfig
            );
            
            String creativeSchema = "{\n" +
                "  \"properties\": {\n" +
                "    \"productName\": {\"type\": \"string\"},\n" +
                "    \"description\": {\"type\": \"string\", \"minLength\": 50, \"maxLength\": 200}\n" +
                "  }\n" +
                "}";
            
            String creativeData = creativeGenerator.generate(
                "Generate creative product descriptions",
                creativeSchema,
                OutputFormat.JSON,
                3
            );
            
            System.out.println("=== Creative Generation (High Temperature) ===");
            System.out.println(creativeData);
            System.out.println();
            
        } catch (MockDataGenerationException e) {
            System.err.println("Error: " + e.getMessage());
        }
        
        // Example 9: Using Learning Features
        try {
            // Create generator with learning enabled
            PatternCache patternCache = new PatternCache(true, 100);
            LearningService learningService = new LearningService(patternCache);
            MockDataGenerator learningGenerator = new MockDataGenerator(
                apiKey,
                DomainConfig.ecommerce(),
                ModelConfig.balanced(),
                learningService
            );
            
            String schema = "{\n" +
                "  \"properties\": {\n" +
                "    \"productId\": {\"type\": \"string\"},\n" +
                "    \"name\": {\"type\": \"string\"},\n" +
                "    \"price\": {\"type\": \"float\", \"minimum\": 10.0, \"maximum\": 1000.0}\n" +
                "  }\n" +
                "}";
            
            // First generation
            String data1 = learningGenerator.generate(
                "Generate product data",
                schema,
                OutputFormat.JSON,
                3
            );
            
            System.out.println("=== First Generation ===");
            System.out.println(data1);
            
            // Provide feedback to improve learning
            learningGenerator.provideFeedback(schema, data1, 0.9, "Good quality data");
            
            // Second generation (will use learned patterns)
            String data2 = learningGenerator.generate(
                "Generate product data",
                schema,
                OutputFormat.JSON,
                3
            );
            
            System.out.println("=== Second Generation (with learning) ===");
            System.out.println(data2);
            
            // Check learning statistics
            PatternCache.CacheStats stats = learningGenerator.getLearningStats();
            System.out.println("=== Learning Statistics ===");
            System.out.println("Cache keys: " + stats.getKeyCount());
            System.out.println("Total patterns: " + stats.getTotalPatterns());
            System.out.println("Learning enabled: " + stats.isEnabled());
            System.out.println();
            
        } catch (MockDataGenerationException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}

