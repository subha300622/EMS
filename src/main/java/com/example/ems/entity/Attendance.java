package com.example.ems.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String status;

    private LocalTime punchInTime;

    private LocalTime punchOutTime;

    @Column(columnDefinition = "TEXT")
    private String notes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalTime getPunchInTime() { return punchInTime; }
    public void setPunchInTime(LocalTime punchInTime) { this.punchInTime = punchInTime; }

    public LocalTime getPunchOutTime() { return punchOutTime; }
    public void setPunchOutTime(LocalTime punchOutTime) { this.punchOutTime = punchOutTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Transient
    @JsonProperty("workingHours")
    public String getWorkingHours() {
        if (punchInTime == null || punchOutTime == null) {
            return null;
        }
        Duration duration = Duration.between(punchInTime, punchOutTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%dh %02dm", hours, minutes);
    }
}
