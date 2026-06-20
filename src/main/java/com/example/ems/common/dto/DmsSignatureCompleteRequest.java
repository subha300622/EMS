package com.example.ems.common.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

public class DmsSignatureCompleteRequest {

    @NotBlank(message = "Status is required (SIGNED or DECLINED)")
    @Schema(example = "ACTIVE")
    private String status;

    @Schema(example = "Excellent progress")
    private String comments;

    public DmsSignatureCompleteRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
