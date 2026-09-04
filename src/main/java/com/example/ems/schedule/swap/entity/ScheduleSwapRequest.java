package com.example.ems.schedule.swap.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import com.example.ems.schedule.entity.Schedule;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "schedule_swap_requests")
public class ScheduleSwapRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, unique = true)
    private String requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_schedule_id", nullable = false)
    private Schedule sourceSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_employee_id", nullable = false)
    private Employee sourceEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_schedule_id", nullable = false)
    private Schedule targetSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_employee_id", nullable = false)
    private Employee targetEmployee;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleSwapStatus status = ScheduleSwapStatus.PENDING_APPROVAL;

    @Column(name = "workflow_instance_id")
    private String workflowInstanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Employee createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    public ScheduleSwapRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Schedule getSourceSchedule() { return sourceSchedule; }
    public void setSourceSchedule(Schedule sourceSchedule) { this.sourceSchedule = sourceSchedule; }

    public Employee getSourceEmployee() { return sourceEmployee; }
    public void setSourceEmployee(Employee sourceEmployee) { this.sourceEmployee = sourceEmployee; }

    public Schedule getTargetSchedule() { return targetSchedule; }
    public void setTargetSchedule(Schedule targetSchedule) { this.targetSchedule = targetSchedule; }

    public Employee getTargetEmployee() { return targetEmployee; }
    public void setTargetEmployee(Employee targetEmployee) { this.targetEmployee = targetEmployee; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public ScheduleSwapStatus getStatus() { return status; }
    public void setStatus(ScheduleSwapStatus status) { this.status = status; }

    public String getWorkflowInstanceId() { return workflowInstanceId; }
    public void setWorkflowInstanceId(String workflowInstanceId) { this.workflowInstanceId = workflowInstanceId; }

    public Employee getCreatedBy() { return createdBy; }
    public void setCreatedBy(Employee createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
