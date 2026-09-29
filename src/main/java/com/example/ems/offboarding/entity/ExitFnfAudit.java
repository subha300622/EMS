package com.example.ems.offboarding.entity;

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
@Table(name = "exit_fnf_audits", indexes = {
        @Index(name = "idx_exit_fnf_audits_settlement", columnList = "settlement_id"),
        @Index(name = "idx_exit_fnf_audits_org", columnList = "organization_id")
})
public class ExitFnfAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    @JsonIgnore
    private Organization organization;

    @Column(name = "settlement_id", nullable = false)
    private Long settlementId;

    @Column(nullable = false, length = 50)
    private String action; // CALCULATE, UPDATE, PAYMENT

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    @JsonIgnoreProperties({"manager", "team", "organization"})
    private Employee actor;

    @Column(name = "actor_name")
    private String actorName;

    @Column(name = "before_amount", precision = 15, scale = 2)
    private BigDecimal beforeAmount;

    @Column(name = "after_amount", precision = 15, scale = 2)
    private BigDecimal afterAmount;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_data", columnDefinition = "jsonb")
    private String snapshotData;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ExitFnfAudit() {}

    public ExitFnfAudit(Organization organization, Long settlementId, String action, Employee actor, String actorName,
                        BigDecimal beforeAmount, BigDecimal afterAmount, String reason, String snapshotData) {
        this.organization = organization;
        this.settlementId = settlementId;
        this.action = action;
        this.actor = actor;
        this.actorName = actorName;
        this.beforeAmount = beforeAmount;
        this.afterAmount = afterAmount;
        this.reason = reason;
        this.snapshotData = snapshotData;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Employee getActor() { return actor; }
    public void setActor(Employee actor) { this.actor = actor; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public BigDecimal getBeforeAmount() { return beforeAmount; }
    public void setBeforeAmount(BigDecimal beforeAmount) { this.beforeAmount = beforeAmount; }

    public BigDecimal getAfterAmount() { return afterAmount; }
    public void setAfterAmount(BigDecimal afterAmount) { this.afterAmount = afterAmount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getSnapshotData() { return snapshotData; }
    public void setSnapshotData(String snapshotData) { this.snapshotData = snapshotData; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
