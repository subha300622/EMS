package com.example.ems.appraisal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appraisal_assessments")
public class AppraisalAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "appraisal_id", nullable = false, unique = true)
    private Appraisal appraisal;

    @Column(name = "overall_rating")
    private Double overallRating;

    @Column(columnDefinition = "TEXT")
    private String strengths;

    @Column(columnDefinition = "TEXT")
    private String achievements;

    @Column(name = "development_areas", columnDefinition = "TEXT")
    private String developmentAreas;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Appraisal getAppraisal() {
        return appraisal;
    }

    public void setAppraisal(Appraisal appraisal) {
        this.appraisal = appraisal;
    }

    public Double getOverallRating() {
        return overallRating;
    }

    public void setOverallRating(Double overallRating) {
        this.overallRating = overallRating;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getAchievements() {
        return achievements;
    }

    public void setAchievements(String achievements) {
        this.achievements = achievements;
    }

    public String getDevelopmentAreas() {
        return developmentAreas;
    }

    public void setDevelopmentAreas(String developmentAreas) {
        this.developmentAreas = developmentAreas;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

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
