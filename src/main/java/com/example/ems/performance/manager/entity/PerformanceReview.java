package com.example.ems.performance.manager.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "ManagerPerformanceReview")
@Table(name = "manager_performance_reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employee_id", "review_cycle"})
})
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @Column(name = "review_cycle", nullable = false)
    private String reviewCycle; // e.g. FY2024-25

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.NOT_STARTED;

    private Double selfRating = 0.0;

    private Double managerRating = 0.0;

    private Double finalScore = 0.0;

    private Integer goalsMet = 0;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    @Column(columnDefinition = "TEXT")
    private String managerComment;

    private String recommendation;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompetencyRating> competencies = new ArrayList<>();

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerformanceGoal> goals = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Employee getManager() { return manager; }
    public void setManager(Employee manager) { this.manager = manager; }

    public String getReviewCycle() { return reviewCycle; }
    public void setReviewCycle(String reviewCycle) { this.reviewCycle = reviewCycle; }

    public ReviewStatus getStatus() { return status; }
    public void setStatus(ReviewStatus status) { this.status = status; }

    public Double getSelfRating() { return selfRating; }
    public void setSelfRating(Double selfRating) { this.selfRating = selfRating; }

    public Double getManagerRating() { return managerRating; }
    public void setManagerRating(Double managerRating) { this.managerRating = managerRating; }

    public Double getFinalScore() { return finalScore; }
    public void setFinalScore(Double finalScore) { this.finalScore = finalScore; }

    public Integer getGoalsMet() { return goalsMet; }
    public void setGoalsMet(Integer goalsMet) { this.goalsMet = goalsMet; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }

    public String getManagerComment() { return managerComment; }
    public void setManagerComment(String managerComment) { this.managerComment = managerComment; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public List<CompetencyRating> getCompetencies() { return competencies; }
    public void setCompetencies(List<CompetencyRating> competencies) { this.competencies = competencies; }

    public List<PerformanceGoal> getGoals() { return goals; }
    public void setGoals(List<PerformanceGoal> goals) { this.goals = goals; }
}
