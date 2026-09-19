package com.example.ems.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class ErrorResponse {
    @Schema(example = "false")
    private boolean success = false;
    @Schema(example = "string")
    private String timestamp;
    private String requestId;
    private ErrorDetails error;

    // Root properties for backward compatibility
    @Schema(example = "string")
    private String message;
    @Schema(example = "EMP101")
    private String errorCode;

    public ErrorResponse() {
        this.timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        this.requestId = getCorrelationId();
    }

    public ErrorResponse(boolean success, String message, String errorCode, String timestamp) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = timestamp != null ? timestamp : Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
        this.requestId = getCorrelationId();
        this.error = new ErrorDetails(errorCode, message, new ArrayList<>());
    }

    private static String getCorrelationId() {
        String cid = org.slf4j.MDC.get("correlationId");
        if (cid == null) {
            return "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (cid.length() > 8) {
            return "REQ-" + cid.substring(0, 8).toUpperCase();
        }
        return "REQ-" + cid.toUpperCase();
    }

    public static ErrorResponse error(String message, String errorCode) {
        return new ErrorResponse(false, message, errorCode, null);
    }

    public static ErrorResponse error(String message, String errorCode, List<ErrorDetails.Detail> details) {
        ErrorResponse resp = new ErrorResponse(false, message, errorCode, null);
        resp.setError(new ErrorDetails(errorCode, message, details));
        return resp;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
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

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }

    // Inner classes for details structure
    public static class ErrorDetails {
        @Schema(description = "Error code identifier", example = "VAL_001")
        private String code;
        @Schema(description = "Error summary message", example = "Validation failed")
        private String message;
        @Schema(description = "Detailed list of field validation errors")
        private List<Detail> details;

        public ErrorDetails() {}

        public ErrorDetails(String code, String message, List<Detail> details) {
            this.code = code;
            this.message = message;
            this.details = details;
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

        public List<Detail> getDetails() {
            return details;
        }

        public void setDetails(List<Detail> details) {
            this.details = details;
        }

        public static class Detail {
            @Schema(description = "Target field name that failed validation", example = "holidayDate")
            private String field;

            @Schema(description = "Rejected field value or explanation", example = "2026-12-25")
            private Object value;

            public Detail() {}

            public Detail(String field, Object value) {
                this.field = field;
                this.value = value;
            }

            public String getField() {
                return field;
            }

            public void setField(String field) {
                this.field = field;
            }

            public Object getValue() {
                return value;
            }

            public void setValue(Object value) {
                this.value = value;
            }
        }
    }
}
