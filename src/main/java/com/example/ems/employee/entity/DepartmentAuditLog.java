package com.example.ems.employee.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "department_audit_logs")
public class DepartmentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long departmentId;
    private String field;
    private String oldValue;
    private String newValue;
    private Long changedByUserId;
    private String changedByUserName;
    private String changedByUserRole;
    private String comment;
    private LocalDateTime changedAt = LocalDateTime.now();

    public DepartmentAuditLog() {}

    public DepartmentAuditLog(Long departmentId, String field, String oldValue, String newValue, 
                              Long changedByUserId, String changedByUserName, String changedByUserRole, String comment) {
        this.departmentId = departmentId;
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.changedByUserId = changedByUserId;
        this.changedByUserName = changedByUserName;
        this.changedByUserRole = changedByUserRole;
        this.comment = comment;
        this.changedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(Long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getChangedByUserName() {
        return changedByUserName;
    }

    public void setChangedByUserName(String changedByUserName) {
        this.changedByUserName = changedByUserName;
    }

    public String getChangedByUserRole() {
        return changedByUserRole;
    }

    public void setChangedByUserRole(String changedByUserRole) {
        this.changedByUserRole = changedByUserRole;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
