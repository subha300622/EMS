package com.example.ems.payroll.dto;

import java.math.BigDecimal;

public class TaxSummaryResponse {

    private String financialYear;
    private TaxDetailsInfo taxDetails;

    public TaxSummaryResponse() {}

    public TaxSummaryResponse(String financialYear, TaxDetailsInfo taxDetails) {
        this.financialYear = financialYear;
        this.taxDetails = taxDetails;
    }

    public String getFinancialYear() { return financialYear; }
    public void setFinancialYear(String financialYear) { this.financialYear = financialYear; }

    public TaxDetailsInfo getTaxDetails() { return taxDetails; }
    public void setTaxDetails(TaxDetailsInfo taxDetails) { this.taxDetails = taxDetails; }

    public static class TaxDetailsInfo {
        private BigDecimal incomeTaxDeducted;
        private BigDecimal providentFundContribution;
        private BigDecimal professionalTax;

        public TaxDetailsInfo() {}

        public TaxDetailsInfo(BigDecimal incomeTaxDeducted, BigDecimal providentFundContribution, BigDecimal professionalTax) {
            this.incomeTaxDeducted = incomeTaxDeducted;
            this.providentFundContribution = providentFundContribution;
            this.professionalTax = professionalTax;
        }

        public BigDecimal getIncomeTaxDeducted() { return incomeTaxDeducted; }
        public void setIncomeTaxDeducted(BigDecimal incomeTaxDeducted) { this.incomeTaxDeducted = incomeTaxDeducted; }

        public BigDecimal getProvidentFundContribution() { return providentFundContribution; }
        public void setProvidentFundContribution(BigDecimal providentFundContribution) { this.providentFundContribution = providentFundContribution; }

        public BigDecimal getProfessionalTax() { return professionalTax; }
        public void setProfessionalTax(BigDecimal professionalTax) { this.professionalTax = professionalTax; }
    }
}
