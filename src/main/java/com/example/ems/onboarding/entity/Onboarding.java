package com.example.ems.onboarding.entity;

import com.example.ems.employee.entity.Employee;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "onboardings")
public class Onboarding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "manager_id")
    private Employee manager;

    @ManyToOne
    @JoinColumn(name = "buddy_id")
    private Employee buddy;

    @Column(nullable = false)
    private String status = "INITIATED";

    private LocalDate startDate;
    private LocalDate joiningDate;
    private LocalDate completionDate;

    @Column(nullable = false)
    private int progress = 0;

    @Column(name = "assigned_template_id")
    private String assignedTemplateId;

    @Column(name = "hr_owner_id")
    private String hrOwnerId;

    @Column(name = "buddy_id_string")
    private String buddyIdString;

    @Column(name = "it_contact_id")
    private String itContactId;

    @Column(name = "finance_contact_id")
    private String financeContactId;

    @Version
    private Long optVersion;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public String getAssignedTemplateId() { return assignedTemplateId; }
    public void setAssignedTemplateId(String assignedTemplateId) { this.assignedTemplateId = assignedTemplateId; }

    public String getHrOwnerId() { return hrOwnerId; }
    public void setHrOwnerId(String hrOwnerId) { this.hrOwnerId = hrOwnerId; }

    public String getBuddyIdString() { return buddyIdString; }
    public void setBuddyIdString(String buddyIdString) { this.buddyIdString = buddyIdString; }

    public String getItContactId() { return itContactId; }
    public void setItContactId(String itContactId) { this.itContactId = itContactId; }

    public String getFinanceContactId() { return financeContactId; }
    public void setFinanceContactId(String financeContactId) { this.financeContactId = financeContactId; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Employee getManager() { return manager; }
    public void setManager(Employee manager) { this.manager = manager; }

    public Employee getBuddy() { return buddy; }
    public void setBuddy(Employee buddy) { this.buddy = buddy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public Long getOptVersion() { return optVersion; }
    public void setOptVersion(Long optVersion) { this.optVersion = optVersion; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
