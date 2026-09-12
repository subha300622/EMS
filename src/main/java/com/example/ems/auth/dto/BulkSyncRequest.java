package com.example.ems.auth.dto;

import java.util.List;

public class BulkSyncRequest {
    private List<Long> roleIds;
    private String reason;

    public BulkSyncRequest() {}

    public BulkSyncRequest(List<Long> roleIds, String reason) {
        this.roleIds = roleIds;
        this.reason = reason;
    }

    public List<Long> getRoleIds() { return roleIds; }
    public void setRoleIds(List<Long> roleIds) { this.roleIds = roleIds; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
