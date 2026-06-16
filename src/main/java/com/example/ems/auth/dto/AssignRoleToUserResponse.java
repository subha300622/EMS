package com.example.ems.auth.dto;

public class AssignRoleToUserResponse {

    private String userId;
    private String role;

    public AssignRoleToUserResponse() {}

    public AssignRoleToUserResponse(String userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
