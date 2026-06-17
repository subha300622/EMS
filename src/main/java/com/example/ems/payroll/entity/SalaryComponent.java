package com.example.ems.payroll.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "salary_components")
public class SalaryComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String type; // EARNING, DEDUCTION

    private BigDecimal percentageOfBasic;

    private BigDecimal fixedAmount;

    @Column(nullable = false)
    private Boolean taxable = true;

    public SalaryComponent() {}

    public SalaryComponent(String name, String type, BigDecimal percentageOfBasic, BigDecimal fixedAmount, Boolean taxable) {
        this.name = name;
        this.type = type;
        this.percentageOfBasic = percentageOfBasic;
        this.fixedAmount = fixedAmount;
        this.taxable = taxable;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getPercentageOfBasic() { return percentageOfBasic; }
    public void setPercentageOfBasic(BigDecimal percentageOfBasic) { this.percentageOfBasic = percentageOfBasic; }

    public BigDecimal getFixedAmount() { return fixedAmount; }
    public void setFixedAmount(BigDecimal fixedAmount) { this.fixedAmount = fixedAmount; }

    public Boolean getTaxable() { return taxable; }
    public void setTaxable(Boolean taxable) { this.taxable = taxable; }
}
