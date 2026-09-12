package com.example.ems.auth.dto;

import java.util.List;

public class OverrideRequest {
    private List<Long> permissionIds;
    private String reason;

    public OverrideRequest() {}

    public OverrideRequest(List<Long> permissionIds, String reason) {
        this.permissionIds = permissionIds;
        this.reason = reason;
    }

    public List<Long> getPermissionIds() { return permissionIds; }
    public void setPermissionIds(List<Long> permissionIds) { this.permissionIds = permissionIds; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
