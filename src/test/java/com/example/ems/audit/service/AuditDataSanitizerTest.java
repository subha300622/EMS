package com.example.ems.audit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuditDataSanitizerTest {

    private AuditDataSanitizer sanitizer;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        sanitizer = new AuditDataSanitizer();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testSanitizeNull() {
        assertNull(sanitizer.sanitizeToJson(null));
    }

    @Test
    public void testSanitizeSensitiveFieldsInMap() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("username", "john_doe");
        data.put("password", "SecretP@ss123");
        data.put("token", "jwt.token.abc");
        data.put("apiKey", "key-xyz-789");
        data.put("salary", 85000);

        String json = sanitizer.sanitizeToJson(data);
        assertNotNull(json);

        JsonNode node = objectMapper.readTree(json);
        assertEquals("john_doe", node.get("username").asText());
        assertEquals("***REDACTED***", node.get("password").asText());
        assertEquals("***REDACTED***", node.get("token").asText());
        assertEquals("***REDACTED***", node.get("apiKey").asText());
        assertEquals(85000, node.get("salary").asInt());
    }

    @Test
    public void testSanitizeNestedJsonString() throws Exception {
        String jsonInput = "{" +
                "\"user\": {" +
                "  \"name\": \"Alice\"," +
                "  \"credentials\": {" +
                "    \"password\": \"alicePass\"," +
                "    \"otp\": \"123456\"," +
                "    \"pin\": \"9999\"" +
                "  }" +
                "}," +
                "\"creditCard\": \"4111-2222-3333-4444\"," +
                "\"department\": \"Engineering\"" +
                "}";

        String result = sanitizer.sanitizeToJson(jsonInput);
        assertNotNull(result);

        JsonNode root = objectMapper.readTree(result);
        assertEquals("Alice", root.get("user").get("name").asText());
        assertEquals("***REDACTED***", root.get("user").get("credentials").get("password").asText());
        assertEquals("***REDACTED***", root.get("user").get("credentials").get("otp").asText());
        assertEquals("***REDACTED***", root.get("user").get("credentials").get("pin").asText());
        assertEquals("***REDACTED***", root.get("creditCard").asText());
        assertEquals("Engineering", root.get("department").asText());
    }

    @Test
    public void testSanitizeListPayload() throws Exception {
        Map<String, Object> item1 = Map.of("email", "test1@example.com", "refreshToken", "refresh-123");
        Map<String, Object> item2 = Map.of("email", "test2@example.com", "cvv", "123");

        String result = sanitizer.sanitizeToJson(List.of(item1, item2));
        assertNotNull(result);

        JsonNode arrayNode = objectMapper.readTree(result);
        assertTrue(arrayNode.isArray());
        assertEquals("***REDACTED***", arrayNode.get(0).get("refreshToken").asText());
        assertEquals("test1@example.com", arrayNode.get(0).get("email").asText());
        assertEquals("***REDACTED***", arrayNode.get(1).get("cvv").asText());
    }
}
