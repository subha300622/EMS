package com.example.ems.auth.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public class DeleteRoleResponse {

    @Schema(example = "Software Engineer")
    private Long roleId;
    @Schema(example = "true")
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
