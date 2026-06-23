package com.example.ems.performance.manager.entity;

import jakarta.persistence.*;

@Entity(name = "ManagerPerformanceGoal")
@Table(name = "manager_performance_goals")
public class PerformanceGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private PerformanceReview review;

    @Column(nullable = false)
    private String title;

    private Integer weight = 0;

    private Integer progress = 0; // 0-100

    private Boolean achieved = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PerformanceReview getReview() { return review; }
    public void setReview(PerformanceReview review) { this.review = review; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getWeight() { return weight; }
    public void setWeight(Integer weight) { this.weight = weight; }

    public Integer getProgress() { return progress; }
    public void setProgress(Integer progress) { this.progress = progress; }

    public Boolean getAchieved() { return achieved; }
    public void setAchieved(Boolean achieved) { this.achieved = achieved; }
}
