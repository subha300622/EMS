package com.example.ems.maintenance.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicMaintenanceResponse {

    private boolean maintenance;
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime startAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime endAt;

    public PublicMaintenanceResponse() {
    }

    public PublicMaintenanceResponse(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public PublicMaintenanceResponse(boolean maintenance, String message, OffsetDateTime startAt, OffsetDateTime endAt) {
        this.maintenance = maintenance;
        this.message = message;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
