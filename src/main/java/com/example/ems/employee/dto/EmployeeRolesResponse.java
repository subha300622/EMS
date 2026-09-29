package com.example.ems.employee.dto;

import java.util.List;

public class EmployeeRolesResponse {
    private Long employeeId;
    private List<EmployeeRoleDto> roles;

    public EmployeeRolesResponse() {}
    public EmployeeRolesResponse(Long employeeId, List<EmployeeRoleDto> roles) {
        this.employeeId = employeeId;
        this.roles = roles;
    }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public List<EmployeeRoleDto> getRoles() { return roles; }
    public void setRoles(List<EmployeeRoleDto> roles) { this.roles = roles; }

    public static class EmployeeRoleDto {
        private Long roleId;
        private String roleName;
        private String status;

        public EmployeeRoleDto() {}
        public EmployeeRoleDto(Long roleId, String roleName, String status) {
            this.roleId = roleId;
            this.roleName = roleName;
            this.status = status;
        }

        public Long getRoleId() { return roleId; }
        public void setRoleId(Long roleId) { this.roleId = roleId; }

        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
