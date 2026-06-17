package com.example.ems.employee.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "my_employee_skills")
public class MyEmployeeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private String name;

    private String level = "INTERMEDIATE"; // BEGINNER, INTERMEDIATE, ADVANCED

    private Integer experienceYears = 0;

    public MyEmployeeSkill() {}

    public MyEmployeeSkill(Long id, Employee employee, String name, String level, Integer experienceYears) {
        this.id = id;
        this.employee = employee;
        this.name = name;
        this.level = level;
        this.experienceYears = experienceYears;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Integer getExperienceYears() { return experienceYears; }
    public void setExperienceYears(Integer experienceYears) { this.experienceYears = experienceYears; }
}
