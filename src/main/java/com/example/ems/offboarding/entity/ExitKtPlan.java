package com.example.ems.offboarding.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exit_kt_plan")
public class ExitKtPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    @ManyToOne
    @JoinColumn(name = "handover_person_id")
    private Employee handoverPerson;

    @Column(nullable = false)
    private String status = "IN_PROGRESS"; // IN_PROGRESS, COMPLETED, etc.

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "ktPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExitKtProject> projects = new ArrayList<>();

    @OneToMany(mappedBy = "ktPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExitKtContact> contacts = new ArrayList<>();

    @OneToMany(mappedBy = "ktPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExitKtSystemAccess> systemAccesses = new ArrayList<>();

    @OneToMany(mappedBy = "ktPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExitKtTask> tasks = new ArrayList<>();

    @Column(nullable = false)
    private boolean projectsCompleted = false;

    @Column(nullable = false)
    private boolean contactsCompleted = false;

    @Column(nullable = false)
    private boolean systemCredentialsCompleted = false;

    @Column(nullable = false)
    private boolean pendingTasksCompleted = false;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Employee getHandoverPerson() { return handoverPerson; }
    public void setHandoverPerson(Employee handoverPerson) { this.handoverPerson = handoverPerson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isProjectsCompleted() { return projectsCompleted; }
    public void setProjectsCompleted(boolean projectsCompleted) { this.projectsCompleted = projectsCompleted; }

    public boolean isContactsCompleted() { return contactsCompleted; }
    public void setContactsCompleted(boolean contactsCompleted) { this.contactsCompleted = contactsCompleted; }

    public boolean isSystemCredentialsCompleted() { return systemCredentialsCompleted; }
    public void setSystemCredentialsCompleted(boolean systemCredentialsCompleted) { this.systemCredentialsCompleted = systemCredentialsCompleted; }

    public boolean isPendingTasksCompleted() { return pendingTasksCompleted; }
    public void setPendingTasksCompleted(boolean pendingTasksCompleted) { this.pendingTasksCompleted = pendingTasksCompleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<ExitKtProject> getProjects() { return projects; }
    public void setProjects(List<ExitKtProject> projects) { this.projects = projects; }

    public List<ExitKtContact> getContacts() { return contacts; }
    public void setContacts(List<ExitKtContact> contacts) { this.contacts = contacts; }

    public List<ExitKtSystemAccess> getSystemAccesses() { return systemAccesses; }
    public void setSystemAccesses(List<ExitKtSystemAccess> systemAccesses) { this.systemAccesses = systemAccesses; }

    public List<ExitKtTask> getTasks() { return tasks; }
    public void setTasks(List<ExitKtTask> tasks) { this.tasks = tasks; }
}
