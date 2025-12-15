package com.aimockdata.gen.schema;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a data schema for mock data generation.
 * Supports value criteria and constraints for field validation.
 */
public class Schema {
    private static final Logger logger = LoggerFactory.getLogger(Schema.class);
    private static final Gson gson = new Gson();
    
    private final JsonObject schemaDefinition;
    
    private Schema(JsonObject schemaDefinition) {
        this.schemaDefinition = schemaDefinition;
    }
    
    /**
     * Parses a JSON schema string into a Schema object.
     * 
     * @param schemaJson JSON string representing the schema
     * @return Schema object
     */
    public static Schema parse(String schemaJson) {
        try {
            JsonObject json = JsonParser.parseString(schemaJson).getAsJsonObject();
            return new Schema(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid schema JSON: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a Schema from a Map.
     * 
     * @param schemaMap Map representing the schema
     * @return Schema object
     */
    public static Schema fromMap(Map<String, Object> schemaMap) {
        String json = gson.toJson(schemaMap);
        return parse(json);
    }
    
    /**
     * Validates the schema structure.
     * 
     * @throws IllegalArgumentException if schema is invalid
     */
    public void validate() {
        if (schemaDefinition == null) {
            throw new IllegalArgumentException("Schema definition cannot be null");
        }
        
        if (!schemaDefinition.has("properties") && !schemaDefinition.has("fields")) {
            throw new IllegalArgumentException("Schema must contain 'properties' or 'fields'");
        }
    }
    
    /**
     * Gets the underlying JSON schema definition.
     * 
     * @return The schema definition as a JsonObject
     */
    public JsonObject getSchemaDefinition() {
        return schemaDefinition;
    }
    
    /**
     * Converts the schema to a JSON string representation.
     * 
     * @return JSON string representation of the schema
     */
    public String toJson() {
        return gson.toJson(schemaDefinition);
    }
    
    /**
     * Extracts value criteria and constraints from the schema for AI prompt generation.
     * 
     * @return List of constraint descriptions for each field
     */
    public List<String> extractConstraints() {
        List<String> constraints = new ArrayList<>();
        
        JsonObject properties = null;
        if (schemaDefinition.has("properties")) {
            properties = schemaDefinition.getAsJsonObject("properties");
        } else if (schemaDefinition.has("fields")) {
            properties = schemaDefinition.getAsJsonObject("fields");
        }
        
        if (properties == null) {
            return constraints;
        }
        
        for (String fieldName : properties.keySet()) {
            JsonObject fieldDef = properties.getAsJsonObject(fieldName);
            List<String> fieldConstraints = new ArrayList<>();
            
            // Extract type information
            String type = extractType(fieldDef);
            if (type != null && !type.isEmpty()) {
                fieldConstraints.add("type: " + type);
            }
            
            // Type constraints
            if (fieldDef.has("type")) {
                String baseType = fieldDef.get("type").getAsString();
                
                // String constraints
                if ("string".equals(baseType) || "str".equals(baseType)) {
                    if (fieldDef.has("minLength")) {
                        fieldConstraints.add("minimum length: " + fieldDef.get("minLength").getAsInt());
                    }
                    if (fieldDef.has("maxLength")) {
                        fieldConstraints.add("maximum length: " + fieldDef.get("maxLength").getAsInt());
                    }
                    if (fieldDef.has("pattern")) {
                        fieldConstraints.add("pattern: " + fieldDef.get("pattern").getAsString());
                    }
                    if (fieldDef.has("format")) {
                        fieldConstraints.add("format: " + fieldDef.get("format").getAsString());
                    }
                }
                
                // Number constraints - support all numeric types
                if (isNumericType(baseType)) {
                    if (fieldDef.has("minimum")) {
                        fieldConstraints.add("minimum: " + fieldDef.get("minimum").getAsNumber());
                    }
                    if (fieldDef.has("maximum")) {
                        fieldConstraints.add("maximum: " + fieldDef.get("maximum").getAsNumber());
                    }
                    if (fieldDef.has("exclusiveMinimum")) {
                        fieldConstraints.add("exclusive minimum: " + fieldDef.get("exclusiveMinimum").getAsNumber());
                    }
                    if (fieldDef.has("exclusiveMaximum")) {
                        fieldConstraints.add("exclusive maximum: " + fieldDef.get("exclusiveMaximum").getAsNumber());
                    }
                }
                
                // Array/List/Set constraints
                if ("array".equals(baseType) || "list".equals(baseType) || "set".equals(baseType)) {
                    if (fieldDef.has("minItems")) {
                        fieldConstraints.add("minimum items: " + fieldDef.get("minItems").getAsInt());
                    }
                    if (fieldDef.has("maxItems")) {
                        fieldConstraints.add("maximum items: " + fieldDef.get("maxItems").getAsInt());
                    }
                    if (fieldDef.has("items")) {
                        JsonElement itemsElement = fieldDef.get("items");
                        if (itemsElement.isJsonObject()) {
                            JsonObject itemsDef = itemsElement.getAsJsonObject();
                            String itemType = extractType(itemsDef);
                            if (itemType != null) {
                                fieldConstraints.add("element type: " + itemType);
                            }
                        }
                    }
                    if (fieldDef.has("uniqueItems") && fieldDef.get("uniqueItems").getAsBoolean()) {
                        fieldConstraints.add("unique items required");
                    }
                }
                
                // Map/Object constraints
                if ("object".equals(baseType) || "map".equals(baseType)) {
                    if (fieldDef.has("additionalProperties")) {
                        JsonElement addProps = fieldDef.get("additionalProperties");
                        if (addProps.isJsonObject()) {
                            JsonObject addPropsDef = addProps.getAsJsonObject();
                            String valueType = extractType(addPropsDef);
                            if (valueType != null) {
                                fieldConstraints.add("value type: " + valueType);
                            }
                        }
                    }
                    if (fieldDef.has("minProperties")) {
                        fieldConstraints.add("minimum properties: " + fieldDef.get("minProperties").getAsInt());
                    }
                    if (fieldDef.has("maxProperties")) {
                        fieldConstraints.add("maximum properties: " + fieldDef.get("maxProperties").getAsInt());
                    }
                }
            }
            
            // Enum constraints
            if (fieldDef.has("enum")) {
                JsonElement enumElement = fieldDef.get("enum");
                if (enumElement.isJsonArray()) {
                    List<String> enumValues = new ArrayList<>();
                    for (JsonElement val : enumElement.getAsJsonArray()) {
                        enumValues.add(val.getAsString());
                    }
                    fieldConstraints.add("must be one of: " + String.join(", ", enumValues));
                }
            }
            
            // Custom constraints/criteria
            if (fieldDef.has("constraints")) {
                JsonElement constraintsElement = fieldDef.get("constraints");
                if (constraintsElement.isJsonPrimitive()) {
                    fieldConstraints.add(constraintsElement.getAsString());
                } else if (constraintsElement.isJsonObject()) {
                    JsonObject constraintsObj = constraintsElement.getAsJsonObject();
                    for (String key : constraintsObj.keySet()) {
                        fieldConstraints.add(key + ": " + constraintsObj.get(key).getAsString());
                    }
                }
            }
            
            // Custom criteria field (alternative to constraints)
            if (fieldDef.has("criteria")) {
                JsonElement criteriaElement = fieldDef.get("criteria");
                if (criteriaElement.isJsonPrimitive()) {
                    fieldConstraints.add(criteriaElement.getAsString());
                } else if (criteriaElement.isJsonArray()) {
                    for (JsonElement crit : criteriaElement.getAsJsonArray()) {
                        fieldConstraints.add(crit.getAsString());
                    }
                }
            }
            
            // Description as additional guidance
            if (fieldDef.has("description")) {
                String description = fieldDef.get("description").getAsString();
                if (description != null && !description.trim().isEmpty()) {
                    fieldConstraints.add("description: " + description);
                }
            }
            
            // Build constraint string for this field
            if (!fieldConstraints.isEmpty()) {
                constraints.add(fieldName + ": " + String.join(", ", fieldConstraints));
            }
        }
        
        return constraints;
    }
    
    /**
     * Extracts the type information from a field definition, including collection types.
     * 
     * @param fieldDef Field definition JSON object
     * @return Type description string
     */
    private String extractType(JsonObject fieldDef) {
        if (!fieldDef.has("type")) {
            return null;
        }
        
        String baseType = fieldDef.get("type").getAsString();
        StringBuilder typeDesc = new StringBuilder();
        
        // Handle collection types
        if ("array".equals(baseType) || "list".equals(baseType)) {
            typeDesc.append("List");
            if (fieldDef.has("items")) {
                JsonElement itemsElement = fieldDef.get("items");
                if (itemsElement.isJsonObject()) {
                    JsonObject itemsDef = itemsElement.getAsJsonObject();
                    if (itemsDef.has("type")) {
                        String itemType = normalizeType(itemsDef.get("type").getAsString());
                        typeDesc.append("<").append(itemType).append(">");
                    }
                }
            }
        } else if ("set".equals(baseType)) {
            typeDesc.append("Set");
            if (fieldDef.has("items")) {
                JsonElement itemsElement = fieldDef.get("items");
                if (itemsElement.isJsonObject()) {
                    JsonObject itemsDef = itemsElement.getAsJsonObject();
                    if (itemsDef.has("type")) {
                        String itemType = normalizeType(itemsDef.get("type").getAsString());
                        typeDesc.append("<").append(itemType).append(">");
                    }
                }
            }
        } else if ("object".equals(baseType) || "map".equals(baseType)) {
            if (fieldDef.has("additionalProperties")) {
                typeDesc.append("Map<String, ");
                JsonElement addProps = fieldDef.get("additionalProperties");
                if (addProps.isJsonObject()) {
                    JsonObject addPropsDef = addProps.getAsJsonObject();
                    if (addPropsDef.has("type")) {
                        String valueType = normalizeType(addPropsDef.get("type").getAsString());
                        typeDesc.append(valueType).append(">");
                    } else {
                        typeDesc.append("Object>");
                    }
                } else {
                    typeDesc.append("Object>");
                }
            } else {
                typeDesc.append("Map");
            }
        } else {
            typeDesc.append(normalizeType(baseType));
        }
        
        return typeDesc.toString();
    }
    
    /**
     * Normalizes type names to standard Java/JSON types.
     * 
     * @param type Raw type string
     * @return Normalized type name
     */
    private String normalizeType(String type) {
        switch (type.toLowerCase()) {
            case "int":
            case "integer":
                return "int";
            case "float":
            case "single":
                return "float";
            case "double":
                return "double";
            case "long":
                return "long";
            case "short":
                return "short";
            case "byte":
                return "byte";
            case "char":
            case "character":
                return "char";
            case "str":
            case "string":
                return "string";
            case "bool":
            case "boolean":
                return "boolean";
            case "array":
                return "array";
            case "list":
                return "list";
            case "set":
                return "set";
            case "map":
            case "object":
                return "map";
            default:
                return type;
        }
    }
    
    /**
     * Checks if a type is a numeric type.
     * 
     * @param type Type string to check
     * @return true if numeric type
     */
    private boolean isNumericType(String type) {
        String normalized = normalizeType(type);
        return "int".equals(normalized) || 
               "integer".equals(normalized) ||
               "float".equals(normalized) ||
               "double".equals(normalized) ||
               "long".equals(normalized) ||
               "short".equals(normalized) ||
               "byte".equals(normalized) ||
               "number".equals(normalized);
    }
}

