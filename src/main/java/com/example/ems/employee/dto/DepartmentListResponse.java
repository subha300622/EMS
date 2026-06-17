package com.example.ems.employee.dto;

import java.util.List;

public class DepartmentListResponse {
    private List<DepartmentItemDto> departments;

    public DepartmentListResponse() {}

    public DepartmentListResponse(List<DepartmentItemDto> departments) {
        this.departments = departments;
    }

    public List<DepartmentItemDto> getDepartments() { return departments; }
    public void setDepartments(List<DepartmentItemDto> departments) { this.departments = departments; }

    public static class DepartmentItemDto {
        private Long departmentId;
        private String name;
        private long employeeCount;

        public DepartmentItemDto() {}

        public DepartmentItemDto(Long departmentId, String name, long employeeCount) {
            this.departmentId = departmentId;
            this.name = name;
            this.employeeCount = employeeCount;
        }

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public long getEmployeeCount() { return employeeCount; }
        public void setEmployeeCount(long employeeCount) { this.employeeCount = employeeCount; }
    }
}
