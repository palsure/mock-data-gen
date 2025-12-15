package com.aimockdata.gen.format;

import com.aimockdata.gen.schema.Schema;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Formatter for XML output format.
 */
public class XmlFormatter implements DataFormatter {
    private static final String DEFAULT_ROOT_ELEMENT = "data";
    private static final String DEFAULT_ITEM_ELEMENT = "item";
    
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
                return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<" + DEFAULT_ROOT_ELEMENT + "/>";
            }
            
            StringBuilder xml = new StringBuilder();
            xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xml.append("<").append(DEFAULT_ROOT_ELEMENT).append(">\n");
            
            for (JsonObject record : records) {
                xml.append("  <").append(DEFAULT_ITEM_ELEMENT).append(">\n");
                for (String key : record.keySet()) {
                    JsonElement value = record.get(key);
                    String tagName = sanitizeXmlTag(key);
                    xml.append("    <").append(tagName).append(">");
                    xml.append(escapeXmlValue(value != null ? value.getAsString() : ""));
                    xml.append("</").append(tagName).append(">\n");
                }
                xml.append("  </").append(DEFAULT_ITEM_ELEMENT).append(">\n");
            }
            
            xml.append("</").append(DEFAULT_ROOT_ELEMENT).append(">");
            return xml.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert to XML: " + e.getMessage(), e);
        }
    }
    
    private String sanitizeXmlTag(String tag) {
        // Replace invalid XML tag characters
        return tag.replaceAll("[^a-zA-Z0-9_]", "_");
    }
    
    private String escapeXmlValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }
}

