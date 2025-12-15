package com.aimockdata.gen;

import java.util.Map;

/**
 * Request object for mock data generation.
 */
public class MockDataRequest {
    private final String prompt;
    private final String schema;
    private final OutputFormat outputFormat;
    private final int count;
    private final Map<String, Object> options;
    
    private MockDataRequest(Builder builder) {
        this.prompt = builder.prompt;
        this.schema = builder.schema;
        this.outputFormat = builder.outputFormat != null ? builder.outputFormat : OutputFormat.JSON;
        this.count = builder.count > 0 ? builder.count : 1;
        this.options = builder.options;
    }
    
    /**
     * Gets the prompt describing what data to generate.
     * 
     * @return User prompt
     */
    public String getPrompt() {
        return prompt;
    }
    
    /**
     * Gets the schema definition (JSON string).
     * 
     * @return Schema definition
     */
    public String getSchema() {
        return schema;
    }
    
    /**
     * Gets the desired output format.
     * 
     * @return Output format
     */
    public OutputFormat getOutputFormat() {
        return outputFormat;
    }
    
    /**
     * Gets the number of records to generate.
     * 
     * @return Record count
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Gets additional options for data generation.
     * 
     * @return Options map
     */
    public Map<String, Object> getOptions() {
        return options;
    }
    
    /**
     * Builder for creating MockDataRequest instances.
     */
    public static class Builder {
        private String prompt;
        private String schema;
        private OutputFormat outputFormat;
        private int count = 1;
        private Map<String, Object> options;
        
        /**
         * Sets the prompt describing what data to generate.
         * 
         * @param prompt User prompt
         * @return Builder instance
         */
        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }
        
        /**
         * Sets the schema definition (JSON string).
         * 
         * @param schema Schema definition
         * @return Builder instance
         */
        public Builder schema(String schema) {
            this.schema = schema;
            return this;
        }
        
        /**
         * Sets the desired output format.
         * 
         * @param outputFormat Output format
         * @return Builder instance
         */
        public Builder outputFormat(OutputFormat outputFormat) {
            this.outputFormat = outputFormat;
            return this;
        }
        
        /**
         * Sets the number of records to generate.
         * 
         * @param count Record count (default: 1)
         * @return Builder instance
         */
        public Builder count(int count) {
            this.count = count;
            return this;
        }
        
        /**
         * Sets additional options for data generation.
         * 
         * @param options Options map
         * @return Builder instance
         */
        public Builder options(Map<String, Object> options) {
            this.options = options;
            return this;
        }
        
        /**
         * Builds the MockDataRequest instance.
         * 
         * @return MockDataRequest instance
         * @throws IllegalArgumentException if prompt or schema is missing
         */
        public MockDataRequest build() {
            if (prompt == null || prompt.trim().isEmpty()) {
                throw new IllegalArgumentException("Prompt is required");
            }
            if (schema == null || schema.trim().isEmpty()) {
                throw new IllegalArgumentException("Schema is required");
            }
            return new MockDataRequest(this);
        }
    }
}

