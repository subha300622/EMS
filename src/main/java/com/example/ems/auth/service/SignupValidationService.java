package com.example.ems.auth.service;

import com.example.ems.auth.repository.UserRepository;
import com.example.ems.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class SignupValidationService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    private static final Set<String> RESERVED_SUBDOMAINS = new HashSet<>(Arrays.asList(
            "admin", "api", "mail", "www", "root", "support", "portal",
            "billing", "dev", "test", "prod", "ems", "system", "auth", "login"
    ));

    private static final Pattern PASSWORD_UPPER = Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_LOWER = Pattern.compile("[a-z]");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile("\\d");
    private static final Pattern PASSWORD_SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\",./<>?]");

    public SignupValidationService(UserRepository userRepository, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    public String normalizeEmail(String email) {
        if (email == null) return null;
        return email.trim().toLowerCase();
    }

    public String normalizePhone(String phone) {
        if (phone == null) return null;
        return phone.replaceAll("[^+\\d]", "");
    }

    public String normalizeOrgName(String name) {
        if (name == null) return null;
        return name.replaceAll("[\\s.,\\-_]", "").toLowerCase();
    }

    public boolean isReservedSubdomain(String subdomain) {
        if (subdomain == null) return true;
        return RESERVED_SUBDOMAINS.contains(subdomain.toLowerCase().trim());
    }

    public void validatePassword(String password, String fullName, String email, String phone, String orgName) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            throw new IllegalArgumentException("Password must be between 8 and 20 characters.");
        }
        if (!PASSWORD_UPPER.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter.");
        }
        if (!PASSWORD_LOWER.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter.");
        }
        if (!PASSWORD_DIGIT.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one number.");
        }
        if (!PASSWORD_SPECIAL.matcher(password).find()) {
            throw new IllegalArgumentException("Password must contain at least one special character.");
        }

        // Avoid common details
        String lowerPass = password.toLowerCase();
        if (fullName != null && !fullName.isBlank() && lowerPass.contains(fullName.toLowerCase().trim())) {
            throw new IllegalArgumentException("Password cannot contain your name.");
        }
        if (email != null && email.contains("@")) {
            String prefix = email.split("@")[0].trim().toLowerCase();
            if (!prefix.isBlank() && lowerPass.contains(prefix)) {
                throw new IllegalArgumentException("Password cannot contain part of your email address.");
            }
        }
        if (phone != null) {
            String digitsOnly = phone.replaceAll("\\D", "");
            if (digitsOnly.length() >= 4 && lowerPass.contains(digitsOnly)) {
                throw new IllegalArgumentException("Password cannot contain your phone number.");
            }
        }
        if (orgName != null && !orgName.isBlank() && lowerPass.contains(orgName.toLowerCase().trim())) {
            throw new IllegalArgumentException("Password cannot contain your organization name.");
        }
    }

    public void validateUniqueUser(String email, String phone) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByWorkEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already exists.");
        }

        String normalizedPhone = normalizePhone(phone);
        if (userRepository.existsByMobileNumber(normalizedPhone)) {
            throw new IllegalArgumentException("Mobile number already exists.");
        }
    }

    public void validateUniqueOrganization(String orgName) {
        String normalizedOrg = normalizeOrgName(orgName);
        if (organizationRepository.existsByNormalizedName(normalizedOrg)) {
            throw new IllegalArgumentException("Organization already exists.");
        }
    }

    public void validateGst(String gst, String country) {
        if ("India".equalsIgnoreCase(country) && gst != null && !gst.isBlank()) {
            Pattern gstPattern = Pattern.compile("\\d{2}[A-Z]{5}\\d{4}[A-Z]{1}[A-Z\\d]{1}[Z]{1}[A-Z\\d]{1}");
            if (!gstPattern.matcher(gst).matches()) {
                throw new IllegalArgumentException("Invalid GST format for India.");
            }
        }
    }
}
