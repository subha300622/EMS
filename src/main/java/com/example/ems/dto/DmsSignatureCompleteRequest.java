package com.example.ems.dto;

import jakarta.validation.constraints.NotBlank;

public class DmsSignatureCompleteRequest {

    @NotBlank(message = "Status is required (SIGNED or DECLINED)")
    private String status;

    private String comments;

    public DmsSignatureCompleteRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
