package com.example.ems.schedule.entity;

import com.example.ems.employee.entity.Employee;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_employee_availabilities")
public class MyEmployeeAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String dayOfWeek; // MONDAY, TUESDAY, etc.

    @Column(nullable = false)
    private String availableFrom; // "09:00"

    @Column(nullable = false)
    private String availableTo; // "18:00"

    @ManyToOne(optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public MyEmployeeAvailability() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(String availableFrom) { this.availableFrom = availableFrom; }

    public String getAvailableTo() { return availableTo; }
    public void setAvailableTo(String availableTo) { this.availableTo = availableTo; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
