package com.example.ems.payroll.statutory;

import java.math.BigDecimal;

public class PtCalculationResult {

    private boolean applicable = true;
    private String stateCode = "KA";
    private BigDecimal ptAmount = BigDecimal.ZERO;
    private String slabDescription;

    public PtCalculationResult() {}

    public PtCalculationResult(boolean applicable, String stateCode, BigDecimal ptAmount, String slabDescription) {
        this.applicable = applicable;
        this.stateCode = stateCode != null ? stateCode : "KA";
        this.ptAmount = ptAmount != null ? ptAmount : BigDecimal.ZERO;
        this.slabDescription = slabDescription;
    }

    public boolean isApplicable() {
        return applicable;
    }

    public void setApplicable(boolean applicable) {
        this.applicable = applicable;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public BigDecimal getPtAmount() {
        return ptAmount;
    }

    public void setPtAmount(BigDecimal ptAmount) {
        this.ptAmount = ptAmount;
    }

    public String getSlabDescription() {
        return slabDescription;
    }

    public void setSlabDescription(String slabDescription) {
        this.slabDescription = slabDescription;
    }
}
