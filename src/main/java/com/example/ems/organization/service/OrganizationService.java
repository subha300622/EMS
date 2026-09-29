package com.example.ems.organization.service;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.organization.dto.*;
import com.example.ems.organization.entity.*;
import com.example.ems.organization.event.OrganizationEvents.*;
import com.example.ems.organization.repository.OrganizationAuditLogRepository;
import com.example.ems.organization.repository.OrganizationRepository;
import com.example.ems.organization.specification.OrganizationSpecification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class OrganizationService {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationAuditLogRepository auditLogRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ObjectMapper objectMapper;

    public OrganizationDetailResponse createOrganization(CreateOrganizationRequest request, String performedBy) {
        Organization org = new Organization();
        org.setName(request.getName());
        org.setEmail(request.getEmail());
        org.setPhone(request.getPhone());
        org.setWebsite(request.getWebsite());
        org.setCreatedAt(Instant.now());

        // Temporarily set random unique code
        org.setOrganizationCode("TEMP-" + UUID.randomUUID().toString().substring(0, 8));

        OrganizationAddress addr = new OrganizationAddress();
        addr.setStreet(request.getAddress().getStreet());
        addr.setCity(request.getAddress().getCity());
        addr.setState(request.getAddress().getState());
        addr.setCountry(request.getAddress().getCountry());
        addr.setZipCode(request.getAddress().getZipCode());
        org.setAddress(addr);

        OrganizationSettings settings = new OrganizationSettings();
        settings.getConfig().put("theme", "light");
        settings.getConfig().put("attendance_enabled", true);
        org.setSettings(settings);

        Subscription sub = new Subscription();
        sub.setPlanCode(request.getSubscriptionPlan().toUpperCase());
        sub.setPlanName(request.getSubscriptionPlan().toUpperCase() + " Plan");
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setStartDate(LocalDate.now());
        sub.setExpiryDate(LocalDate.now().plusYears(1));
        sub.getBillingInfo().put("amount", 1200.00);
        sub.getLimitsInfo().put("maxEmployees", 1000);
        sub.getLimitsInfo().put("maxAdmins", 25);
        org.addSubscription(sub);

        org = organizationRepository.save(org);

        // Update with clean year-based code
        org.setOrganizationCode("ORG-" + LocalDate.now().getYear() + String.format("%05d", org.getId()));
        org = organizationRepository.save(org);

        logAction(org.getId(), "CREATE", "Organization", org.getId(), performedBy, null, writeJson(org));

        eventPublisher.publishEvent(new OrganizationCreatedEvent(org.getId(), org.getOrganizationCode()));

        return mapToDetailResponse(org);
    }

    public OrganizationDetailResponse updateOrganization(Long id, UpdateOrganizationRequest request, String performedBy) {
        Organization org = organizationRepository.findById(id)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        String oldVal = writeJson(org);

        if (request.getName() != null) org.setName(request.getName());
        if (request.getPhone() != null) org.setPhone(request.getPhone());
        if (request.getWebsite() != null) org.setWebsite(request.getWebsite());

        org = organizationRepository.save(org);

        logAction(org.getId(), "UPDATE", "Organization", org.getId(), performedBy, oldVal, writeJson(org));

        eventPublisher.publishEvent(new OrganizationUpdatedEvent(org.getId()));

        return mapToDetailResponse(org);
    }

    public void suspendOrganization(Long id, String reason, String performedBy) {
        Organization org = organizationRepository.findById(id)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        Subscription sub = org.getActiveSubscription();
        if (sub != null) {
            String oldVal = writeJson(sub);
            sub.setStatus(SubscriptionStatus.SUSPENDED);
            sub.setUpdatedAt(Instant.now());
            org.setDeletedBy(reason); // Store suspend reason/metadata temporarily
            organizationRepository.save(org);

            logAction(org.getId(), "SUSPEND", "Subscription", sub.getId(), performedBy, oldVal, writeJson(sub));
            eventPublisher.publishEvent(new OrganizationSuspendedEvent(org.getId(), reason));
        }
    }

    public void activateOrganization(Long id, String performedBy) {
        Organization org = organizationRepository.findById(id)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        Subscription sub = org.getActiveSubscription();
        if (sub != null) {
            String oldVal = writeJson(sub);
            sub.setStatus(SubscriptionStatus.ACTIVE);
            sub.setUpdatedAt(Instant.now());
            organizationRepository.save(org);

            logAction(org.getId(), "ACTIVATE", "Subscription", sub.getId(), performedBy, oldVal, writeJson(sub));
            eventPublisher.publishEvent(new OrganizationActivatedEvent(org.getId()));
        }
    }

    public void updateSubscription(Long id, UpdateSubscriptionRequest request, String performedBy) {
        Organization org = organizationRepository.findById(id)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        Subscription sub = org.getActiveSubscription();
        if (sub != null) {
            String oldVal = writeJson(sub);
            sub.setPlanCode(request.getPlan().toUpperCase());
            sub.setPlanName(request.getPlan().toUpperCase() + " Plan");
            sub.setExpiryDate(request.getExpiryDate());
            sub.setUpdatedAt(Instant.now());
            organizationRepository.save(org);

            logAction(org.getId(), "UPDATE_SUBSCRIPTION", "Subscription", sub.getId(), performedBy, oldVal, writeJson(sub));
            eventPublisher.publishEvent(new SubscriptionUpdatedEvent(org.getId(), sub.getPlanCode(), sub.getStatus().name()));
        }
    }

    public void deleteOrganization(Long id, String performedBy) {
        Organization org = organizationRepository.findById(id)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        String oldVal = writeJson(org);

        org.setDeleted(true);
        org.setDeletedAt(Instant.now());
        org.setDeletedBy(performedBy);
        organizationRepository.save(org);

        logAction(org.getId(), "DELETE", "Organization", org.getId(), performedBy, oldVal, writeJson(org));
        eventPublisher.publishEvent(new OrganizationDeletedEvent(org.getId(), performedBy));
    }

    public OrganizationDetailResponse getOrganizationDetails(Long id) {
        Organization org = organizationRepository.findById(id)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        return mapToDetailResponse(org);
    }

    public Page<OrganizationListItemResponse> searchOrganizations(String search, String status, String plan, Pageable pageable) {
        Specification<Organization> spec = OrganizationSpecification.filter(search, status, plan);
        Page<Organization> page = organizationRepository.findAll(spec, pageable);
        return page.map(this::mapToListItemResponse);
    }

    public OrganizationStatisticsResponse getStatistics(Long id) {
        organizationRepository.findById(id)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        long employees = organizationRepository.countEmployees(id);
        long departments = organizationRepository.countDepartments(id);
        long admins = organizationRepository.countAdmins(id);
        long activeUsers = organizationRepository.countActiveEmployees(id);

        double revenue = employees * 35.00;
        double storage = employees * 0.15;

        return new OrganizationStatisticsResponse(
                employees, departments, admins, activeUsers, revenue, storage, Instant.now().toString()
        );
    }

    public OrganizationSummaryResponse getSummary() {
        List<Organization> orgs = organizationRepository.findAll();
        long total = 0;
        long active = 0;
        long suspended = 0;
        long trial = 0;
        long premium = 0;
        long enterprise = 0;

        for (Organization o : orgs) {
            if (o.isDeleted()) continue;
            total++;
            Subscription sub = o.getActiveSubscription();
            if (sub != null) {
                if (sub.getStatus() == SubscriptionStatus.ACTIVE) active++;
                if (sub.getStatus() == SubscriptionStatus.SUSPENDED) suspended++;
                if (sub.getStatus() == SubscriptionStatus.TRIAL) trial++;
                if ("PREMIUM".equalsIgnoreCase(sub.getPlanCode())) premium++;
                if ("ENTERPRISE".equalsIgnoreCase(sub.getPlanCode())) enterprise++;
            }
        }

        return new OrganizationSummaryResponse(total, active, suspended, trial, premium, enterprise);
    }

    public Page<Employee> getEmployees(Long id, Pageable pageable) {
        return employeeRepository.findAll((root, query, cb) ->
                cb.equal(root.get("organization").get("id"), id), pageable);
    }

    public List<User> getAdmins(Long id) {
        return userRepository.findAll((root, query, cb) ->
                cb.and(
                        cb.equal(root.get("organization").get("id"), id),
                        cb.or(
                                cb.equal(root.get("role").get("name"), "ADMIN"),
                                cb.equal(root.get("role").get("name"), "SUPER_ADMIN")
                        )
                )
        );
    }

    public Page<OrganizationAuditLog> getAuditLogs(Long id, Pageable pageable) {
        return auditLogRepository.findAll((root, query, cb) ->
                cb.equal(root.get("organizationId"), id), pageable);
    }

    public List<Organization> getAllForExport() {
        return organizationRepository.findAll((root, query, cb) ->
                cb.equal(root.get("isDeleted"), false));
    }

    // ── Internal Helpers ─────────────────────────────────────────────────────

    private void logAction(Long orgId, String action, String entity, Long entityId, String performedBy, String oldVal, String newVal) {
        OrganizationAuditLog log = new OrganizationAuditLog();
        log.setOrganizationId(orgId);
        log.setAction(action);
        log.setEntity(entity);
        log.setEntityId(entityId);
        log.setPerformedBy(performedBy);
        log.setPerformedAt(Instant.now());
        log.setOldValues(oldVal);
        log.setNewValues(newVal);
        auditLogRepository.save(log);
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private OrganizationDetailResponse mapToDetailResponse(Organization org) {
        OrganizationAddressDto addrDto = null;
        if (org.getAddress() != null) {
            addrDto = new OrganizationAddressDto(
                    org.getAddress().getStreet(),
                    org.getAddress().getCity(),
                    org.getAddress().getState(),
                    org.getAddress().getCountry(),
                    org.getAddress().getZipCode()
            );
        }

        OrganizationSubscriptionDto subDto = null;
        Subscription sub = org.getActiveSubscription();
        if (sub != null) {
            subDto = new OrganizationSubscriptionDto(
                    sub.getPlanCode(),
                    sub.getStatus().name(),
                    sub.getStartDate().toString(),
                    sub.getExpiryDate().toString()
            );
        }

        long employeeCount = organizationRepository.countEmployees(org.getId());
        long adminCount = organizationRepository.countAdmins(org.getId());

        return new OrganizationDetailResponse(
                org.getId(),
                org.getOrganizationCode(),
                org.getName(),
                org.getEmail(),
                org.getPhone(),
                org.getWebsite(),
                addrDto,
                subDto,
                employeeCount,
                adminCount,
                org.getCreatedAt().toString()
        );
    }

    private OrganizationListItemResponse mapToListItemResponse(Organization org) {
        Subscription sub = org.getActiveSubscription();
        String plan = sub != null ? sub.getPlanCode() : "N/A";
        String status = sub != null ? sub.getStatus().name() : "N/A";
        long employeeCount = organizationRepository.countEmployees(org.getId());

        return new OrganizationListItemResponse(
                org.getId(),
                org.getOrganizationCode(),
                org.getName(),
                org.getEmail(),
                org.getPhone(),
                plan,
                employeeCount,
                status,
                org.getCreatedAt().toString()
        );
    }
}
