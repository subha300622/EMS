package com.example.ems.employee.dto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class EmployeeHierarchyResponse {
    private EmployeeRefDto employee;
    private EmployeeRefDto manager;
    private List<EmployeeRefDto> reportees;

    public EmployeeHierarchyResponse() {}

    public EmployeeHierarchyResponse(EmployeeRefDto employee, EmployeeRefDto manager, List<EmployeeRefDto> reportees) {
        this.employee = employee;
        this.manager = manager;
        this.reportees = reportees;
    }

    public EmployeeRefDto getEmployee() { return employee; }
    public void setEmployee(EmployeeRefDto employee) { this.employee = employee; }

    public EmployeeRefDto getManager() { return manager; }
    public void setManager(EmployeeRefDto manager) { this.manager = manager; }

    public List<EmployeeRefDto> getReportees() { return reportees; }
    public void setReportees(List<EmployeeRefDto> reportees) { this.reportees = reportees; }

    public static class EmployeeRefDto {
        @Schema(example = "1")
        private Long employeeId;
        @Schema(example = "string")
        private String name;

        public EmployeeRefDto() {}

        public EmployeeRefDto(Long employeeId, String name) {
            this.employeeId = employeeId;
            this.name = name;
        }

        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
