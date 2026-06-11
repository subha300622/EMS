package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payslips")
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payroll_id", nullable = false)
    private Payroll payroll;

    @Column(unique = true, nullable = false)
    private String payslipNumber;

    private LocalDateTime generatedAt = LocalDateTime.now();

    public Payslip() {}

    public Payslip(Long id, Payroll payroll, String payslipNumber, LocalDateTime generatedAt) {
        this.id = id;
        this.payroll = payroll;
        this.payslipNumber = payslipNumber;
        this.generatedAt = generatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Payroll getPayroll() {
        return payroll;
    }

    public void setPayroll(Payroll payroll) {
        this.payroll = payroll;
    }

    public String getPayslipNumber() {
        return payslipNumber;
    }

    public void setPayslipNumber(String payslipNumber) {
        this.payslipNumber = payslipNumber;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
