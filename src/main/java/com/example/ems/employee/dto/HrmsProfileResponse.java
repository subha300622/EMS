package com.example.ems.employee.dto;

import java.util.List;

/**
 * Production-grade HRMS profile response DTO.
 * Covers: employee info, org hierarchy, contact, personalInfo,
 * skills, systemAccess, exitSummary, and audit.
 */
public class HrmsProfileResponse {

    private EmployeeSection employee;
    private OrganizationSection organization;
    private ContactSection contact;
    private PersonalInfoSection personalInfo;
    private SkillsSection skills;
    private SystemAccessSection systemAccess;
    private ExitSummarySection exitSummary;
    private AuditSection audit;

    public HrmsProfileResponse() {}

    // --- Getters / Setters ---

    public EmployeeSection getEmployee() { return employee; }
    public void setEmployee(EmployeeSection employee) { this.employee = employee; }

    public OrganizationSection getOrganization() { return organization; }
    public void setOrganization(OrganizationSection organization) { this.organization = organization; }

    public ContactSection getContact() { return contact; }
    public void setContact(ContactSection contact) { this.contact = contact; }

    public PersonalInfoSection getPersonalInfo() { return personalInfo; }
    public void setPersonalInfo(PersonalInfoSection personalInfo) { this.personalInfo = personalInfo; }

    public SkillsSection getSkills() { return skills; }
    public void setSkills(SkillsSection skills) { this.skills = skills; }

    public SystemAccessSection getSystemAccess() { return systemAccess; }
    public void setSystemAccess(SystemAccessSection systemAccess) { this.systemAccess = systemAccess; }

    public ExitSummarySection getExitSummary() { return exitSummary; }
    public void setExitSummary(ExitSummarySection exitSummary) { this.exitSummary = exitSummary; }

    public AuditSection getAudit() { return audit; }
    public void setAudit(AuditSection audit) { this.audit = audit; }

    // ─────────────────────────────────────────────
    // SECTION: employee
    // ─────────────────────────────────────────────
    public static class EmployeeSection {
        private Long id;
        private String code;
        private String fullName;
        private String designation;
        private String department;
        private String employmentStatus;
        private String employmentType;
        private String profileImage;
        private String workMode;
        private String location;
        private String joiningDate;
        private String probationStatus;

        public EmployeeSection() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getEmploymentStatus() { return employmentStatus; }
        public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }

        public String getEmploymentType() { return employmentType; }
        public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

        public String getWorkMode() { return workMode; }
        public void setWorkMode(String workMode) { this.workMode = workMode; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public String getJoiningDate() { return joiningDate; }
        public void setJoiningDate(String joiningDate) { this.joiningDate = joiningDate; }

        public String getProbationStatus() { return probationStatus; }
        public void setProbationStatus(String probationStatus) { this.probationStatus = probationStatus; }
    }

    // ─────────────────────────────────────────────
    // SECTION: organization
    // ─────────────────────────────────────────────
    public static class OrganizationSection {
        private ManagerDto manager;
        private List<String> reportingChain;
        private int teamSize;

        public OrganizationSection() {}

        public ManagerDto getManager() { return manager; }
        public void setManager(ManagerDto manager) { this.manager = manager; }

        public List<String> getReportingChain() { return reportingChain; }
        public void setReportingChain(List<String> reportingChain) { this.reportingChain = reportingChain; }

        public int getTeamSize() { return teamSize; }
        public void setTeamSize(int teamSize) { this.teamSize = teamSize; }
    }

    public static class ManagerDto {
        private Long id;
        private String name;
        private String designation;
        private String email;

        public ManagerDto() {}

        public ManagerDto(Long id, String name, String designation, String email) {
            this.id = id;
            this.name = name;
            this.designation = designation;
            this.email = email;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDesignation() { return designation; }
        public void setDesignation(String designation) { this.designation = designation; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // ─────────────────────────────────────────────
    // SECTION: contact
    // ─────────────────────────────────────────────
    public static class ContactSection {
        private String email;
        private String phone;
        private EmergencyContactDto emergencyContact;

        public ContactSection() {}

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public EmergencyContactDto getEmergencyContact() { return emergencyContact; }
        public void setEmergencyContact(EmergencyContactDto emergencyContact) { this.emergencyContact = emergencyContact; }
    }

    public static class EmergencyContactDto {
        private String name;
        private String phone;
        private String relation;

        public EmergencyContactDto() {}

        public EmergencyContactDto(String name, String phone, String relation) {
            this.name = name;
            this.phone = phone;
            this.relation = relation;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getRelation() { return relation; }
        public void setRelation(String relation) { this.relation = relation; }
    }

    // ─────────────────────────────────────────────
    // SECTION: personalInfo
    // ─────────────────────────────────────────────
    public static class PersonalInfoSection {
        private String gender;
        private String dateOfBirth;
        private AddressDto address;

        public PersonalInfoSection() {}

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

        public AddressDto getAddress() { return address; }
        public void setAddress(AddressDto address) { this.address = address; }
    }

    public static class AddressDto {
        private String line1;
        private String city;
        private String state;
        private String postalCode;
        private String country;

        public AddressDto() {}

        public AddressDto(String line1, String city, String state, String postalCode, String country) {
            this.line1 = line1;
            this.city = city;
            this.state = state;
            this.postalCode = postalCode;
            this.country = country;
        }

        public String getLine1() { return line1; }
        public void setLine1(String line1) { this.line1 = line1; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public String getPostalCode() { return postalCode; }
        public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
    }

    // ─────────────────────────────────────────────
    // SECTION: skills
    // ─────────────────────────────────────────────
    public static class SkillsSection {
        private List<String> primary;
        private List<String> secondary;
        private List<String> certifications;

        public SkillsSection() {}

        public List<String> getPrimary() { return primary; }
        public void setPrimary(List<String> primary) { this.primary = primary; }

        public List<String> getSecondary() { return secondary; }
        public void setSecondary(List<String> secondary) { this.secondary = secondary; }

        public List<String> getCertifications() { return certifications; }
        public void setCertifications(List<String> certifications) { this.certifications = certifications; }
    }

    // ─────────────────────────────────────────────
    // SECTION: systemAccess
    // ─────────────────────────────────────────────
    public static class SystemAccessSection {
        private List<String> roles;
        private List<String> permissions;
        private String lastLogin;

        public SystemAccessSection() {}

        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }

        public List<String> getPermissions() { return permissions; }
        public void setPermissions(List<String> permissions) { this.permissions = permissions; }

        public String getLastLogin() { return lastLogin; }
        public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
    }

    // ─────────────────────────────────────────────
    // SECTION: exitSummary
    // ─────────────────────────────────────────────
    public static class ExitSummarySection {
        private boolean isExitInitiated;
        private String exitStatus;
        private String lastWorkingDay;
        private int pendingKTTasks;
        private String clearanceStatus;

        public ExitSummarySection() {}

        public boolean isExitInitiated() { return isExitInitiated; }
        public void setExitInitiated(boolean exitInitiated) { isExitInitiated = exitInitiated; }

        public String getExitStatus() { return exitStatus; }
        public void setExitStatus(String exitStatus) { this.exitStatus = exitStatus; }

        public String getLastWorkingDay() { return lastWorkingDay; }
        public void setLastWorkingDay(String lastWorkingDay) { this.lastWorkingDay = lastWorkingDay; }

        public int getPendingKTTasks() { return pendingKTTasks; }
        public void setPendingKTTasks(int pendingKTTasks) { this.pendingKTTasks = pendingKTTasks; }

        public String getClearanceStatus() { return clearanceStatus; }
        public void setClearanceStatus(String clearanceStatus) { this.clearanceStatus = clearanceStatus; }
    }

    // ─────────────────────────────────────────────
    // SECTION: audit
    // ─────────────────────────────────────────────
    public static class AuditSection {
        private String createdAt;
        private String updatedAt;

        public AuditSection() {}

        public AuditSection(String createdAt, String updatedAt) {
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }
}
