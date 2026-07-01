package com.example.ems.offboarding.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "exit_kt_system_access")
public class ExitKtSystemAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "kt_plan_id", nullable = false)
    @JsonIgnore
    private ExitKtPlan ktPlan;

    @Column(nullable = false)
    private String systemName;

    @Column(nullable = false)
    private String accessType = "READ_ONLY";

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, REVOKED, PENDING, etc.

    @Column(nullable = false)
    private String handoverStatus = "PENDING"; // PENDING, TRANSFERRED

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ExitKtPlan getKtPlan() { return ktPlan; }
    public void setKtPlan(ExitKtPlan ktPlan) { this.ktPlan = ktPlan; }

    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = systemName; }

    public String getAccessType() { return accessType; }
    public void setAccessType(String accessType) { this.accessType = accessType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getHandoverStatus() { return handoverStatus; }
    public void setHandoverStatus(String handoverStatus) { this.handoverStatus = handoverStatus; }
}
