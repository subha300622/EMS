package com.example.ems.appraisal.dto;

public class CycleEligibilityCriteriaDto {
    private Integer minimumServiceMonths;
    private String department;
    private String designation;

    public Integer getMinimumServiceMonths() { return minimumServiceMonths; }
    public void setMinimumServiceMonths(Integer minimumServiceMonths) { this.minimumServiceMonths = minimumServiceMonths; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
}
