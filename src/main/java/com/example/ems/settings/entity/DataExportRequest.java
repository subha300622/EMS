package com.example.ems.settings.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "data_export_requests", indexes = {
    @Index(name = "idx_data_export_req_id", columnList = "request_id"),
    @Index(name = "idx_data_export_email", columnList = "user_email")
})
public class DataExportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", unique = true, nullable = false)
    private String requestId;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    private String status = "PROCESSING"; // PROCESSING, COMPLETED, FAILED
    private LocalDateTime requestedAt;

    public DataExportRequest() {
        this.requestedAt = LocalDateTime.now();
    }

    public DataExportRequest(String requestId, String userEmail, String status) {
        this.requestId = requestId;
        this.userEmail = userEmail;
        this.status = status;
        this.requestedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
}
