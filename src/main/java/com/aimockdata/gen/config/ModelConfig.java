package com.aimockdata.gen.config;

/**
 * Configuration for AI model parameters.
 * Allows customization of model behavior, temperature, tokens, and other settings.
 */
public class ModelConfig {
    private final String model;
    private final double temperature;
    private final int maxTokens;
    private final double topP;
    private final int frequencyPenalty;
    private final int presencePenalty;
    private final String systemPrompt;
    
    private ModelConfig(Builder builder) {
        this.model = builder.model != null ? builder.model : "gpt-4o-mini";
        this.temperature = builder.temperature >= 0 && builder.temperature <= 2 ? builder.temperature : 0.7;
        this.maxTokens = builder.maxTokens > 0 ? builder.maxTokens : 4000;
        this.topP = builder.topP >= 0 && builder.topP <= 1 ? builder.topP : 1.0;
        this.frequencyPenalty = builder.frequencyPenalty >= -2 && builder.frequencyPenalty <= 2 ? builder.frequencyPenalty : 0;
        this.presencePenalty = builder.presencePenalty >= -2 && builder.presencePenalty <= 2 ? builder.presencePenalty : 0;
        this.systemPrompt = builder.systemPrompt != null ? builder.systemPrompt : 
            "You are a data generation assistant. Generate realistic mock data based on the provided schema. Return only valid JSON data, no explanations.";
    }
    
    public static ModelConfig defaultConfig() {
        return new Builder().build();
    }
    
    public static ModelConfig forModel(String model) {
        return new Builder().model(model).build();
    }
    
    public static ModelConfig creative() {
        return new Builder()
            .temperature(0.9)
            .topP(0.95)
            .build();
    }
    
    public static ModelConfig precise() {
        return new Builder()
            .temperature(0.3)
            .topP(0.8)
            .build();
    }
    
    public static ModelConfig balanced() {
        return new Builder()
            .temperature(0.7)
            .topP(1.0)
            .build();
    }
    
    public String getModel() {
        return model;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public int getMaxTokens() {
        return maxTokens;
    }
    
    public double getTopP() {
        return topP;
    }
    
    public int getFrequencyPenalty() {
        return frequencyPenalty;
    }
    
    public int getPresencePenalty() {
        return presencePenalty;
    }
    
    public String getSystemPrompt() {
        return systemPrompt;
    }
    
    public static class Builder {
        private String model;
        private double temperature = 0.7;
        private int maxTokens = 4000;
        private double topP = 1.0;
        private int frequencyPenalty = 0;
        private int presencePenalty = 0;
        private String systemPrompt;
        
        public Builder model(String model) {
            this.model = model;
            return this;
        }
        
        public Builder temperature(double temperature) {
            this.temperature = temperature;
            return this;
        }
        
        public Builder maxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }
        
        public Builder topP(double topP) {
            this.topP = topP;
            return this;
        }
        
        public Builder frequencyPenalty(int frequencyPenalty) {
            this.frequencyPenalty = frequencyPenalty;
            return this;
        }
        
        public Builder presencePenalty(int presencePenalty) {
            this.presencePenalty = presencePenalty;
            return this;
        }
        
        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }
        
        public ModelConfig build() {
            return new ModelConfig(this);
        }
    }
}

