package com.aimockdata.gen.format;

import com.aimockdata.gen.schema.Schema;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * Formatter for YAML output format.
 */
public class YamlFormatter implements DataFormatter {
    private final Yaml yaml;
    
    /**
     * Creates a new YAML formatter with default formatting options.
     * Configures YAML output with 2-space indentation and block style.
     */
    public YamlFormatter() {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        this.yaml = new Yaml(options);
    }
    
    @Override
    public String format(String aiGeneratedData, Schema schema, int count) {
        try {
            JsonElement jsonElement = JsonParser.parseString(aiGeneratedData);
            Object data = convertJsonToObject(jsonElement);
            return yaml.dump(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to YAML: " + e.getMessage(), e);
        }
    }
    
    private Object convertJsonToObject(JsonElement element) {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        if (element.isJsonObject()) {
            return gson.fromJson(element, Map.class);
        } else if (element.isJsonArray()) {
            return gson.fromJson(element, java.util.List.class);
        } else if (element.isJsonPrimitive()) {
            if (element.getAsJsonPrimitive().isString()) {
                return element.getAsString();
            } else if (element.getAsJsonPrimitive().isNumber()) {
                return element.getAsNumber();
            } else if (element.getAsJsonPrimitive().isBoolean()) {
                return element.getAsBoolean();
            }
        }
        return null;
    }
}

