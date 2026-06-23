package com.example.ems.performance.manager.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "competency_ratings")
public class CompetencyRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private PerformanceReview review;

    @Column(name = "competency_name", nullable = false)
    private String competencyName; // Technical Skills

    private Integer selfScore = 0;

    private Integer managerScore = 0;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PerformanceReview getReview() { return review; }
    public void setReview(PerformanceReview review) { this.review = review; }

    public String getCompetencyName() { return competencyName; }
    public void setCompetencyName(String competencyName) { this.competencyName = competencyName; }

    public Integer getSelfScore() { return selfScore; }
    public void setSelfScore(Integer selfScore) { this.selfScore = selfScore; }

    public Integer getManagerScore() { return managerScore; }
    public void setManagerScore(Integer managerScore) { this.managerScore = managerScore; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
