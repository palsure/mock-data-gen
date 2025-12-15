package com.aimockdata.gen.format;

import com.aimockdata.gen.schema.Schema;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Formatter for JSON output format.
 */
public class JsonFormatter implements DataFormatter {
    private static final Gson gson = new Gson();
    
    @Override
    public String format(String aiGeneratedData, Schema schema, int count) {
        try {
            // Parse and pretty-print JSON
            JsonElement jsonElement = JsonParser.parseString(aiGeneratedData);
            return gson.toJson(jsonElement);
        } catch (Exception e) {
            // If parsing fails, return as-is
            return aiGeneratedData;
        }
    }
}

