package com.example.ems.appraisal.entity;

import com.example.ems.employee.entity.Employee;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appraisals")
public class Appraisal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cycle_id", nullable = false)
    private AppraisalCycle cycle;

    private Integer selfRating;

    @Column(columnDefinition = "TEXT")
    private String selfReview;

    private LocalDateTime selfReviewSubmittedAt;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private Employee reviewer;

    private Integer managerRating;

    @Column(columnDefinition = "TEXT")
    private String managerReview;

    private LocalDateTime managerReviewSubmittedAt;

    private Integer finalRating;

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, SELF_REVIEWED, MANAGER_REVIEWED, FINALIZED

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public AppraisalCycle getCycle() { return cycle; }
    public void setCycle(AppraisalCycle cycle) { this.cycle = cycle; }

    public Integer getSelfRating() { return selfRating; }
    public void setSelfRating(Integer selfRating) { this.selfRating = selfRating; }

    public String getSelfReview() { return selfReview; }
    public void setSelfReview(String selfReview) { this.selfReview = selfReview; }

    public LocalDateTime getSelfReviewSubmittedAt() { return selfReviewSubmittedAt; }
    public void setSelfReviewSubmittedAt(LocalDateTime selfReviewSubmittedAt) { this.selfReviewSubmittedAt = selfReviewSubmittedAt; }

    public Employee getReviewer() { return reviewer; }
    public void setReviewer(Employee reviewer) { this.reviewer = reviewer; }

    public Integer getManagerRating() { return managerRating; }
    public void setManagerRating(Integer managerRating) { this.managerRating = managerRating; }

    public String getManagerReview() { return managerReview; }
    public void setManagerReview(String managerReview) { this.managerReview = managerReview; }

    public LocalDateTime getManagerReviewSubmittedAt() { return managerReviewSubmittedAt; }
    public void setManagerReviewSubmittedAt(LocalDateTime managerReviewSubmittedAt) { this.managerReviewSubmittedAt = managerReviewSubmittedAt; }

    public Integer getFinalRating() { return finalRating; }
    public void setFinalRating(Integer finalRating) { this.finalRating = finalRating; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
