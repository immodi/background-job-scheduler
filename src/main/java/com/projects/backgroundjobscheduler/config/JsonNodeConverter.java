package com.projects.backgroundjobscheduler.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Converter
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode value) {
        try {
            return value == null ? null
                    : objectMapper.writeValueAsString(value);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to serialize JSON", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String value) {
        try {
            return value == null ? null
                    : objectMapper.readTree(value);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to deserialize JSON", e);
        }
    }
}