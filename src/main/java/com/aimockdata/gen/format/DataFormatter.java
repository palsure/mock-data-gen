package com.aimockdata.gen.format;

import com.aimockdata.gen.schema.Schema;

/**
 * Interface for formatting generated data into different output formats.
 */
public interface DataFormatter {
    /**
     * Formats the AI-generated data into the target format.
     * 
     * @param aiGeneratedData Raw data from AI service (typically JSON)
     * @param schema The schema definition
     * @param count Number of records
     * @return Formatted data string
     */
    String format(String aiGeneratedData, Schema schema, int count);
}

