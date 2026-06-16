package com.example.ems.offboarding.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "offboarding_settlements")
public class OffboardingSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "offboarding_id", nullable = false)
    private Offboarding offboarding;

    private BigDecimal gratuity = BigDecimal.ZERO;
    private BigDecimal severance = BigDecimal.ZERO;
    private BigDecimal pendingSalary = BigDecimal.ZERO;
    private BigDecimal deductions = BigDecimal.ZERO;
    private BigDecimal totalSettlementAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private String paymentStatus = "PENDING"; // PENDING, PAID

    private LocalDateTime processedAt;

    private BigDecimal leaveEncashment = BigDecimal.ZERO;
    private BigDecimal reimbursements = BigDecimal.ZERO;
    private LocalDate expectedSettlementDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Offboarding getOffboarding() { return offboarding; }
    public void setOffboarding(Offboarding offboarding) { this.offboarding = offboarding; }

    public BigDecimal getGratuity() { return gratuity; }
    public void setGratuity(BigDecimal gratuity) { this.gratuity = gratuity; }

    public BigDecimal getSeverance() { return severance; }
    public void setSeverance(BigDecimal severance) { this.severance = severance; }

    public BigDecimal getPendingSalary() { return pendingSalary; }
    public void setPendingSalary(BigDecimal pendingSalary) { this.pendingSalary = pendingSalary; }

    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }

    public BigDecimal getTotalSettlementAmount() { return totalSettlementAmount; }
    public void setTotalSettlementAmount(BigDecimal totalSettlementAmount) { this.totalSettlementAmount = totalSettlementAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public BigDecimal getLeaveEncashment() { return leaveEncashment; }
    public void setLeaveEncashment(BigDecimal leaveEncashment) { this.leaveEncashment = leaveEncashment; }

    public BigDecimal getReimbursements() { return reimbursements; }
    public void setReimbursements(BigDecimal reimbursements) { this.reimbursements = reimbursements; }

    public LocalDate getExpectedSettlementDate() { return expectedSettlementDate; }
    public void setExpectedSettlementDate(LocalDate expectedSettlementDate) { this.expectedSettlementDate = expectedSettlementDate; }
}
