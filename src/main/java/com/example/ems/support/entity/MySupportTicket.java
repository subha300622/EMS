package com.example.ems.support.entity;

import com.example.ems.employee.entity.Employee;
import com.example.ems.organization.entity.Organization;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_support_tickets")
public class MySupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String ticketNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private MySupportCategory category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_category_id")
    private MySupportSubCategory subCategory;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportTicketPriority priority = SupportTicketPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupportTicketStatus status = SupportTicketStatus.OPEN;

    private String preferredContactMethod = "EMAIL";

    private String assignedTeam = "IT Helpdesk";

    private String assignedAgent;

    private Integer rating;

    private String feedback;

    private String escalationReason;

    private String oldPriority;

    private Integer slaResolutionTimeHours = 24;

    private LocalDateTime slaResponseDueAt;

    private LocalDateTime slaResolutionDueAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "merged_into_ticket_id")
    private Long mergedIntoTicketId;

    @Column(name = "merged_by")
    private String mergedBy;

    @Column(name = "merged_at")
    private LocalDateTime mergedAt;

    @Column(name = "merge_reason", length = 1000)
    private String mergeReason;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;

    private LocalDateTime closedAt;

    public MySupportTicket() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public MySupportCategory getCategory() { return category; }
    public void setCategory(MySupportCategory category) { this.category = category; }

    public MySupportSubCategory getSubCategory() { return subCategory; }
    public void setSubCategory(MySupportSubCategory subCategory) { this.subCategory = subCategory; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public SupportTicketPriority getPriority() { return priority; }
    public void setPriority(SupportTicketPriority priority) { this.priority = priority; }

    public SupportTicketStatus getStatus() { return status; }
    public void setStatus(SupportTicketStatus status) { this.status = status; }

    public String getPreferredContactMethod() { return preferredContactMethod; }
    public void setPreferredContactMethod(String preferredContactMethod) { this.preferredContactMethod = preferredContactMethod; }

    public String getAssignedTeam() { return assignedTeam; }
    public void setAssignedTeam(String assignedTeam) { this.assignedTeam = assignedTeam; }

    public String getAssignedAgent() { return assignedAgent; }
    public void setAssignedAgent(String assignedAgent) { this.assignedAgent = assignedAgent; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getEscalationReason() { return escalationReason; }
    public void setEscalationReason(String escalationReason) { this.escalationReason = escalationReason; }

    public String getOldPriority() { return oldPriority; }
    public void setOldPriority(String oldPriority) { this.oldPriority = oldPriority; }

    public Integer getSlaResolutionTimeHours() { return slaResolutionTimeHours; }
    public void setSlaResolutionTimeHours(Integer slaResolutionTimeHours) { this.slaResolutionTimeHours = slaResolutionTimeHours; }

    public LocalDateTime getSlaResponseDueAt() { return slaResponseDueAt; }
    public void setSlaResponseDueAt(LocalDateTime slaResponseDueAt) { this.slaResponseDueAt = slaResponseDueAt; }

    public LocalDateTime getSlaResolutionDueAt() { return slaResolutionDueAt; }
    public void setSlaResolutionDueAt(LocalDateTime slaResolutionDueAt) { this.slaResolutionDueAt = slaResolutionDueAt; }

    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }

    public Long getMergedIntoTicketId() { return mergedIntoTicketId; }
    public void setMergedIntoTicketId(Long mergedIntoTicketId) { this.mergedIntoTicketId = mergedIntoTicketId; }

    public String getMergedBy() { return mergedBy; }
    public void setMergedBy(String mergedBy) { this.mergedBy = mergedBy; }

    public LocalDateTime getMergedAt() { return mergedAt; }
    public void setMergedAt(LocalDateTime mergedAt) { this.mergedAt = mergedAt; }

    public String getMergeReason() { return mergeReason; }
    public void setMergeReason(String mergeReason) { this.mergeReason = mergeReason; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
}
