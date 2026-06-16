package com.example.ems.offboarding.dto;

public class ExitInterviewScheduleResponse {

    private Long interviewId;
    private String status;
    private String hrRepresentative;

    public ExitInterviewScheduleResponse() {}

    public ExitInterviewScheduleResponse(Long interviewId, String status, String hrRepresentative) {
        this.interviewId = interviewId;
        this.status = status;
        this.hrRepresentative = hrRepresentative;
    }

    public Long getInterviewId() { return interviewId; }
    public void setInterviewId(Long interviewId) { this.interviewId = interviewId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getHrRepresentative() { return hrRepresentative; }
    public void setHrRepresentative(String hrRepresentative) { this.hrRepresentative = hrRepresentative; }
}
