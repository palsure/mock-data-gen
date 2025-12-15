package com.aimockdata.gen.format;

import com.aimockdata.gen.schema.Schema;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Formatter for time series data output format.
 */
public class DataSeriesFormatter implements DataFormatter {
    
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
            
            // Format as time series data
            // Assume first field is timestamp or time-related
            JsonObject firstRecord = records.get(0);
            List<String> fields = new ArrayList<>(firstRecord.keySet());
            
            StringBuilder series = new StringBuilder();
            series.append("timestamp,").append(String.join(",", fields)).append("\n");
            
            for (int i = 0; i < records.size(); i++) {
                JsonObject record = records.get(i);
                List<String> values = new ArrayList<>();
                
                // Add timestamp (using index as time offset if no timestamp field)
                if (record.has("timestamp")) {
                    values.add(record.get("timestamp").getAsString());
                } else {
                    values.add(String.valueOf(i));
                }
                
                for (String field : fields) {
                    if (!field.equals("timestamp")) {
                        JsonElement value = record.get(field);
                        values.add(value != null ? value.getAsString() : "");
                    }
                }
                
                series.append(String.join(",", values)).append("\n");
            }
            
            return series.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to data series: " + e.getMessage(), e);
        }
    }
}

