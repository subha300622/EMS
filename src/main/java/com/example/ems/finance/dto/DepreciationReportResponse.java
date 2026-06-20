package com.example.ems.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public class DepreciationReportResponse {
    private String financialYear;
    private BigDecimal totalDepreciation;
    private List<CategoryDepreciationItem> categories;

    public DepreciationReportResponse() {}

    public DepreciationReportResponse(String financialYear, BigDecimal totalDepreciation, List<CategoryDepreciationItem> categories) {
        this.financialYear = financialYear;
        this.totalDepreciation = totalDepreciation;
        this.categories = categories;
    }

    public String getFinancialYear() {
        return financialYear;
    }

    public void setFinancialYear(String financialYear) {
        this.financialYear = financialYear;
    }

    public BigDecimal getTotalDepreciation() {
        return totalDepreciation;
    }

    public void setTotalDepreciation(BigDecimal totalDepreciation) {
        this.totalDepreciation = totalDepreciation;
    }

    public List<CategoryDepreciationItem> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDepreciationItem> categories) {
        this.categories = categories;
    }
}
