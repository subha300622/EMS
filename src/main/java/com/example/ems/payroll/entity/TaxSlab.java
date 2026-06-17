package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tax_slabs")
public class TaxSlab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String regime; // OLD, NEW

    @Column(nullable = false)
    private BigDecimal minIncome;

    private BigDecimal maxIncome;

    @Column(nullable = false)
    private BigDecimal taxRate; // percentage (e.g. 5.0, 10.0)

    public TaxSlab() {}

    public TaxSlab(String regime, BigDecimal minIncome, BigDecimal maxIncome, BigDecimal taxRate) {
        this.regime = regime;
        this.minIncome = minIncome;
        this.maxIncome = maxIncome;
        this.taxRate = taxRate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRegime() { return regime; }
    public void setRegime(String regime) { this.regime = regime; }

    public BigDecimal getMinIncome() { return minIncome; }
    public void setMinIncome(BigDecimal minIncome) { this.minIncome = minIncome; }

    public BigDecimal getMaxIncome() { return maxIncome; }
    public void setMaxIncome(BigDecimal maxIncome) { this.maxIncome = maxIncome; }

    public BigDecimal getTaxRate() { return taxRate; }
    public void setTaxRate(BigDecimal taxRate) { this.taxRate = taxRate; }
}
