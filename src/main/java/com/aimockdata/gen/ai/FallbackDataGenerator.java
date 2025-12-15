package com.aimockdata.gen.ai;

import com.aimockdata.gen.config.DomainConfig;
import com.aimockdata.gen.schema.Schema;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Fallback data generator that creates mock data based on schema rules
 * without requiring an AI API. Used when API key is not provided.
 */
public class FallbackDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(FallbackDataGenerator.class);
    private static final Random random = new Random();
    
    private static final String[] FIRST_NAMES = {
        "John", "Jane", "Michael", "Sarah", "David", "Emily", "James", "Emma",
        "Robert", "Olivia", "William", "Sophia", "Richard", "Isabella", "Joseph", "Ava"
    };
    
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Wilson", "Anderson", "Thomas", "Taylor"
    };
    
    private static final String[] EMAIL_DOMAINS = {
        "gmail.com", "yahoo.com", "outlook.com", "hotmail.com", "example.com"
    };
    
    private static final String[] PRODUCT_CATEGORIES = {
        "Electronics", "Clothing", "Books", "Food", "Toys", "Sports", "Home", "Garden"
    };
    
    /**
     * Generates mock data based on schema without AI.
     * 
     * @param schema Data schema definition
     * @param count Number of records to generate
     * @param domainConfig Domain configuration (for context)
     * @return Generated data as JSON string
     */
    public String generateData(Schema schema, int count, DomainConfig domainConfig) {
        try {
            JsonObject schemaDef = schema.getSchemaDefinition();
            JsonObject properties = null;
            
            if (schemaDef.has("properties")) {
                properties = schemaDef.getAsJsonObject("properties");
            } else if (schemaDef.has("fields")) {
                properties = schemaDef.getAsJsonObject("fields");
            }
            
            if (properties == null || properties.size() == 0) {
                return count > 1 ? "[]" : "{}";
            }
            
            if (count == 1) {
                JsonObject record = generateRecord(properties, domainConfig);
                return record.toString();
            } else {
                JsonArray records = new JsonArray();
                for (int i = 0; i < count; i++) {
                    JsonObject record = generateRecord(properties, domainConfig);
                    records.add(record);
                }
                return records.toString();
            }
        } catch (Exception e) {
            logger.error("Error generating fallback data", e);
            throw new RuntimeException("Failed to generate fallback data: " + e.getMessage(), e);
        }
    }
    
    private JsonObject generateRecord(JsonObject properties, DomainConfig domainConfig) {
        JsonObject record = new JsonObject();
        
        for (String fieldName : properties.keySet()) {
            JsonObject fieldDef = properties.getAsJsonObject(fieldName);
            JsonElement value = generateFieldValue(fieldName, fieldDef, domainConfig);
            record.add(fieldName, value);
        }
        
        return record;
    }
    
    private JsonElement generateFieldValue(String fieldName, JsonObject fieldDef, DomainConfig domainConfig) {
        if (!fieldDef.has("type")) {
            return new com.google.gson.JsonPrimitive("unknown");
        }
        
        String type = fieldDef.get("type").getAsString().toLowerCase();
        
        // Check for enum first
        if (fieldDef.has("enum")) {
            JsonArray enumArray = fieldDef.getAsJsonArray("enum");
            if (enumArray.size() > 0) {
                int index = random.nextInt(enumArray.size());
                return enumArray.get(index);
            }
        }
        
        // Generate based on type
        switch (type) {
            case "string":
            case "str":
                return new com.google.gson.JsonPrimitive(generateString(fieldName, fieldDef, domainConfig));
            case "integer":
            case "int":
                return new com.google.gson.JsonPrimitive(generateInteger(fieldDef));
            case "number":
                return new com.google.gson.JsonPrimitive(generateNumber(fieldDef));
            case "float":
            case "single":
                return new com.google.gson.JsonPrimitive(generateFloat(fieldDef));
            case "double":
                return new com.google.gson.JsonPrimitive(generateDouble(fieldDef));
            case "long":
                return new com.google.gson.JsonPrimitive(generateLong(fieldDef));
            case "short":
                return new com.google.gson.JsonPrimitive(generateShort(fieldDef));
            case "byte":
                return new com.google.gson.JsonPrimitive(generateByte(fieldDef));
            case "boolean":
            case "bool":
                return new com.google.gson.JsonPrimitive(random.nextBoolean());
            case "char":
            case "character":
                return new com.google.gson.JsonPrimitive((char) ('A' + random.nextInt(26)));
            case "array":
            case "list":
                return generateArray(fieldDef, domainConfig);
            case "set":
                return generateSet(fieldDef, domainConfig);
            case "object":
            case "map":
                return generateObject(fieldDef, domainConfig);
            default:
                return new com.google.gson.JsonPrimitive("unknown");
        }
    }
    
    private String generateString(String fieldName, JsonObject fieldDef, DomainConfig domainConfig) {
        // Check for format
        if (fieldDef.has("format")) {
            String format = fieldDef.get("format").getAsString();
            switch (format.toLowerCase()) {
                case "email":
                    return generateEmail();
                case "date":
                    return generateDate();
                case "uri":
                case "url":
                    return "https://example.com/resource/" + random.nextInt(1000);
                default:
                    break;
            }
        }
        
        // Check for pattern
        if (fieldDef.has("pattern")) {
            String pattern = fieldDef.get("pattern").getAsString();
            return generateFromPattern(pattern, fieldDef);
        }
        
        // Check for constraints
        if (fieldDef.has("constraints")) {
            String constraints = fieldDef.get("constraints").getAsString();
            if (constraints.contains("digit")) {
                return generateNumericString(fieldDef);
            }
        }
        
        // Generate based on field name
        String lowerName = fieldName.toLowerCase();
        if (lowerName.contains("email")) {
            return generateEmail();
        } else if (lowerName.contains("name") && !lowerName.contains("user")) {
            return generateName();
        } else if (lowerName.contains("phone")) {
            return generatePhoneNumber(fieldDef);
        } else if (lowerName.contains("id")) {
            return generateId(fieldName, fieldDef);
        } else if (lowerName.contains("url") || lowerName.contains("uri")) {
            return "https://example.com/" + random.nextInt(1000);
        } else if (lowerName.contains("date")) {
            return generateDate();
        } else if (lowerName.contains("category")) {
            return PRODUCT_CATEGORIES[random.nextInt(PRODUCT_CATEGORIES.length)];
        } else if (lowerName.contains("description")) {
            return generateDescription(fieldDef);
        }
        
        // Default string generation
        int minLength = fieldDef.has("minLength") ? fieldDef.get("minLength").getAsInt() : 5;
        int maxLength = fieldDef.has("maxLength") ? fieldDef.get("maxLength").getAsInt() : 20;
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        return generateRandomString(length);
    }
    
    private int generateInteger(JsonObject fieldDef) {
        int min = fieldDef.has("minimum") ? fieldDef.get("minimum").getAsInt() : 0;
        int max = fieldDef.has("maximum") ? fieldDef.get("maximum").getAsInt() : 1000;
        if (max <= min) max = min + 1000;
        return min + random.nextInt(max - min + 1);
    }
    
    private double generateNumber(JsonObject fieldDef) {
        double min = fieldDef.has("minimum") ? fieldDef.get("minimum").getAsDouble() : 0.0;
        double max = fieldDef.has("maximum") ? fieldDef.get("maximum").getAsDouble() : 1000.0;
        if (max <= min) max = min + 1000.0;
        return min + random.nextDouble() * (max - min);
    }
    
    private float generateFloat(JsonObject fieldDef) {
        return (float) generateNumber(fieldDef);
    }
    
    private double generateDouble(JsonObject fieldDef) {
        return generateNumber(fieldDef);
    }
    
    private long generateLong(JsonObject fieldDef) {
        long min = fieldDef.has("minimum") ? fieldDef.get("minimum").getAsLong() : 0L;
        long max = fieldDef.has("maximum") ? fieldDef.get("maximum").getAsLong() : 1000000L;
        if (max <= min) max = min + 1000000L;
        return min + (long)(random.nextDouble() * (max - min));
    }
    
    private short generateShort(JsonObject fieldDef) {
        int min = fieldDef.has("minimum") ? fieldDef.get("minimum").getAsInt() : 0;
        int max = fieldDef.has("maximum") ? fieldDef.get("maximum").getAsInt() : 32767;
        if (max > 32767) max = 32767;
        if (max <= min) max = min + 100;
        return (short)(min + random.nextInt(max - min + 1));
    }
    
    private byte generateByte(JsonObject fieldDef) {
        int min = fieldDef.has("minimum") ? fieldDef.get("minimum").getAsInt() : 0;
        int max = fieldDef.has("maximum") ? fieldDef.get("maximum").getAsInt() : 127;
        if (max > 127) max = 127;
        if (max <= min) max = min + 10;
        return (byte)(min + random.nextInt(max - min + 1));
    }
    
    private JsonArray generateArray(JsonObject fieldDef, DomainConfig domainConfig) {
        JsonArray array = new JsonArray();
        
        int minItems = fieldDef.has("minItems") ? fieldDef.get("minItems").getAsInt() : 1;
        int maxItems = fieldDef.has("maxItems") ? fieldDef.get("maxItems").getAsInt() : 5;
        int itemCount = minItems + random.nextInt(maxItems - minItems + 1);
        
        if (fieldDef.has("items")) {
            JsonObject itemsDef = fieldDef.getAsJsonObject("items");
            Set<JsonElement> seen = new HashSet<>();
            boolean uniqueItems = fieldDef.has("uniqueItems") && fieldDef.get("uniqueItems").getAsBoolean();
            
            for (int i = 0; i < itemCount; i++) {
                JsonElement item = generateFieldValue("item", itemsDef, domainConfig);
                if (uniqueItems && seen.contains(item)) {
                    i--; // Retry
                    continue;
                }
                array.add(item);
                if (uniqueItems) {
                    seen.add(item);
                }
            }
        } else {
            // Default string items
            for (int i = 0; i < itemCount; i++) {
                array.add(new com.google.gson.JsonPrimitive("item" + i));
            }
        }
        
        return array;
    }
    
    private JsonArray generateSet(JsonObject fieldDef, DomainConfig domainConfig) {
        // Set is same as array with uniqueItems=true
        JsonObject setDef = fieldDef.deepCopy();
        setDef.addProperty("uniqueItems", true);
        return generateArray(setDef, domainConfig);
    }
    
    private JsonObject generateObject(JsonObject fieldDef, DomainConfig domainConfig) {
        JsonObject obj = new JsonObject();
        
        if (fieldDef.has("properties")) {
            JsonObject props = fieldDef.getAsJsonObject("properties");
            for (String key : props.keySet()) {
                JsonObject propDef = props.getAsJsonObject(key);
                obj.add(key, generateFieldValue(key, propDef, domainConfig));
            }
        } else if (fieldDef.has("additionalProperties")) {
            // Map type
            JsonObject valueDef = fieldDef.getAsJsonObject("additionalProperties");
            int propCount = 2 + random.nextInt(4); // 2-5 properties
            
            for (int i = 0; i < propCount; i++) {
                String key = "key" + i;
                obj.add(key, generateFieldValue(key, valueDef, domainConfig));
            }
        }
        
        return obj;
    }
    
    private String generateEmail() {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)].toLowerCase();
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)].toLowerCase();
        String domain = EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
        return firstName + "." + lastName + random.nextInt(100) + "@" + domain;
    }
    
    private String generateName() {
        return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)] + " " + 
               LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    }
    
    private String generatePhoneNumber(JsonObject fieldDef) {
        if (fieldDef.has("pattern")) {
            return generateFromPattern(fieldDef.get("pattern").getAsString(), fieldDef);
        }
        return String.format("+1-%03d-%03d-%04d", 
            random.nextInt(900) + 100,
            random.nextInt(900) + 100,
            random.nextInt(9000) + 1000);
    }
    
    private String generateId(String fieldName, JsonObject fieldDef) {
        if (fieldDef.has("pattern")) {
            return generateFromPattern(fieldDef.get("pattern").getAsString(), fieldDef);
        }
        String prefix = fieldName.toUpperCase().substring(0, Math.min(4, fieldName.length()));
        return prefix + "-" + String.format("%06d", random.nextInt(1000000));
    }
    
    private String generateDate() {
        int year = 2020 + random.nextInt(5);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28);
        return String.format("%04d-%02d-%02d", year, month, day);
    }
    
    private String generateDescription(JsonObject fieldDef) {
        int minLength = fieldDef.has("minLength") ? fieldDef.get("minLength").getAsInt() : 20;
        int maxLength = fieldDef.has("maxLength") ? fieldDef.get("maxLength").getAsInt() : 200;
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        return generateRandomString(length);
    }
    
    private String generateNumericString(JsonObject fieldDef) {
        int minLength = fieldDef.has("minLength") ? fieldDef.get("minLength").getAsInt() : 1;
        int maxLength = fieldDef.has("maxLength") ? fieldDef.get("maxLength").getAsInt() : 10;
        int length = minLength + random.nextInt(maxLength - minLength + 1);
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    
    private String generateFromPattern(String pattern, JsonObject fieldDef) {
        // Simple pattern matching for common cases
        if (pattern.contains("[0-9]")) {
            int digitCount = countDigitsInPattern(pattern);
            if (digitCount > 0) {
                return generateNumericString(fieldDef);
            }
        }
        
        // Try to extract length from pattern
        java.util.regex.Pattern regex = null;
        try {
            regex = Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            logger.warn("Invalid pattern: " + pattern, e);
        }
        
        // Fallback to random string
        int minLength = fieldDef.has("minLength") ? fieldDef.get("minLength").getAsInt() : 5;
        int maxLength = fieldDef.has("maxLength") ? fieldDef.get("maxLength").getAsInt() : 20;
        return generateRandomString(minLength + random.nextInt(maxLength - minLength + 1));
    }
    
    private int countDigitsInPattern(String pattern) {
        int count = 0;
        for (char c : pattern.toCharArray()) {
            if (Character.isDigit(c)) {
                count++;
            }
        }
        return count;
    }
    
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

