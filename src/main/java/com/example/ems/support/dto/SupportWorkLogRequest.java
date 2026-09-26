package com.example.ems.support.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class SupportWorkLogRequest {

    @NotNull(message = "startedAt is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][X]")
    private LocalDateTime startedAt;

    @NotNull(message = "endedAt is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss[XXX][X]")
    private LocalDateTime endedAt;

    private String description;

    public SupportWorkLogRequest() {}

    public SupportWorkLogRequest(LocalDateTime startedAt, LocalDateTime endedAt, String description) {
        this.startedAt = startedAt;
        this.endedAt = endedAt;
        this.description = description;
    }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
