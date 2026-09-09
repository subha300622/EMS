package com.example.ems.increment.eligibility;

import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.employee.entity.Employee;
import com.example.ems.increment.entity.IncrementCycle;
import com.example.ems.increment.entity.IncrementPolicy;

import java.math.BigDecimal;

public class EligibilityContext {

    private final Employee employee;
    private final Appraisal appraisal;
    private final IncrementCycle cycle;
    private final IncrementPolicy policy;
    private final BigDecimal currentSalary;
    private final BigDecimal allocatedBudget;
    private final Double proposedIncrementPercentage;
    private final boolean disciplinaryClear;

    public EligibilityContext(Employee employee, Appraisal appraisal, IncrementCycle cycle, IncrementPolicy policy,
                              BigDecimal currentSalary, BigDecimal allocatedBudget, Double proposedIncrementPercentage,
                              boolean disciplinaryClear) {
        this.employee = employee;
        this.appraisal = appraisal;
        this.cycle = cycle;
        this.policy = policy;
        this.currentSalary = currentSalary != null ? currentSalary : BigDecimal.ZERO;
        this.allocatedBudget = allocatedBudget != null ? allocatedBudget : BigDecimal.ZERO;
        this.proposedIncrementPercentage = proposedIncrementPercentage;
        this.disciplinaryClear = disciplinaryClear;
    }

    public Employee getEmployee() { return employee; }
    public Appraisal getAppraisal() { return appraisal; }
    public IncrementCycle getCycle() { return cycle; }
    public IncrementPolicy getPolicy() { return policy; }
    public BigDecimal getCurrentSalary() { return currentSalary; }
    public BigDecimal getAllocatedBudget() { return allocatedBudget; }
    public Double getProposedIncrementPercentage() { return proposedIncrementPercentage; }
    public boolean isDisciplinaryClear() { return disciplinaryClear; }
}
