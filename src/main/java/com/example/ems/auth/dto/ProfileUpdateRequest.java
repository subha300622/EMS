package com.example.ems.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for PUT /api/v1/me/profile.
 * Only self-service editable fields are exposed here.
 * Admin-only fields (fullName, department, designation, salary, etc.) are intentionally excluded.
 */
@Schema(description = "Self-service profile update. All fields are optional — only supplied fields are updated.")
public class ProfileUpdateRequest {

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be 10–15 digits, optionally starting with '+'")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Schema(example = "+919876543210", description = "Personal phone number (10–15 digits)")
    private String phone;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Schema(example = "42, MG Road, Bangalore 560001", description = "Residential address")
    private String address;

    @Size(max = 100, message = "Emergency contact must not exceed 100 characters")
    @Schema(example = "Jane Doe - +919876543211", description = "Emergency contact name and number")
    private String emergencyContact;

    @Size(max = 1000, message = "Profile image URL must not exceed 1000 characters")
    @Schema(example = "https://cdn.example.com/avatars/john.jpg", description = "URL of the profile image")
    private String profileImage;

    @Schema(example = "Google", description = "Organization name")
    private String organizationName;

    @Schema(example = "Main Branch", description = "Branch location")
    private String branch;

    // --- Getters and Setters ---

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    /**
     * Returns true if at least one field was supplied in the request.
     */
    public boolean hasAnyUpdate() {
        return phone != null || address != null || emergencyContact != null || profileImage != null || organizationName != null || branch != null;
    }
}
