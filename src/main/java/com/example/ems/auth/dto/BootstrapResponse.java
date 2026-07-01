package com.example.ems.auth.dto;

import java.util.List;

public class BootstrapResponse {
    private UserProfileResponse user;
    private AuthDataResponse auth;
    private OrgContextResponse org;

    public BootstrapResponse() {}

    public BootstrapResponse(UserProfileResponse user, AuthDataResponse auth, OrgContextResponse org) {
        this.user = user;
        this.auth = auth;
        this.org = org;
    }

    public UserProfileResponse getUser() { return user; }
    public void setUser(UserProfileResponse user) { this.user = user; }

    public AuthDataResponse getAuth() { return auth; }
    public void setAuth(AuthDataResponse auth) { this.auth = auth; }

    public OrgContextResponse getOrg() { return org; }
    public void setOrg(OrgContextResponse org) { this.org = org; }

    public static class UserProfileResponse {
        private Long id;
        private String employeeId;
        private String name;
        private String email;
        private String role;
        private String profileImage;
        private boolean mustChangePassword;
        private boolean mfaRequired;
        private String status;
        private String lastLogin;        private String organizationName;
        private String branch;

        public UserProfileResponse() {}

        public UserProfileResponse(Long id, String employeeId, String name, String email, String role, String profileImage, boolean mustChangePassword, boolean mfaRequired, String status, String lastLogin) {
            this.id = id;
            this.employeeId = employeeId;
            this.name = name;
            this.email = email;
            this.role = role;
            this.profileImage = profileImage;
            this.mustChangePassword = mustChangePassword;
            this.mfaRequired = mfaRequired;
            this.status = status;
            this.lastLogin = lastLogin;
        }

        public UserProfileResponse(Long id, String employeeId, String name, String email, String role, String profileImage, boolean mustChangePassword, boolean mfaRequired, String status, String lastLogin, String organizationName, String branch) {
            this(id, employeeId, name, email, role, profileImage, mustChangePassword, mfaRequired, status, lastLogin);
            this.organizationName = organizationName;
            this.branch = branch;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

        public boolean isMustChangePassword() { return mustChangePassword; }
        public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }

        public boolean isMfaRequired() { return mfaRequired; }
        public void setMfaRequired(boolean mfaRequired) { this.mfaRequired = mfaRequired; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getLastLogin() { return lastLogin; }
        public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

        public String getBranch() { return branch; }
        public void setBranch(String branch) { this.branch = branch; }
    }

    public static class AuthDataResponse {
        private List<String> permissions;

        public AuthDataResponse() {}

        public AuthDataResponse(List<String> permissions) {
            this.permissions = permissions;
        }

        public List<String> getPermissions() { return permissions; }
        public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    }

    public static class OrgContextResponse {
        private Long companyId;
        private Long departmentId;
        private LoginResponse.BranchContext branch;

        public OrgContextResponse() {}

        public OrgContextResponse(Long companyId, Long departmentId, LoginResponse.BranchContext branch) {
            this.companyId = companyId;
            this.departmentId = departmentId;
            this.branch = branch;
        }

        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }

        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        public LoginResponse.BranchContext getBranch() { return branch; }
        public void setBranch(LoginResponse.BranchContext branch) { this.branch = branch; }
    }
}
