package com.example.ems.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;

import java.util.List;

public class UserManagementDtos {

    public static class RoleItemDto {
        @Schema(example = "ROLE-001")
        private String roleId;
        @Schema(example = "HR_MANAGER")
        private String roleName;

        public RoleItemDto() {}

        public RoleItemDto(String roleId, String roleName) {
            this.roleId = roleId;
            this.roleName = roleName;
        }

        public String getRoleId() {
            return roleId;
        }

        public void setRoleId(String roleId) {
            this.roleId = roleId;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }
    }

    public static class UserDetailResponse {
        @Schema(example = "USR-1001")
        private String userId;
        @Schema(example = "EMP-1001")
        private String employeeId;
        @Schema(example = "ORG-001")
        private String organizationId;
        @Schema(example = "ABC Hospital")
        private String organizationName;
        @Schema(example = "John Doe")
        private String fullName;
        @Schema(example = "john@abc.com")
        private String email;
        @Schema(example = "+919876543210")
        private String mobile;
        @Schema(example = "ACTIVE")
        private String status;
        private List<RoleItemDto> roles;

        public UserDetailResponse() {}

        public UserDetailResponse(String userId, String employeeId, String organizationId, String organizationName,
                                  String fullName, String email, String mobile, String status, List<RoleItemDto> roles) {
            this.userId = userId;
            this.employeeId = employeeId;
            this.organizationId = organizationId;
            this.organizationName = organizationName;
            this.fullName = fullName;
            this.email = email;
            this.mobile = mobile;
            this.status = status;
            this.roles = roles;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<RoleItemDto> getRoles() { return roles; }
        public void setRoles(List<RoleItemDto> roles) { this.roles = roles; }
    }

    public static class UpdateUserRequest {
        @Schema(example = "John Michael Doe")
        private String fullName;
        @Email(message = "Invalid email format")
        @Schema(example = "john.doe@abc.com")
        private String email;
        @Schema(example = "+919876543210")
        private String mobile;

        public UpdateUserRequest() {}

        public UpdateUserRequest(String fullName, String email, String mobile) {
            this.fullName = fullName;
            this.email = email;
            this.mobile = mobile;
        }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
    }

    public static class UpdatedUserDataResponse {
        @Schema(example = "USR-1001")
        private String userId;
        @Schema(example = "John Michael Doe")
        private String fullName;
        @Schema(example = "john.doe@abc.com")
        private String email;
        @Schema(example = "+919876543210")
        private String mobile;

        public UpdatedUserDataResponse() {}

        public UpdatedUserDataResponse(String userId, String fullName, String email, String mobile) {
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
            this.mobile = mobile;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
    }

    public static class ResetPasswordAdminRequest {
        @Schema(example = "true")
        private Boolean sendTemporaryPassword;
        @Schema(example = "true")
        private Boolean forceChangeOnNextLogin;
        private String newPassword;
        private String confirmPassword;

        public ResetPasswordAdminRequest() {}

        public Boolean getSendTemporaryPassword() { return sendTemporaryPassword; }
        public void setSendTemporaryPassword(Boolean sendTemporaryPassword) { this.sendTemporaryPassword = sendTemporaryPassword; }
        public Boolean getForceChangeOnNextLogin() { return forceChangeOnNextLogin; }
        public void setForceChangeOnNextLogin(Boolean forceChangeOnNextLogin) { this.forceChangeOnNextLogin = forceChangeOnNextLogin; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
        public String getConfirmPassword() { return confirmPassword; }
        public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    }

    public static class UpdateRoleRequest {
        @Schema(example = "ROLE-001")
        private String roleId;

        public UpdateRoleRequest() {}

        public UpdateRoleRequest(String roleId) {
            this.roleId = roleId;
        }

        public String getRoleId() { return roleId; }
        public void setRoleId(String roleId) { this.roleId = roleId; }
    }

    public static class UserRoleUpdatedDataResponse {
        @Schema(example = "USR-1001")
        private String userId;
        private RoleItemDto role;

        public UserRoleUpdatedDataResponse() {}

        public UserRoleUpdatedDataResponse(String userId, RoleItemDto role) {
            this.userId = userId;
            this.role = role;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public RoleItemDto getRole() { return role; }
        public void setRole(RoleItemDto role) { this.role = role; }
    }

    public static class UserRolesResponse {
        @Schema(example = "USR-1001")
        private String userId;
        private List<RoleItemDto> roles;

        public UserRolesResponse() {}

        public UserRolesResponse(String userId, List<RoleItemDto> roles) {
            this.userId = userId;
            this.roles = roles;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public List<RoleItemDto> getRoles() { return roles; }
        public void setRoles(List<RoleItemDto> roles) { this.roles = roles; }
    }

    public static class AssignMultipleRolesRequest {
        @Schema(example = "[\"ROLE-001\", \"ROLE-002\"]")
        private List<String> roleIds;

        public AssignMultipleRolesRequest() {}

        public AssignMultipleRolesRequest(List<String> roleIds) {
            this.roleIds = roleIds;
        }

        public List<String> getRoleIds() { return roleIds; }
        public void setRoleIds(List<String> roleIds) { this.roleIds = roleIds; }
    }

    public static class UpdateStatusRequest {
        @Schema(example = "SUSPENDED")
        private String status;
        @Schema(example = "Employee suspended by administrator")
        private String reason;

        public UpdateStatusRequest() {}

        public UpdateStatusRequest(String status, String reason) {
            this.status = status;
            this.reason = reason;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class UserStatusUpdatedDataResponse {
        @Schema(example = "USR-1001")
        private String userId;
        @Schema(example = "SUSPENDED")
        private String status;

        public UserStatusUpdatedDataResponse() {}

        public UserStatusUpdatedDataResponse(String userId, String status) {
            this.userId = userId;
            this.status = status;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class OrgSummaryDto {
        @Schema(example = "ORG-001")
        private String organizationId;
        @Schema(example = "ABC Hospital")
        private String organizationName;

        public OrgSummaryDto() {}

        public OrgSummaryDto(String organizationId, String organizationName) {
            this.organizationId = organizationId;
            this.organizationName = organizationName;
        }

        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    }

    public static class UserSummaryDto {
        @Schema(example = "USR-1001")
        private String userId;
        @Schema(example = "EMP-1001")
        private String employeeId;
        @Schema(example = "John Doe")
        private String fullName;
        @Schema(example = "john@abc.com")
        private String email;
        @Schema(example = "+919876543210")
        private String mobile;
        @Schema(example = "ACTIVE")
        private String status;

        public UserSummaryDto() {}

        public UserSummaryDto(String userId, String employeeId, String fullName, String email, String mobile, String status) {
            this.userId = userId;
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.email = email;
            this.mobile = mobile;
            this.status = status;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class BootstrapDataResponse {
        private UserSummaryDto user;
        private OrgSummaryDto organization;
        private List<RoleItemDto> roles;

        public BootstrapDataResponse() {}

        public BootstrapDataResponse(UserSummaryDto user, OrgSummaryDto organization, List<RoleItemDto> roles) {
            this.user = user;
            this.organization = organization;
            this.roles = roles;
        }

        public UserSummaryDto getUser() { return user; }
        public void setUser(UserSummaryDto user) { this.user = user; }
        public OrgSummaryDto getOrganization() { return organization; }
        public void setOrganization(OrgSummaryDto organization) { this.organization = organization; }
        public List<RoleItemDto> getRoles() { return roles; }
        public void setRoles(List<RoleItemDto> roles) { this.roles = roles; }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public static class ContextDataResponse {
        @Schema(example = "USR-1001")
        private String userId;
        @Schema(example = "ORG-001")
        private String organizationId;
        @Schema(example = "ABC Hospital")
        private String organizationName;
        @Schema(example = "ORGANIZATION")
        private String scope;

        public ContextDataResponse() {}

        public ContextDataResponse(String userId, String organizationId, String organizationName, String scope) {
            this.userId = userId;
            this.organizationId = organizationId;
            this.organizationName = organizationName;
            this.scope = scope;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
        public String getScope() { return scope; }
        public void setScope(String scope) { this.scope = scope; }
    }

    public static class UserProfileResponse {
        @Schema(example = "USR-1001")
        private String userId;
        @Schema(example = "EMP-1001")
        private String employeeId;
        @Schema(example = "John Doe")
        private String fullName;
        @Schema(example = "john@abc.com")
        private String email;
        @Schema(example = "+919876543210")
        private String mobile;
        private String profileImageUrl;
        private OrgSummaryDto organization;
        private List<RoleItemDto> roles;

        public UserProfileResponse() {}

        public UserProfileResponse(String userId, String employeeId, String fullName, String email, String mobile,
                                   String profileImageUrl, OrgSummaryDto organization, List<RoleItemDto> roles) {
            this.userId = userId;
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.email = email;
            this.mobile = mobile;
            this.profileImageUrl = profileImageUrl;
            this.organization = organization;
            this.roles = roles;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getProfileImageUrl() { return profileImageUrl; }
        public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
        public OrgSummaryDto getOrganization() { return organization; }
        public void setOrganization(OrgSummaryDto organization) { this.organization = organization; }
        public List<RoleItemDto> getRoles() { return roles; }
        public void setRoles(List<RoleItemDto> roles) { this.roles = roles; }
    }

    public static class PendingUserDto {
        @Schema(example = "USR-1005")
        private String userId;
        @Schema(example = "Jane Doe")
        private String fullName;
        @Schema(example = "jane@abc.com")
        private String email;
        @Schema(example = "PENDING")
        private String status;
        @Schema(example = "2026-08-18T10:30:00Z")
        private String createdAt;

        public PendingUserDto() {}

        public PendingUserDto(String userId, String fullName, String email, String status, String createdAt) {
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
            this.status = status;
            this.createdAt = createdAt;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    }

    public static class SearchUserItemDto {
        @Schema(example = "USR-1001")
        private String userId;
        @Schema(example = "EMP-1001")
        private String employeeId;
        @Schema(example = "John Doe")
        private String fullName;
        @Schema(example = "john@abc.com")
        private String email;
        @Schema(example = "ACTIVE")
        private String status;
        private List<RoleItemDto> roles;

        public SearchUserItemDto() {}

        public SearchUserItemDto(String userId, String employeeId, String fullName, String email, String status, List<RoleItemDto> roles) {
            this.userId = userId;
            this.employeeId = employeeId;
            this.fullName = fullName;
            this.email = email;
            this.status = status;
            this.roles = roles;
        }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<RoleItemDto> getRoles() { return roles; }
        public void setRoles(List<RoleItemDto> roles) { this.roles = roles; }
    }

    public static class PaginatedSearchResponse {
        private List<SearchUserItemDto> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;

        public PaginatedSearchResponse() {}

        public PaginatedSearchResponse(List<SearchUserItemDto> content, int page, int size, long totalElements, int totalPages) {
            this.content = content;
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public List<SearchUserItemDto> getContent() { return content; }
        public void setContent(List<SearchUserItemDto> content) { this.content = content; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public long getTotalElements() { return totalElements; }
        public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
        public int getTotalPages() { return totalPages; }
        public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    }
}
