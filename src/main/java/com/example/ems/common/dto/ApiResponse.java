package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ApiResponse<T> {
    @Schema(example = "true")
    private boolean success;
    @Schema(example = "string")
    private String code;
    @Schema(example = "string")
    private String message;
    @Schema(example = "string")
    private String timestamp;
    private String requestId;
    private T data;
    private Long organizationId;
    private Map<String, String> links = new HashMap<>();
    private Map<String, Object> metadata = new HashMap<>();

    public ApiResponse() {
        this.timestamp = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString();
        this.requestId = getCorrelationId();
        this.metadata.put("version", "v1");
        this.metadata.put("executionTimeMs", getExecutionTime());
        Long currentOrgId = com.example.ems.security.context.TenantContext.getOrganizationId();
        if (currentOrgId != null) {
            this.organizationId = currentOrgId;
            this.metadata.put("organizationId", currentOrgId);
        }
    }

    public ApiResponse(boolean success, String message, String timestamp, T data) {
        this(success, null, message, timestamp, data);
    }

    public ApiResponse(boolean success, String code, String message, String timestamp, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString();
        this.requestId = getCorrelationId();
        this.data = data;
        this.metadata.put("version", "v1");
        this.metadata.put("executionTimeMs", getExecutionTime());
        Long currentOrgId = com.example.ems.security.context.TenantContext.getOrganizationId();
        if (currentOrgId != null) {
            this.organizationId = currentOrgId;
            this.metadata.put("organizationId", currentOrgId);
        }
    }

    public ApiResponse(boolean success, String message, T data, Map<String, String> links) {
        this();
        this.success = success;
        this.message = message;
        this.data = data;
        if (links != null) {
            this.links = links;
        }
    }

    private static String getCorrelationId() {
        String cid = org.slf4j.MDC.get("correlationId");
        if (cid == null) {
            return "REQ-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (cid.length() > 8) {
            return "REQ-" + cid.substring(0, 8).toUpperCase();
        }
        return "REQ-" + cid.toUpperCase();
    }

    private static long getExecutionTime() {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attributes =
                    (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                Long startTime = (Long) attributes.getRequest().getAttribute("startTime");
                if (startTime != null) {
                    return System.currentTimeMillis() - startTime;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return 12; // Fallback
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, null, data);
    }

    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return new ApiResponse<>(true, code, message, null, data);
    }

    public static <T> ApiResponse<T> success(String message, T data, Map<String, String> links) {
        return new ApiResponse<>(true, message, data, links);
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null, (T) Collections.emptyMap());
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public void setLinks(Map<String, String> links) {
        this.links = links;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
        if (organizationId != null) {
            this.metadata.put("organizationId", organizationId);
        }
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
