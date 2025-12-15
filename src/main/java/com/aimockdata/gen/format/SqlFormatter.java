package com.aimockdata.gen.format;

import com.aimockdata.gen.schema.Schema;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Formatter for SQL INSERT statements output format.
 */
public class SqlFormatter implements DataFormatter {
    private static final String DEFAULT_TABLE_NAME = "mock_data";
    
    @Override
    public String format(String aiGeneratedData, Schema schema, int count) {
        try {
            JsonElement jsonElement = JsonParser.parseString(aiGeneratedData);
            List<JsonObject> records = new ArrayList<>();
            
            if (jsonElement.isJsonArray()) {
                JsonArray array = jsonElement.getAsJsonArray();
                for (JsonElement element : array) {
                    if (element.isJsonObject()) {
                        records.add(element.getAsJsonObject());
                    }
                }
            } else if (jsonElement.isJsonObject()) {
                records.add(jsonElement.getAsJsonObject());
            }
            
            if (records.isEmpty()) {
                return "";
            }
            
            // Extract table name from schema or use default
            String tableName = DEFAULT_TABLE_NAME;
            if (schema.getSchemaDefinition().has("tableName")) {
                tableName = schema.getSchemaDefinition().get("tableName").getAsString();
            }
            
            // Extract column names from first record
            JsonObject firstRecord = records.get(0);
            List<String> columns = new ArrayList<>(firstRecord.keySet());
            
            StringBuilder sql = new StringBuilder();
            
            // Generate INSERT statements
            for (JsonObject record : records) {
                sql.append("INSERT INTO ").append(tableName).append(" (");
                sql.append(String.join(", ", columns));
                sql.append(") VALUES (");
                
                List<String> values = new ArrayList<>();
                for (String column : columns) {
                    JsonElement value = record.get(column);
                    values.add(formatSqlValue(value));
                }
                sql.append(String.join(", ", values));
                sql.append(");\n");
            }
            
            return sql.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to SQL: " + e.getMessage(), e);
        }
    }
    
    private String formatSqlValue(JsonElement value) {
        if (value == null || value.isJsonNull()) {
            return "NULL";
        }
        
        if (value.isJsonPrimitive()) {
            if (value.getAsJsonPrimitive().isString()) {
                return "'" + escapeSqlString(value.getAsString()) + "'";
            } else if (value.getAsJsonPrimitive().isNumber()) {
                return value.getAsString();
            } else if (value.getAsJsonPrimitive().isBoolean()) {
                return value.getAsBoolean() ? "1" : "0";
            }
        }
        
        // For complex types, convert to JSON string
        return "'" + escapeSqlString(value.toString()) + "'";
    }
    
    private String escapeSqlString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("'", "''");
    }
}

