package com.example.ems.auth.dto;

public class DeleteRoleResponse {

    private Long roleId;
    private boolean deleted;

    public DeleteRoleResponse() {}

    public DeleteRoleResponse(Long roleId, boolean deleted) {
        this.roleId = roleId;
        this.deleted = deleted;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
