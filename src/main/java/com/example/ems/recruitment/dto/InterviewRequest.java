package com.example.ems.recruitment.dto;
import io.swagger.v3.oas.annotations.media.Schema;




import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class InterviewRequest {

    @NotNull(message = "Candidate ID is required")
    @Schema(example = "1")
    private Long candidateId;

    @NotBlank(message = "Interviewer name is required")
    @Schema(example = "string")
    private String interviewerName;

    @NotNull(message = "Interview date is required")
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime interviewDate;

    @Schema(example = "string")
    private String type; // TECHNICAL, HR, MANAGERIAL

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }

    public LocalDateTime getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDateTime interviewDate) { this.interviewDate = interviewDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
