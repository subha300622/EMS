package com.example.ems.organization.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateOrganizationRequest {
    @NotBlank(message = "Organization name is required")
    private String name;

    @Email(message = "Invalid email address format")
    @NotBlank(message = "Admin email is required")
    private String email;

    private String phone;
    private String website;

    @NotBlank(message = "Subscription plan is required")
    private String subscriptionPlan;

    @Valid
    @NotNull(message = "Address is required")
    private OrganizationAddressDto address;

    public CreateOrganizationRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public OrganizationAddressDto getAddress() { return address; }
    public void setAddress(OrganizationAddressDto address) { this.address = address; }
}
