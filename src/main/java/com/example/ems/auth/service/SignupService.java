package com.example.ems.auth.service;

import com.example.ems.audit.entity.AuditLog;
import com.example.ems.audit.entity.Severity;
import com.example.ems.audit.repository.AuditLogRepository;
import com.example.ems.auth.dto.SignupRequest;
import com.example.ems.auth.entity.EmailVerification;
import com.example.ems.auth.entity.Role;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.event.UserRegisteredEvent;
import com.example.ems.auth.repository.RoleRepository;
import com.example.ems.employee.entity.Department;
import com.example.ems.employee.repository.DepartmentRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationAddress;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationAddressRepository;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.organization.service.SaasSubscriptionService;
import com.example.ems.organization.service.TenantProvisioningService;
import com.example.ems.organization.service.TenantRoleProvisioningService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SignupService {

    private static final Logger log = LoggerFactory.getLogger(SignupService.class);

    private final SignupValidationService validationService;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAddressRepository addressRepository;
    private final TenantProvisioningService tenantProvisioningService;
    private final SaasSubscriptionService subscriptionService;
    private final TenantRoleProvisioningService tenantRoleProvisioningService;
    private final UserProvisioningService userProvisioningService;
    private final VerificationService verificationService;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SignupService(SignupValidationService validationService,
                         OrganizationRepository organizationRepository,
                         OrganizationAddressRepository addressRepository,
                         TenantProvisioningService tenantProvisioningService,
                         SaasSubscriptionService subscriptionService,
                         TenantRoleProvisioningService tenantRoleProvisioningService,
                         UserProvisioningService userProvisioningService,
                         VerificationService verificationService,
                         RoleRepository roleRepository,
                         DepartmentRepository departmentRepository,
                         AuditLogRepository auditLogRepository,
                         ApplicationEventPublisher eventPublisher) {
        this.validationService = validationService;
        this.organizationRepository = organizationRepository;
        this.addressRepository = addressRepository;
        this.tenantProvisioningService = tenantProvisioningService;
        this.subscriptionService = subscriptionService;
        this.tenantRoleProvisioningService = tenantRoleProvisioningService;
        this.userProvisioningService = userProvisioningService;
        this.verificationService = verificationService;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.auditLogRepository = auditLogRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(rollbackFor = Exception.class)
    public SignupResult register(SignupRequest dto, String clientIp, String userAgent) {
        try {
            // 1. Normalization & Form Validation
            String normalizedEmail = validationService.normalizeEmail(dto.getEmail());
            String normalizedPhone = validationService.normalizePhone(dto.getPhone());
            String normalizedOrgName = validationService.normalizeOrgName(dto.getOrgName());

            validationService.validateUniqueUser(normalizedEmail, normalizedPhone);
            validationService.validateUniqueOrganization(dto.getOrgName());
            validationService.validatePassword(dto.getPassword(), dto.getFullName(), normalizedEmail, normalizedPhone, dto.getOrgName());
            validationService.validateGst(dto.getGstNumber(), dto.getCountry());

            // 2. Create Organization
            Organization newOrg = new Organization();
            newOrg.setName(dto.getOrgName());
            newOrg.setNormalizedName(normalizedOrgName);
            newOrg.setIndustry(dto.getIndustry());
            newOrg.setRegNumber(dto.getRegNumber());
            newOrg.setGstNumber(dto.getGstNumber());
            newOrg.setEmail(normalizedEmail);
            newOrg.setPhone(normalizedPhone);
            newOrg.setStatus(OrganizationStatus.ACTIVE);
            newOrg.setOrganizationCode("TEMP_" + java.util.UUID.randomUUID().toString().substring(0, 10));
            
            final Organization savedOrg = organizationRepository.save(newOrg);

            // Set unique organization code: ORG + padded ID (e.g. ORG0001)
            String orgCode = "ORG" + String.format("%04d", savedOrg.getId());
            savedOrg.setOrganizationCode(orgCode);
            organizationRepository.save(savedOrg);

            // 3. Create Address
            OrganizationAddress addr = new OrganizationAddress();
            addr.setCountry(dto.getCountry());
            addr.setState(dto.getState());
            addr.setCity(dto.getCity());
            addr.setStreet(dto.getAddress());
            addr.setZipCode("000000"); // default
            addr.setOrganization(savedOrg);
            addressRepository.save(addr);

            // 4. Create Tenant Subdomain
            tenantProvisioningService.provisionTenant(savedOrg, dto.getTimezone(), dto.getCurrency(), dto.getLocale());

            // 5. Create Subscription
            subscriptionService.createTrialSubscription(savedOrg, dto.getPlan(), dto.getBillingCycle());

            // 6. Provision Default Tenant Roles from Platform Templates
            tenantRoleProvisioningService.provisionTenantRoles(savedOrg.getId());

            // Load provisioned SUPER_ADMIN or ADMIN Role for the tenant admin user creation
            Role adminRole = roleRepository.findByOrganizationIdAndName(savedOrg.getId(), "SUPER_ADMIN")
                    .or(() -> roleRepository.findByOrganizationIdAndName(savedOrg.getId(), "ADMIN"))
                    .orElseGet(() -> {
                        Role template = roleRepository.findByNameAndIsPlatformTemplateTrue("SUPER_ADMIN")
                                .or(() -> roleRepository.findByNameAndIsPlatformTemplateTrue("ADMIN"))
                                .orElseGet(() -> {
                                    Role defaultRole = new Role();
                                    defaultRole.setName("SUPER_ADMIN");
                                    defaultRole.setDescription("Tenant Super Admin");
                                    defaultRole.setPlatformTemplate(true);
                                    defaultRole.setSystemRole(true);
                                    return roleRepository.save(defaultRole);
                                });
                        Role tr = new Role();
                        tr.setName(template.getName());
                        tr.setDescription(template.getDescription());
                        tr.setOrganization(savedOrg);
                        tr.setPlatformTemplate(false);
                        tr.setSystemRole(template.isSystemRole());
                        if (template.getPermissions() != null) {
                            tr.setPermissions(new HashSet<>(template.getPermissions()));
                        } else {
                            tr.setPermissions(new HashSet<>());
                        }
                        return roleRepository.save(tr);
                    });

            // 7. Seed Default Departments: HR, Finance, IT, Sales
            seedDefaultDepartments(savedOrg, orgCode);

            // 8. Create Admin User & Employee profile
            User admin = userProvisioningService.createAdminUser(
                    savedOrg,
                    adminRole,
                    dto.getFullName(),
                    normalizedEmail,
                    normalizedPhone,
                    dto.getPassword(),
                    dto.getAddress()
            );

            // 9. Generate Verification Token
            EmailVerification verification = verificationService.createVerificationToken(admin);

            // 10. Audit Log
            AuditLog audit = new AuditLog(
                    admin.getUserId(),
                    admin.getWorkEmail(),
                    admin.getFullName(),
                    "SIGNUP_COMPLETED",
                    "Organization",
                    savedOrg.getId().toString(),
                    clientIp,
                    userAgent,
                    Severity.INFO,
                    "Organization " + savedOrg.getName() + " (" + orgCode + ") successfully registered."
            );
            auditLogRepository.save(audit);

            // 11. Publish User Registered Event (Async email sending post-commit)
            eventPublisher.publishEvent(new UserRegisteredEvent(this, admin, verification.getToken()));

            return new SignupResult(savedOrg.getOrganizationCode(), admin.getUserId(), false);

        } catch (DataIntegrityViolationException ex) {
            log.error("DataIntegrityViolationException during registration: ", ex);
            throw new IllegalArgumentException("An organization or user with this name, email, or phone number already exists.");
        } catch (IllegalArgumentException ex) {
            log.error("IllegalArgumentException during registration: ", ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected exception during registration: ", ex);
            throw new IllegalArgumentException("Registration failed: " + ex.getMessage(), ex);
        }
    }

    private void seedDefaultDepartments(Organization org, String orgCode) {
        String[] defaultDepts = {"HR", "Finance", "IT", "Sales"};
        for (String deptName : defaultDepts) {
            Department d = new Department();
            d.setName(deptName + " (" + orgCode + ")");
            d.setCode((deptName + "-" + orgCode).toUpperCase());
            d.setOrganization(org);
            d.setDescription(deptName + " department for " + org.getName());
            d.setBudget(BigDecimal.ZERO);
            d.setStatus("ACTIVE");
            departmentRepository.save(d);
        }
    }

    public static class SignupResult {
        private final String organizationId;
        private final String userId;
        private final boolean emailVerificationRequired;

        public SignupResult(String organizationId, String userId, boolean emailVerificationRequired) {
            this.organizationId = organizationId;
            this.userId = userId;
            this.emailVerificationRequired = emailVerificationRequired;
        }

        public String getOrganizationId() { return organizationId; }
        public String getUserId() { return userId; }
        public boolean isEmailVerificationRequired() { return emailVerificationRequired; }
    }
}
