package com.example.ems.security.logging;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_api_payload_logs")
public class PostApiPayloadLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, length = 1024)
    private String uri;

    @Column(columnDefinition = "TEXT")
    private String requestPayload;

    @Column(columnDefinition = "TEXT")
    private String responsePayload;

    @Column(nullable = false)
    private Integer status;

    private String userEmail;

    public PostApiPayloadLog() {}

    public PostApiPayloadLog(LocalDateTime timestamp, String uri, String requestPayload, String responsePayload, Integer status, String userEmail) {
        this.timestamp = timestamp;
        this.uri = uri;
        this.requestPayload = requestPayload;
        this.responsePayload = responsePayload;
        this.status = status;
        this.userEmail = userEmail;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public String getRequestPayload() { return requestPayload; }
    public void setRequestPayload(String requestPayload) { this.requestPayload = requestPayload; }

    public String getResponsePayload() { return responsePayload; }
    public void setResponsePayload(String responsePayload) { this.responsePayload = responsePayload; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
