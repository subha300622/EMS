package com.example.ems.schedule.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "my_shift_templates")
public class MyShiftTemplate {

    @Id
    private Long id; // Set manually (e.g. 101, 102) for consistent seeding

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String startTime;

    @Column(nullable = false)
    private String endTime;

    @Column(nullable = false)
    private Integer breakDurationMinutes;

    @Column(nullable = false)
    private String location;

    public MyShiftTemplate() {}

    public MyShiftTemplate(Long id, String name, String startTime, String endTime, Integer breakDurationMinutes, String location) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.breakDurationMinutes = breakDurationMinutes;
        this.location = location;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public Integer getBreakDurationMinutes() { return breakDurationMinutes; }
    public void setBreakDurationMinutes(Integer breakDurationMinutes) { this.breakDurationMinutes = breakDurationMinutes; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
