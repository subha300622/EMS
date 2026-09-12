package com.example.ems.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.example.ems.security.context.TenantContext;

public class ApiResponse<T> {
    @Schema(description = "Indicates whether the API request succeeded", example = "true")
    private boolean success;

    @Schema(description = "Business or status code", example = "SUCCESS")
    private String code;

    @Schema(description = "Human-readable response message", example = "Operation completed successfully")
    private String message;

    @Schema(description = "ISO-8601 UTC timestamp of response generation", example = "2026-09-09T14:30:00Z")
    private String timestamp;

    @Schema(description = "Unique correlation request ID", example = "REQ-A1B2C3D4")
    private String requestId;

    @Schema(description = "Response data payload")
    private T data;

    @Schema(description = "Tenant Organization Identifier", example = "9645")
    private Long organizationId;

    @Schema(description = "HATEOAS Navigation Links", example = "{}")
    private Map<String, String> links = new HashMap<>();

    @Schema(description = "Response Metadata", example = "{\"version\": \"v1\", \"executionTimeMs\": 12}")
    private Map<String, Object> metadata = new HashMap<>();

    @Schema(description = "Error details for failure responses")
    private ErrorResponse.ErrorDetails error;

    public ApiResponse() {
        this.timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        this.requestId = getCorrelationId();
        this.metadata.put("version", "v1");
        this.metadata.put("executionTimeMs", getExecutionTime());
        Long currentOrgId = TenantContext.getOrganizationId();
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
        this.timestamp = timestamp != null ? timestamp : Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        this.requestId = getCorrelationId();
        this.data = data;
        this.metadata.put("version", "v1");
        this.metadata.put("executionTimeMs", getExecutionTime());
        Long currentOrgId = TenantContext.getOrganizationId();
        if (currentOrgId != null) {
            this.organizationId = currentOrgId;
            this.metadata.put("organizationId", currentOrgId);
        }
        if (!success && (code != null || message != null)) {
            this.error = new ErrorResponse.ErrorDetails(code, message, Collections.emptyList());
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
        String cid = MDC.get("correlationId");
        if (cid == null) {
            return "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (cid.length() > 8) {
            return "REQ-" + cid.substring(0, 8).toUpperCase();
        }
        return "REQ-" + cid.toUpperCase();
    }

    private static long getExecutionTime() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
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

    public static <T> ApiResponse<T> error(String message, String code) {
        return new ApiResponse<>(false, code, message, null, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message, null, null);
    }

    public ErrorResponse.ErrorDetails getError() {
        if (this.error == null && !this.success && (this.code != null || this.message != null)) {
            return new ErrorResponse.ErrorDetails(this.code, this.message, Collections.emptyList());
        }
        return this.error;
    }

    public void setError(ErrorResponse.ErrorDetails error) {
        this.error = error;
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

    public String getErrorCode() {
        return code;
    }

    public void setErrorCode(String errorCode) {
        this.code = errorCode;
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
