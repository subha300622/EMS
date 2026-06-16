package com.example.ems.schedule.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_schedule_timeline_events")
public class MyScheduleTimelineEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String event; // SHIFT_ASSIGNED, etc.

    @Column(nullable = false)
    private String performedBy;

    @Column(nullable = false)
    private LocalDateTime performedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    public MyScheduleTimelineEvent() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
}
