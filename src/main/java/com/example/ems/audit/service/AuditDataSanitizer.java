package com.example.ems.audit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AuditDataSanitizer {

    private static final Logger log = LoggerFactory.getLogger(AuditDataSanitizer.class);
    private static final String REDACTED = "***REDACTED***";

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "pwd", "token", "secret", "otp", "pin", "cvv",
            "creditcard", "cardnumber", "card_number", "apikey", "api_key",
            "authorization", "accesstoken", "access_token", "refreshtoken",
            "refresh_token", "privatekey", "private_key", "secretkey", "secret_key"
    );

    private final ObjectMapper objectMapper;

    public AuditDataSanitizer() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public AuditDataSanitizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public String sanitizeToJson(Object data) {
        if (data == null) {
            return null;
        }

        try {
            JsonNode rootNode;
            if (data instanceof String str) {
                String trimmed = str.trim();
                if ((trimmed.startsWith("{") && trimmed.endsWith("}")) || (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
                    rootNode = objectMapper.readTree(trimmed);
                } else {
                    return trimmed;
                }
            } else if (data instanceof JsonNode jsonNode) {
                rootNode = jsonNode.deepCopy();
            } else {
                rootNode = objectMapper.valueToTree(data);
            }

            sanitizeJsonNode(rootNode);
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            log.warn("Failed to sanitize and serialize audit data to JSON: {}", e.getMessage());
            return String.valueOf(data);
        }
    }

    private void sanitizeJsonNode(JsonNode node) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            List<String> fieldNames = new ArrayList<>();
            objectNode.fieldNames().forEachRemaining(fieldNames::add);

            for (String fieldName : fieldNames) {
                if (isSensitiveKey(fieldName)) {
                    objectNode.set(fieldName, new TextNode(REDACTED));
                } else {
                    sanitizeJsonNode(objectNode.get(fieldName));
                }
            }
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (int i = 0; i < arrayNode.size(); i++) {
                sanitizeJsonNode(arrayNode.get(i));
            }
        }
    }

    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String normalized = key.toLowerCase().replaceAll("[^a-z0-9]", "");
        for (String sensitive : SENSITIVE_KEYS) {
            if (normalized.contains(sensitive.replaceAll("[^a-z0-9]", ""))) {
                return true;
            }
        }
        return false;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
