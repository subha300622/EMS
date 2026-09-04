package com.example.ems.recruitment.dto;

import com.example.ems.recruitment.entity.Interview;
import com.example.ems.recruitment.entity.InterviewRecommendation;
import com.example.ems.recruitment.entity.InterviewStatus;
import com.example.ems.recruitment.entity.InterviewType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class InterviewResponse {

    private Long id;
    private Long organizationId;
    private Long applicationId;
    private String candidateName;
    private String candidateEmail;
    private String jobTitle;
    private Long interviewerId;
    private String interviewerName;
    private InterviewType interviewType;
    private LocalDate scheduledDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String meetingLink;
    private InterviewStatus status;
    private Integer technicalRating;
    private Integer communicationRating;
    private Integer overallRating;
    private InterviewRecommendation recommendation;
    private String comments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InterviewResponse() {}

    public InterviewResponse(Interview interview) {
        this.id = interview.getId();
        this.organizationId = interview.getOrganizationId();
        if (interview.getApplication() != null) {
            this.applicationId = interview.getApplication().getId();
            if (interview.getApplication().getCandidate() != null) {
                this.candidateName = interview.getApplication().getCandidate().getFullName();
                this.candidateEmail = interview.getApplication().getCandidate().getEmail();
            }
            if (interview.getApplication().getJob() != null) {
                this.jobTitle = interview.getApplication().getJob().getTitle();
            }
        }
        if (interview.getInterviewer() != null) {
            this.interviewerId = interview.getInterviewer().getId();
            this.interviewerName = interview.getInterviewer().getFirstName() + " " + interview.getInterviewer().getLastName();
        } else {
            this.interviewerName = interview.getInterviewerName();
        }
        this.interviewType = interview.getInterviewType();
        this.scheduledDate = interview.getScheduledDate();
        this.startTime = interview.getStartTime();
        this.endTime = interview.getEndTime();
        this.meetingLink = interview.getMeetingLink();
        this.status = interview.getStatus();
        this.technicalRating = interview.getTechnicalRating();
        this.communicationRating = interview.getCommunicationRating();
        this.overallRating = interview.getOverallRating();
        this.recommendation = interview.getRecommendation();
        this.comments = interview.getComments();
        this.createdAt = interview.getCreatedAt();
        this.updatedAt = interview.getUpdatedAt();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getCandidateName() { return candidateName; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }

    public String getCandidateEmail() { return candidateEmail; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Long getInterviewerId() { return interviewerId; }
    public void setInterviewerId(Long interviewerId) { this.interviewerId = interviewerId; }

    public String getInterviewerName() { return interviewerName; }
    public void setInterviewerName(String interviewerName) { this.interviewerName = interviewerName; }

    public InterviewType getInterviewType() { return interviewType; }
    public void setInterviewType(InterviewType interviewType) { this.interviewType = interviewType; }

    public LocalDate getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(LocalDate scheduledDate) { this.scheduledDate = scheduledDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getMeetingLink() { return meetingLink; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }

    public InterviewStatus getStatus() { return status; }
    public void setStatus(InterviewStatus status) { this.status = status; }

    public Integer getTechnicalRating() { return technicalRating; }
    public void setTechnicalRating(Integer technicalRating) { this.technicalRating = technicalRating; }

    public Integer getCommunicationRating() { return communicationRating; }
    public void setCommunicationRating(Integer communicationRating) { this.communicationRating = communicationRating; }

    public Integer getOverallRating() { return overallRating; }
    public void setOverallRating(Integer overallRating) { this.overallRating = overallRating; }

    public InterviewRecommendation getRecommendation() { return recommendation; }
    public void setRecommendation(InterviewRecommendation recommendation) { this.recommendation = recommendation; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
