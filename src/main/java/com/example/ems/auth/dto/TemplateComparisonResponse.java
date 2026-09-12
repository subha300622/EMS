package com.example.ems.auth.dto;

import java.util.List;

public class TemplateComparisonResponse {
    private String roleName;
    private boolean customized;
    private List<String> addedPermissions;
    private List<String> missingPermissions;

    public TemplateComparisonResponse() {}

    public TemplateComparisonResponse(String roleName, boolean customized, List<String> addedPermissions, List<String> missingPermissions) {
        this.roleName = roleName;
        this.customized = customized;
        this.addedPermissions = addedPermissions;
        this.missingPermissions = missingPermissions;
    }

    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }

    public boolean isCustomized() { return customized; }
    public void setCustomized(boolean customized) { this.customized = customized; }

    public List<String> getAddedPermissions() { return addedPermissions; }
    public void setAddedPermissions(List<String> addedPermissions) { this.addedPermissions = addedPermissions; }

    public List<String> getMissingPermissions() { return missingPermissions; }
    public void setMissingPermissions(List<String> missingPermissions) { this.missingPermissions = missingPermissions; }
}
