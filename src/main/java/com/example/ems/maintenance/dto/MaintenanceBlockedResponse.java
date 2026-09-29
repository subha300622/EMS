package com.example.ems.maintenance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MaintenanceBlockedResponse {

    private String code = "MAINTENANCE_MODE";
    private String message = "The EMS platform is currently under maintenance.";
    private String maintenanceMessage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime endAt;

    public MaintenanceBlockedResponse() {
    }

    public MaintenanceBlockedResponse(String message, String maintenanceMessage,
                                    OffsetDateTime startAt, OffsetDateTime endAt) {
        this.code = "MAINTENANCE_MODE";
        if (message != null) {
            this.message = message;
        }
        this.maintenanceMessage = maintenanceMessage;
        this.startAt = startAt;
        this.endAt = endAt;
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

    public String getMaintenanceMessage() {
        return maintenanceMessage;
    }

    public void setMaintenanceMessage(String maintenanceMessage) {
        this.maintenanceMessage = maintenanceMessage;
    }

    public OffsetDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(OffsetDateTime startAt) {
        this.startAt = startAt;
    }

    public OffsetDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(OffsetDateTime endAt) {
        this.endAt = endAt;
    }
}
