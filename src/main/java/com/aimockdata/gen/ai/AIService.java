package com.aimockdata.gen.ai;

import com.aimockdata.gen.config.DomainConfig;
import com.aimockdata.gen.config.ModelConfig;
import com.aimockdata.gen.ai.learning.LearningService;
import com.aimockdata.gen.schema.Schema;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Service for interacting with AI APIs to generate mock data.
 * Currently supports OpenAI-compatible APIs.
 */
public class AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private static final String DEFAULT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final Gson gson = new Gson();
    
    private final String apiKey;
    private final String apiUrl;
    private final OkHttpClient httpClient;
    private final ModelConfig modelConfig;
    private final LearningService learningService;
    private final FallbackDataGenerator fallbackGenerator;
    private final boolean useFallback;
    
    /**
     * Creates a new AIService with default API URL and model configuration.
     * 
     * @param apiKey The API key for the AI service (can be null/empty to use fallback generator)
     */
    public AIService(String apiKey) {
        this(apiKey, DEFAULT_API_URL, ModelConfig.defaultConfig(), null);
    }
    
    /**
     * Creates a new AIService with custom API URL and default model configuration.
     * 
     * @param apiKey The API key for the AI service (can be null/empty to use fallback generator)
     * @param apiUrl The API endpoint URL
     */
    public AIService(String apiKey, String apiUrl) {
        this(apiKey, apiUrl, ModelConfig.defaultConfig(), null);
    }
    
    /**
     * Creates a new AIService with model configuration and default API URL.
     * 
     * @param apiKey The API key for the AI service (can be null/empty to use fallback generator)
     * @param modelConfig Model configuration for AI parameters
     */
    public AIService(String apiKey, ModelConfig modelConfig) {
        this(apiKey, DEFAULT_API_URL, modelConfig, null);
    }
    
    /**
     * Creates a new AIService with model configuration and learning service.
     * 
     * @param apiKey The API key for the AI service (can be null/empty to use fallback generator)
     * @param modelConfig Model configuration for AI parameters
     * @param learningService Learning service for pattern learning
     */
    public AIService(String apiKey, ModelConfig modelConfig, LearningService learningService) {
        this(apiKey, DEFAULT_API_URL, modelConfig, learningService);
    }
    
    /**
     * Creates a new AIService with full configuration.
     * 
     * @param apiKey The API key for the AI service (can be null/empty to use fallback generator)
     * @param apiUrl The API endpoint URL
     * @param modelConfig Model configuration for AI parameters
     * @param learningService Learning service for pattern learning
     */
    public AIService(String apiKey, String apiUrl, ModelConfig modelConfig, LearningService learningService) {
        this.useFallback = (apiKey == null || apiKey.trim().isEmpty());
        
        if (!this.useFallback) {
            this.apiKey = apiKey;
            this.apiUrl = apiUrl;
            this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        } else {
            this.apiKey = null;
            this.apiUrl = null;
            this.httpClient = null;
            logger.info("No API key provided. Using fallback data generator.");
        }
        
        this.modelConfig = modelConfig != null ? modelConfig : ModelConfig.defaultConfig();
        this.learningService = learningService;
        this.fallbackGenerator = new FallbackDataGenerator();
    }
    
    /**
     * Generates mock data using AI based on prompt, schema, and domain configuration.
     * 
     * @param prompt User prompt describing the data to generate
     * @param schema Data schema definition
     * @param count Number of records to generate
     * @param domainConfig Domain-specific configuration
     * @return Generated data as JSON string
     * @throws IOException if API call fails
     */
    public String generateData(String prompt, Schema schema, int count, DomainConfig domainConfig) 
            throws IOException {
        // Use fallback generator if no API key is provided
        if (useFallback) {
            logger.debug("Using fallback data generator (no API key)");
            return fallbackGenerator.generateData(schema, count, domainConfig);
        }
        
        // Get learned patterns if learning is enabled
        List<String> learnedPatterns = null;
        if (learningService != null) {
            learnedPatterns = learningService.getLearnedPatterns(schema, domainConfig, 3);
        }
        
        String enhancedPrompt = buildPrompt(prompt, schema, count, domainConfig, learnedPatterns);
        
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", modelConfig.getModel());
        requestBody.addProperty("temperature", modelConfig.getTemperature());
        requestBody.addProperty("max_tokens", modelConfig.getMaxTokens());
        requestBody.addProperty("top_p", modelConfig.getTopP());
        requestBody.addProperty("frequency_penalty", modelConfig.getFrequencyPenalty());
        requestBody.addProperty("presence_penalty", modelConfig.getPresencePenalty());
        
        JsonArray messages = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", modelConfig.getSystemPrompt());
        messages.add(systemMessage);
        
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", enhancedPrompt);
        messages.add(userMessage);
        
        requestBody.add("messages", messages);
        
        Request request = new Request.Builder()
            .url(apiUrl)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
            .build();
        
        logger.debug("Sending request to AI service");
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                throw new IOException("AI API request failed: " + response.code() + " - " + errorBody);
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
            
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                throw new IOException("No choices in AI response");
            }
            
            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            String content = message.get("content").getAsString();
            
            // Clean up the response - remove markdown code blocks if present
            content = content.trim();
            if (content.startsWith("```json")) {
                content = content.substring(7);
            }
            if (content.startsWith("```")) {
                content = content.substring(3);
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.length() - 3);
            }
            content = content.trim();
            
            // Learn from successful generation
            if (learningService != null) {
                learningService.learnFromSuccess(schema, domainConfig, content);
            }
            
            return content;
        }
    }
    
    private String buildPrompt(String prompt, Schema schema, int count, DomainConfig domainConfig, 
                              List<String> learnedPatterns) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Generate ").append(count).append(" realistic mock data record(s) based on the following requirements:\n\n");
        
        if (prompt != null && !prompt.trim().isEmpty()) {
            sb.append("User Request: ").append(prompt).append("\n\n");
        }
        
        if (domainConfig != null && domainConfig.getDomain() != null) {
            sb.append("Domain: ").append(domainConfig.getDomain()).append("\n");
            if (domainConfig.getContext() != null && !domainConfig.getContext().isEmpty()) {
                sb.append("Domain Context: ").append(domainConfig.getContext()).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("Schema Definition:\n");
        sb.append(schema.toJson()).append("\n\n");
        
        // Extract and include value constraints
        List<String> constraints = schema.extractConstraints();
        if (!constraints.isEmpty()) {
            sb.append("Value Constraints and Criteria:\n");
            for (String constraint : constraints) {
                sb.append("- ").append(constraint).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("Requirements:\n");
        sb.append("- Generate exactly ").append(count).append(" record(s)\n");
        sb.append("- Follow the schema structure exactly\n");
        sb.append("- Use realistic, domain-appropriate values\n");
        sb.append("- Strictly adhere to all value constraints and criteria specified above\n");
        sb.append("- Return data as a JSON array if count > 1, or a JSON object if count = 1\n");
        sb.append("- Ensure all data types match the schema\n");
        sb.append("\n");
        sb.append("Type Guidelines:\n");
        sb.append("- For 'int'/'integer': Generate whole numbers\n");
        sb.append("- For 'float': Generate decimal numbers with single precision\n");
        sb.append("- For 'double': Generate decimal numbers with double precision\n");
        sb.append("- For 'long': Generate large whole numbers\n");
        sb.append("- For 'array'/'list': Generate JSON arrays with elements matching the 'items' type\n");
        sb.append("- For 'set': Generate JSON arrays with unique elements matching the 'items' type\n");
        sb.append("- For 'map'/'object': Generate JSON objects with key-value pairs\n");
        sb.append("- For nested collections: Follow the nested type structure recursively\n");
        
        // Include learned patterns if available
        if (learnedPatterns != null && !learnedPatterns.isEmpty()) {
            sb.append("\n");
            sb.append("Example Patterns (learned from previous successful generations):\n");
            for (int i = 0; i < learnedPatterns.size(); i++) {
                sb.append("Example ").append(i + 1).append(":\n");
                sb.append(learnedPatterns.get(i)).append("\n\n");
            }
            sb.append("Use these examples as reference for style and format, but generate new unique data.\n");
        }
        
        return sb.toString();
    }
}

