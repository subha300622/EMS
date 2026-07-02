package com.example.ems.auth.dto;

import java.util.List;

public class BulkDeleteRequest {
    private List<Long> roleIds;
    private String reason;

    public BulkDeleteRequest() {}

    public BulkDeleteRequest(List<Long> roleIds, String reason) {
        this.roleIds = roleIds;
        this.reason = reason;
    }

    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
