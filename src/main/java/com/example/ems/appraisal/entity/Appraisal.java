package com.example.ems.appraisal.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appraisals")
public class Appraisal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cycle_id")
    private AppraisalCycle cycle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private AppraisalRequest request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppraisalStatus status = AppraisalStatus.CREATED;

    @Column(name = "current_stage_order")
    private Integer currentStageOrder = 1;

    private Double selfRating;

    @Column(columnDefinition = "TEXT")
    private String selfReview;

    private LocalDateTime selfReviewSubmittedAt;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private Employee reviewer;

    private Double managerRating;

    @Column(columnDefinition = "TEXT")
    private String managerReview;

    private LocalDateTime managerReviewSubmittedAt;

    private Double finalRating;

    @Column(name = "performance_category", length = 100)
    private String performanceCategory;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    private boolean attendanceJustified;

    private String attendanceJustification;

    private LocalDateTime attendanceJustifiedAt;

    private Long attendanceJustifiedBy;

    @ElementCollection
    @CollectionTable(name = "appraisal_achievements", joinColumns = @JoinColumn(name = "appraisal_id"))
    @Column(name = "achievement")
    private java.util.List<String> achievements = new java.util.ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "appraisal_strengths", joinColumns = @JoinColumn(name = "appraisal_id"))
    @Column(name = "strength")
    private java.util.List<String> strengths = new java.util.ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "appraisal_improvement_areas", joinColumns = @JoinColumn(name = "appraisal_id"))
    @Column(name = "improvement_area")
    private java.util.List<String> improvementAreas = new java.util.ArrayList<>();

    private Double leadershipOwnershipRating;
    private Double technicalExcellenceRating;
    private Double deliveryManagementRating;
    private Double communicationInfluenceRating;
    private Double teamMentorshipRating;
    private Double innovationInitiativeRating;

    private String myBand = "A+";

    private java.time.LocalDate selfReviewDueDate = java.time.LocalDate.of(2026, 4, 25);

    private boolean financeStageStarted = false;
    private boolean compensationFrozen = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public AppraisalCycle getCycle() {
        return cycle;
    }

    public void setCycle(AppraisalCycle cycle) {
        this.cycle = cycle;
    }

    public AppraisalRequest getRequest() {
        return request;
    }

    public void setRequest(AppraisalRequest request) {
        this.request = request;
    }

    public AppraisalStatus getStatus() {
        return status;
    }

    public void setStatus(AppraisalStatus status) {
        this.status = status;
    }

    public Integer getCurrentStageOrder() {
        return currentStageOrder;
    }

    public void setCurrentStageOrder(Integer currentStageOrder) {
        this.currentStageOrder = currentStageOrder;
    }

    public Double getSelfRating() {
        return selfRating;
    }

    public void setSelfRating(Double selfRating) {
        this.selfRating = selfRating;
    }

    public String getSelfReview() {
        return selfReview;
    }

    public void setSelfReview(String selfReview) {
        this.selfReview = selfReview;
    }

    public LocalDateTime getSelfReviewSubmittedAt() {
        return selfReviewSubmittedAt;
    }

    public void setSelfReviewSubmittedAt(LocalDateTime selfReviewSubmittedAt) {
        this.selfReviewSubmittedAt = selfReviewSubmittedAt;
    }

    public Employee getReviewer() {
        return reviewer;
    }

    public void setReviewer(Employee reviewer) {
        this.reviewer = reviewer;
    }

    public Double getManagerRating() {
        return managerRating;
    }

    public void setManagerRating(Double managerRating) {
        this.managerRating = managerRating;
    }

    public String getManagerReview() {
        return managerReview;
    }

    public void setManagerReview(String managerReview) {
        this.managerReview = managerReview;
    }

    public LocalDateTime getManagerReviewSubmittedAt() {
        return managerReviewSubmittedAt;
    }

    public void setManagerReviewSubmittedAt(LocalDateTime managerReviewSubmittedAt) {
        this.managerReviewSubmittedAt = managerReviewSubmittedAt;
    }

    public Double getFinalRating() {
        return finalRating;
    }

    public void setFinalRating(Double finalRating) {
        this.finalRating = finalRating;
    }

    public String getPerformanceCategory() {
        return performanceCategory;
    }

    public void setPerformanceCategory(String performanceCategory) {
        this.performanceCategory = performanceCategory;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public boolean isAttendanceJustified() {
        return attendanceJustified;
    }

    public void setAttendanceJustified(boolean attendanceJustified) {
        this.attendanceJustified = attendanceJustified;
    }

    public String getAttendanceJustification() {
        return attendanceJustification;
    }

    public void setAttendanceJustification(String attendanceJustification) {
        this.attendanceJustification = attendanceJustification;
    }

    public LocalDateTime getAttendanceJustifiedAt() {
        return attendanceJustifiedAt;
    }

    public void setAttendanceJustifiedAt(LocalDateTime attendanceJustifiedAt) {
        this.attendanceJustifiedAt = attendanceJustifiedAt;
    }

    public Long getAttendanceJustifiedBy() {
        return attendanceJustifiedBy;
    }

    public void setAttendanceJustifiedBy(Long attendanceJustifiedBy) {
        this.attendanceJustifiedBy = attendanceJustifiedBy;
    }

    public java.util.List<String> getAchievements() {
        return achievements;
    }

    public void setAchievements(java.util.List<String> achievements) {
        this.achievements = achievements;
    }

    public java.util.List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(java.util.List<String> strengths) {
        this.strengths = strengths;
    }

    public java.util.List<String> getImprovementAreas() {
        return improvementAreas;
    }

    public void setImprovementAreas(java.util.List<String> improvementAreas) {
        this.improvementAreas = improvementAreas;
    }

    public Double getLeadershipOwnershipRating() { return leadershipOwnershipRating; }
    public void setLeadershipOwnershipRating(Double leadershipOwnershipRating) { this.leadershipOwnershipRating = leadershipOwnershipRating; }

    public Double getTechnicalExcellenceRating() { return technicalExcellenceRating; }
    public void setTechnicalExcellenceRating(Double technicalExcellenceRating) { this.technicalExcellenceRating = technicalExcellenceRating; }

    public Double getDeliveryManagementRating() { return deliveryManagementRating; }
    public void setDeliveryManagementRating(Double deliveryManagementRating) { this.deliveryManagementRating = deliveryManagementRating; }

    public Double getCommunicationInfluenceRating() { return communicationInfluenceRating; }
    public void setCommunicationInfluenceRating(Double communicationInfluenceRating) { this.communicationInfluenceRating = communicationInfluenceRating; }

    public Double getTeamMentorshipRating() { return teamMentorshipRating; }
    public void setTeamMentorshipRating(Double teamMentorshipRating) { this.teamMentorshipRating = teamMentorshipRating; }

    public Double getInnovationInitiativeRating() { return innovationInitiativeRating; }
    public void setInnovationInitiativeRating(Double innovationInitiativeRating) { this.innovationInitiativeRating = innovationInitiativeRating; }

    public String getMyBand() { return myBand; }
    public void setMyBand(String myBand) { this.myBand = myBand; }

    public java.time.LocalDate getSelfReviewDueDate() { return selfReviewDueDate; }
    public void setSelfReviewDueDate(java.time.LocalDate selfReviewDueDate) { this.selfReviewDueDate = selfReviewDueDate; }

    public boolean isFinanceStageStarted() { return financeStageStarted; }
    public void setFinanceStageStarted(boolean financeStageStarted) { this.financeStageStarted = financeStageStarted; }

    public boolean isCompensationFrozen() { return compensationFrozen; }
    public void setCompensationFrozen(boolean compensationFrozen) { this.compensationFrozen = compensationFrozen; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
