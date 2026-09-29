package com.example.ems.training.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "training_feedbacks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"training_id", "employee_id"})
})
public class TrainingFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_id", nullable = false)
    private Long trainingId;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(nullable = false)
    private Integer rating;

    @Column(name = "content_quality_rating")
    private Integer contentQualityRating;

    @Column(name = "trainer_rating")
    private Integer trainerRating;

    @Column(name = "overall_experience_rating")
    private Integer overallExperienceRating;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTrainingId() { return trainingId; }
    public void setTrainingId(Long trainingId) { this.trainingId = trainingId; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Integer getContentQualityRating() { return contentQualityRating; }
    public void setContentQualityRating(Integer contentQualityRating) { this.contentQualityRating = contentQualityRating; }

    public Integer getTrainerRating() { return trainerRating; }
    public void setTrainerRating(Integer trainerRating) { this.trainerRating = trainerRating; }

    public Integer getOverallExperienceRating() { return overallExperienceRating; }
    public void setOverallExperienceRating(Integer overallExperienceRating) { this.overallExperienceRating = overallExperienceRating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
