package com.example.ems.finance.dto.manager;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.math.BigDecimal;

@Schema(description = "Manager Payroll Summary metrics (accessible with payroll/salary view permission)")
public class ManagerPayrollSummaryDto implements Serializable {

    @Schema(description = "Number of employees in payroll scope", example = "24")
    private Integer employees;

    @Schema(description = "Total monthly gross payroll amount", example = "1850000")
    private BigDecimal totalMonthlyGross;

    @Schema(description = "Status of the latest payroll cycle", example = "PROCESSED")
    private String lastPayrollStatus;

    public ManagerPayrollSummaryDto() {}

    public ManagerPayrollSummaryDto(Integer employees, BigDecimal totalMonthlyGross, String lastPayrollStatus) {
        this.employees = employees;
        this.totalMonthlyGross = totalMonthlyGross;
        this.lastPayrollStatus = lastPayrollStatus;
    }

    public Integer getEmployees() {
        return employees;
    }

    public void setEmployees(Integer employees) {
        this.employees = employees;
    }

    public BigDecimal getTotalMonthlyGross() {
        return totalMonthlyGross;
    }

    public void setTotalMonthlyGross(BigDecimal totalMonthlyGross) {
        this.totalMonthlyGross = totalMonthlyGross;
    }

    public String getLastPayrollStatus() {
        return lastPayrollStatus;
    }

    public void setLastPayrollStatus(String lastPayrollStatus) {
        this.lastPayrollStatus = lastPayrollStatus;
    }
}
