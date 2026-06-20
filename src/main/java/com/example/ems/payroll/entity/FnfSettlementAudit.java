package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fnf_settlement_audit_trail")
public class FnfSettlementAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long settlementId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FnfSettlementStatus status;

    @Column(nullable = false)
    private String updatedBy;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String remarks;

    public FnfSettlementAudit() {}

    public FnfSettlementAudit(Long settlementId, FnfSettlementStatus status, String updatedBy, String remarks) {
        this.settlementId = settlementId;
        this.status = status;
        this.updatedBy = updatedBy;
        this.remarks = remarks;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSettlementId() { return settlementId; }
    public void setSettlementId(Long settlementId) { this.settlementId = settlementId; }

    public FnfSettlementStatus getStatus() { return status; }
    public void setStatus(FnfSettlementStatus status) { this.status = status; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
