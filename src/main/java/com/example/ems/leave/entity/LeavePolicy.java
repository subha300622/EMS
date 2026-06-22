package com.example.ems.leave.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_policies")
public class LeavePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "leave_type_id")
    private LeaveType leaveType;

    private Integer carryingLimit;

    private String accrualType; // MONTHLY, ANNUALLY, etc.

    private String status = "ACTIVE"; // ACTIVE, INACTIVE

    public LeavePolicy() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public Integer getCarryingLimit() {
        return carryingLimit;
    }

    public void setCarryingLimit(Integer carryingLimit) {
        this.carryingLimit = carryingLimit;
    }

    public String getAccrualType() {
        return accrualType;
    }

    public void setAccrualType(String accrualType) {
        this.accrualType = accrualType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
