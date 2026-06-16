package com.example.ems.schedule.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_schedule_change_requests")
public class MyScheduleChangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String requestNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "current_shift_id", nullable = false)
    private MyShiftTemplate currentShift;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_shift_id", nullable = false)
    private MyShiftTemplate requestedShift;

    @Column(nullable = false)
    private LocalDate requestedDate;

    @Column(nullable = false)
    private String requestType; // SHIFT_CHANGE

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private String status; // PENDING_MANAGER_APPROVAL, APPROVED, REJECTED

    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    public MyScheduleChangeRequest() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequestNumber() { return requestNumber; }
    public void setRequestNumber(String requestNumber) { this.requestNumber = requestNumber; }

    public MyShiftTemplate getCurrentShift() { return currentShift; }
    public void setCurrentShift(MyShiftTemplate currentShift) { this.currentShift = currentShift; }

    public MyShiftTemplate getRequestedShift() { return requestedShift; }
    public void setRequestedShift(MyShiftTemplate requestedShift) { this.requestedShift = requestedShift; }

    public LocalDate getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDate requestedDate) { this.requestedDate = requestedDate; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
}
