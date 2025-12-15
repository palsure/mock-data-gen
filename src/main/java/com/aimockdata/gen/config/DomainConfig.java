package com.aimockdata.gen.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain configuration for customizing data generation based on specific domains.
 */
public class DomainConfig {
    private final String domain;
    private final String context;
    private final Map<String, Object> parameters;
    
    private DomainConfig(Builder builder) {
        this.domain = builder.domain;
        this.context = builder.context;
        this.parameters = builder.parameters != null ? new HashMap<>(builder.parameters) : new HashMap<>();
    }
    
    /**
     * Creates a default domain configuration with no specific domain context.
     * 
     * @return Default domain configuration
     */
    public static DomainConfig defaultConfig() {
        return new Builder().build();
    }
    
    /**
     * Gets the domain name.
     * 
     * @return Domain name (e.g., "ecommerce", "healthcare")
     */
    public String getDomain() {
        return domain;
    }
    
    /**
     * Gets the domain context description.
     * 
     * @return Context description for the domain
     */
    public String getContext() {
        return context;
    }
    
    /**
     * Gets the domain-specific parameters.
     * 
     * @return Map of domain parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }
    
    /**
     * Builder for creating custom domain configurations.
     */
    public static class Builder {
        private String domain;
        private String context;
        private Map<String, Object> parameters;
        
        /**
         * Sets the domain name.
         * 
         * @param domain Domain name
         * @return Builder instance
         */
        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }
        
        /**
         * Sets the domain context description.
         * 
         * @param context Context description
         * @return Builder instance
         */
        public Builder context(String context) {
            this.context = context;
            return this;
        }
        
        /**
         * Adds a domain parameter.
         * 
         * @param key Parameter key
         * @param value Parameter value
         * @return Builder instance
         */
        public Builder parameter(String key, Object value) {
            if (this.parameters == null) {
                this.parameters = new HashMap<>();
            }
            this.parameters.put(key, value);
            return this;
        }
        
        /**
         * Sets domain parameters map.
         * 
         * @param parameters Map of parameters
         * @return Builder instance
         */
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }
        
        /**
         * Builds the DomainConfig instance.
         * 
         * @return DomainConfig instance
         */
        public DomainConfig build() {
            return new DomainConfig(this);
        }
    }
    
    /**
     * Creates a predefined e-commerce domain configuration.
     * 
     * @return E-commerce domain configuration
     */
    public static DomainConfig ecommerce() {
        return new Builder()
            .domain("ecommerce")
            .context("Online retail, product catalogs, shopping carts, orders, customers")
            .build();
    }
    
    /**
     * Creates a predefined healthcare domain configuration.
     * 
     * @return Healthcare domain configuration
     */
    public static DomainConfig healthcare() {
        return new Builder()
            .domain("healthcare")
            .context("Medical records, patient data, appointments, prescriptions, diagnoses")
            .build();
    }
    
    /**
     * Creates a predefined finance domain configuration.
     * 
     * @return Finance domain configuration
     */
    public static DomainConfig finance() {
        return new Builder()
            .domain("finance")
            .context("Banking, transactions, accounts, investments, financial instruments")
            .build();
    }
    
    /**
     * Creates a predefined education domain configuration.
     * 
     * @return Education domain configuration
     */
    public static DomainConfig education() {
        return new Builder()
            .domain("education")
            .context("Students, courses, grades, enrollments, academic records")
            .build();
    }
}

