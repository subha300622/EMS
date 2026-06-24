package com.example.ems.leave.dto;

public class ApplyLeaveResponseDto {
    private Long id;
    private String status;
    private Long approverId;
    private String approverName;
    private String message;

    public ApplyLeaveResponseDto() {}

    public ApplyLeaveResponseDto(Long id, String status, Long approverId, String approverName, String message) {
        this.id = id;
        this.status = status;
        this.approverId = approverId;
        this.approverName = approverName;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }

    public String getApproverName() { return approverName; }
    public void setApproverName(String approverName) { this.approverName = approverName; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
