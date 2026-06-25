package com.example.ems.offboarding.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "exit_kt_contacts")
public class ExitKtContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "kt_plan_id", nullable = false)
    @JsonIgnore
    private ExitKtPlan ktPlan;

    @Column(nullable = false)
    private String name;

    private String role;

    private String email;

    @Column(columnDefinition = "TEXT")
    private String responsibility;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExitKtPlan getKtPlan() { return ktPlan; }
    public void setKtPlan(ExitKtPlan ktPlan) { this.ktPlan = ktPlan; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getResponsibility() { return responsibility; }
    public void setResponsibility(String responsibility) { this.responsibility = responsibility; }
}
