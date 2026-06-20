package com.example.ems.recruitment.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.ems.recruitment.entity.Interview;

import java.time.LocalDateTime;

public class InterviewResponse {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "string")
    private String interviewerName;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime interviewDate;
    @Schema(example = "string")
    private String type;
    @Schema(example = "ACTIVE")
    private String status;
    @Schema(example = "Excellent progress")
    private String feedback;
    @Schema(example = "1")
    private Integer rating;
    private CandidateResponse candidate;
    private JobResponse job;
    @Schema(example = "2026-06-19T10:00:00")
    private LocalDateTime createdAt;

    public InterviewResponse() {}

    public InterviewResponse(Interview interview) {
        this.id = interview.getId();
        this.interviewerName = interview.getInterviewerName();
        this.interviewDate = interview.getInterviewDate();
        this.type = interview.getType();
        this.status = interview.getStatus();
        this.feedback = interview.getFeedback();
        this.rating = interview.getRating();
        this.createdAt = interview.getCreatedAt();
        if (interview.getCandidate() != null) {
            this.candidate = new CandidateResponse(interview.getCandidate());
        }
        if (interview.getJob() != null) {
            this.job = new JobResponse(interview.getJob());
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }

    public LocalDateTime getInterviewDate() { return interviewDate; }
    public void setInterviewDate(LocalDateTime interviewDate) { this.interviewDate = interviewDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public CandidateResponse getCandidate() { return candidate; }
    public void setCandidate(CandidateResponse candidate) { this.candidate = candidate; }

    public JobResponse getJob() { return job; }
    public void setJob(JobResponse job) { this.job = job; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
