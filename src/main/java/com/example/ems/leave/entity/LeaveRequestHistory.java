package com.example.ems.leave.entity;

import com.example.ems.employee.entity.Employee;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_request_histories")
public class LeaveRequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_id", nullable = false)
    private Leave leave;

    @Column(nullable = false)
    private String action; // APPLIED, EDITED, APPROVED, REJECTED, CANCELLED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_id")
    @JsonIgnoreProperties({"manager", "team", "hibernateLazyInitializer", "handler"})
    private Employee performedBy;

    private LocalDateTime performedAt = LocalDateTime.now();

    private String oldStatus;
    private String newStatus;
    private String remarks;

    public LeaveRequestHistory() {}

    public LeaveRequestHistory(Leave leave, String action, Employee performedBy, String oldStatus, String newStatus, String remarks) {
        this.leave = leave;
        this.action = action;
        this.performedBy = performedBy;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.remarks = remarks;
        this.performedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Leave getLeave() { return leave; }
    public void setLeave(Leave leave) { this.leave = leave; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Employee getPerformedBy() { return performedBy; }
    public void setPerformedBy(Employee performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }

    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }

    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
