package com.example.ems.appraisal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bulk_appraisal_item_results")
public class BulkAppraisalItemResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "action_log_id", nullable = false)
    private BulkAppraisalActionLog actionLog;

    @Column(nullable = false)
    private Long appraisalId;

    @Column(nullable = false)
    private String status; // SUCCESS or FAILED

    private String reason;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BulkAppraisalActionLog getActionLog() { return actionLog; }
    public void setActionLog(BulkAppraisalActionLog actionLog) { this.actionLog = actionLog; }

    public Long getAppraisalId() { return appraisalId; }
    public void setAppraisalId(Long appraisalId) { this.appraisalId = appraisalId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
