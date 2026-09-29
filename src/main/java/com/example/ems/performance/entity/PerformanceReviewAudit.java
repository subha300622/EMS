package com.example.ems.performance.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "performance_review_audits", indexes = {
        @Index(name = "idx_perf_audits_org", columnList = "organization_id"),
        @Index(name = "idx_perf_audits_review", columnList = "review_id")
})
public class PerformanceReviewAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @Column(name = "review_id", nullable = false)
    private Long reviewId;

    @Column(nullable = false, length = 50)
    private String action; // CREATE, SELF_REVIEW, MANAGER_REVIEW, CALCULATE, RECALCULATE, SUBMIT, APPROVE, REJECT, REOPEN, PUBLISH, LOCK, CANCEL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee actor;

    @Column(name = "actor_name")
    private String actorName;

    @Column(name = "before_status", length = 50)
    private String beforeStatus;

    @Column(name = "after_status", length = 50)
    private String afterStatus;

    @Column(name = "before_score", precision = 5, scale = 2)
    private BigDecimal beforeScore;

    @Column(name = "after_score", precision = 5, scale = 2)
    private BigDecimal afterScore;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_data", columnDefinition = "jsonb")
    private String snapshotData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public PerformanceReviewAudit() {}

    public PerformanceReviewAudit(Organization organization, Long reviewId, String action,
                                  Employee actor, String actorName, String beforeStatus,
                                  String afterStatus, BigDecimal beforeScore, BigDecimal afterScore,
                                  String reason, String snapshotData) {
        this.organization = organization;
        this.reviewId = reviewId;
        this.action = action;
        this.actor = actor;
        this.actorName = actorName;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.beforeScore = beforeScore;
        this.afterScore = afterScore;
        this.reason = reason;
        this.snapshotData = snapshotData;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long reviewId) { this.reviewId = reviewId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Employee getActor() { return actor; }
    public void setActor(Employee actor) { this.actor = actor; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getBeforeStatus() { return beforeStatus; }
    public void setBeforeStatus(String beforeStatus) { this.beforeStatus = beforeStatus; }

    public String getAfterStatus() { return afterStatus; }
    public void setAfterStatus(String afterStatus) { this.afterStatus = afterStatus; }

    public BigDecimal getBeforeScore() { return beforeScore; }
    public void setBeforeScore(BigDecimal beforeScore) { this.beforeScore = beforeScore; }

    public BigDecimal getAfterScore() { return afterScore; }
    public void setAfterScore(BigDecimal afterScore) { this.afterScore = afterScore; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getSnapshotData() { return snapshotData; }
    public void setSnapshotData(String snapshotData) { this.snapshotData = snapshotData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
