package com.aimockdata.gen.format;

import com.aimockdata.gen.schema.Schema;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Formatter for CSV output format.
 */
public class CsvFormatter implements DataFormatter {
    
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
            
            // Extract headers from first record
            JsonObject firstRecord = records.get(0);
            List<String> headers = new ArrayList<>(firstRecord.keySet());
            
            StringBuilder csv = new StringBuilder();
            
            // Write header
            csv.append(String.join(",", headers)).append("\n");
            
            // Write data rows
            for (JsonObject record : records) {
                List<String> values = new ArrayList<>();
                for (String header : headers) {
                    JsonElement value = record.get(header);
                    String strValue = value != null ? escapeCsvValue(value.getAsString()) : "";
                    values.add(strValue);
                }
                csv.append(String.join(",", values)).append("\n");
            }
            
            return csv.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to CSV: " + e.getMessage(), e);
        }
    }
    
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes and wrap in quotes if contains comma, newline, or quote
        if (value.contains(",") || value.contains("\n") || value.contains("\"") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

