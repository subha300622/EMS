package com.example.ems.employee.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "my_teams")
public class MyTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String teamName;

    private String department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id")
    private Employee manager;

    public MyTeam() {}

    public MyTeam(Long id, String teamName, String department, Employee manager) {
        this.id = id;
        this.teamName = teamName;
        this.department = department;
        this.manager = manager;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Employee getManager() { return manager; }
    public void setManager(Employee manager) { this.manager = manager; }
}
