package com.example.ems.subscription.service;

import com.example.ems.organization.dto.SubscriptionDtos.*;
import com.example.ems.organization.entity.*;
import com.example.ems.organization.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
public class SubscriptionService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SubscriptionService.class);

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionInvoiceRepository invoiceRepository;

    @Autowired
    private SubscriptionHistoryRepository historyRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BillingEngine billingEngine;

    public SubscriptionResponse createSubscription(Long orgId, CreateSubscriptionRequest request, String performedBy) {
        Subscription sub = billingEngine.calculateAndCreateDraft(orgId, request, performedBy);

        logHistory(sub.getId(), "CREATED", null, sub.getPlanCode(), null, sub.getStatus().name(), performedBy, "Initial onboarding subscription");

        return mapToResponse(sub, performedBy);
    }

    public SubscriptionResponse getSubscription(Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        return mapToResponse(sub, "system");
    }

    public SubscriptionResponse getActiveSubscription(Long orgId) {
        return subscriptionRepository.findAll().stream()
                .filter(s -> s.getOrganization().getId().equals(orgId) && s.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .map(s -> mapToResponse(s, "system"))
                .orElseThrow(() -> new IllegalArgumentException("No active subscription found for organization"));
    }

    public List<SubscriptionResponse> getSubscriptionHistory(Long orgId) {
        List<SubscriptionResponse> history = new ArrayList<>();
        subscriptionRepository.findAll().stream()
                .filter(s -> s.getOrganization().getId().equals(orgId))
                .forEach(s -> history.add(mapToResponse(s, "system")));
        return history;
    }

    public SubscriptionResponse upgradeSubscription(Long id, UpgradeSubscriptionRequest request, String performedBy) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        String oldPlan = sub.getPlanCode();
        String oldStatus = sub.getStatus().name();

        sub.setPlanCode(request.newPlanCode());
        sub.setPlanName(request.newPlanCode() + " Plan");
        if (request.effectiveDate() != null) {
            sub.setStartDate(request.effectiveDate());
        }
        if (request.billing() != null) {
            sub.setBillingInfo(mapJson(request.billing()));
            // Generate upgrade invoice
            generateInvoice(sub, request.billing());
        }
        if (request.limits() != null) {
            sub.setLimitsInfo(mapJson(request.limits()));
        }
        if (request.features() != null) {
            sub.setFeaturesInfo(mapJson(request.features()));
        }
        sub.setUpdatedAt(Instant.now());
        sub = subscriptionRepository.save(sub);

        logHistory(sub.getId(), "UPGRADED", oldPlan, sub.getPlanCode(), oldStatus, sub.getStatus().name(), performedBy, request.remarks());

        return mapToResponse(sub, performedBy);
    }

    public SubscriptionResponse downgradeSubscription(Long id, DowngradeSubscriptionRequest request, String performedBy) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        // Downgrade validation check
        long currentEmployees = organizationRepository.countEmployees(sub.getOrganization().getId());
        Integer limitEmployees = request.limits() != null ? request.limits().maxEmployees() : null;
        if (limitEmployees != null && currentEmployees > limitEmployees) {
            throw new IllegalArgumentException("Cannot downgrade plan: Current employee count (" + currentEmployees + ") exceeds the requested limit (" + limitEmployees + ")");
        }

        String oldPlan = sub.getPlanCode();
        String oldStatus = sub.getStatus().name();

        sub.setPlanCode(request.newPlanCode());
        sub.setPlanName(request.newPlanCode() + " Plan");
        if (request.effectiveDate() != null) {
            sub.setStartDate(request.effectiveDate());
        }
        if (request.billing() != null) {
            sub.setBillingInfo(mapJson(request.billing()));
        }
        if (request.limits() != null) {
            sub.setLimitsInfo(mapJson(request.limits()));
        }
        if (request.features() != null) {
            sub.setFeaturesInfo(mapJson(request.features()));
        }
        sub.setUpdatedAt(Instant.now());
        sub = subscriptionRepository.save(sub);

        logHistory(sub.getId(), "DOWNGRADED", oldPlan, sub.getPlanCode(), oldStatus, sub.getStatus().name(), performedBy, request.remarks());

        return mapToResponse(sub, performedBy);
    }

    public SubscriptionResponse renewSubscription(Long id, RenewSubscriptionRequest request, String performedBy) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        String oldPlan = sub.getPlanCode();
        String oldStatus = sub.getStatus().name();

        if (request.duration() != null && request.duration().endDate() != null) {
            sub.setExpiryDate(request.duration().endDate());
        } else {
            sub.setExpiryDate(sub.getExpiryDate().plusYears(1));
        }

        if (request.billing() != null) {
            sub.setBillingInfo(mapJson(request.billing()));
            generateInvoice(sub, request.billing());
        }

        sub.setUpdatedAt(Instant.now());
        sub = subscriptionRepository.save(sub);

        logHistory(sub.getId(), "RENEWED", oldPlan, sub.getPlanCode(), oldStatus, sub.getStatus().name(), performedBy, request.remarks());

        return mapToResponse(sub, performedBy);
    }

    public void cancelSubscription(Long id, String reason, String performedBy) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        transitionSubscription(sub, SubscriptionStatus.CANCELLED, performedBy, reason, "CANCELLED");
    }

    public void suspendSubscription(Long id, String reason, String performedBy) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        transitionSubscription(sub, SubscriptionStatus.SUSPENDED, performedBy, reason, "SUSPENDED");
    }

    public void resumeSubscription(Long id, String performedBy) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        transitionSubscription(sub, SubscriptionStatus.ACTIVE, performedBy, "Resumed service", "RESUMED");
    }

    public void updateAutoRenew(Long id, AutoRenewRequest request, String performedBy) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        sub.setAutoRenew(request.autoRenew());
        sub.setUpdatedAt(Instant.now());
        subscriptionRepository.save(sub);

        logHistory(sub.getId(), "AUTO_RENEW_CHANGED", sub.getPlanCode(), sub.getPlanCode(), sub.getStatus().name(), sub.getStatus().name(), performedBy, request.remarks());
    }

    public void updateModules(Long id, ModulesRequest request, String performedBy) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        Map<String, Object> features = sub.getFeaturesInfo();
        if (request.enabledModules() != null) {
            request.enabledModules().forEach(m -> features.put(m.toLowerCase(), true));
        }
        if (request.disabledModules() != null) {
            request.disabledModules().forEach(m -> features.put(m.toLowerCase(), false));
        }
        sub.setFeaturesInfo(features);
        sub.setUpdatedAt(Instant.now());
        subscriptionRepository.save(sub);

        logHistory(sub.getId(), "MODULES_UPDATED", sub.getPlanCode(), sub.getPlanCode(), sub.getStatus().name(), sub.getStatus().name(), performedBy, "Custom modules setup");
    }

    public void updateLimits(Long id, LimitsRequest request, String performedBy) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        Map<String, Object> limits = sub.getLimitsInfo();
        if (request.employeeLimit() != null) limits.put("maxEmployees", request.employeeLimit());
        if (request.adminLimit() != null) limits.put("maxAdmins", request.adminLimit());
        if (request.storageLimit() != null) limits.put("maxStorageGB", request.storageLimit());
        if (request.apiRateLimit() != null) limits.put("maxApiRequestsPerMonth", request.apiRateLimit());

        sub.setLimitsInfo(limits);
        sub.setUpdatedAt(Instant.now());
        subscriptionRepository.save(sub);

        logHistory(sub.getId(), "LIMITS_UPDATED", sub.getPlanCode(), sub.getPlanCode(), sub.getStatus().name(), sub.getStatus().name(), performedBy, "Custom limits adjustments");
    }

    public SubscriptionUsageResponse getUsage(Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        long currentEmployees = organizationRepository.countEmployees(sub.getOrganization().getId());
        long currentAdmins = organizationRepository.countAdmins(sub.getOrganization().getId());
        long currentDepts = organizationRepository.countDepartments(sub.getOrganization().getId());

        Map<String, Object> limitMap = sub.getLimitsInfo();
        long limitEmployees = Long.parseLong(limitMap.getOrDefault("maxEmployees", 1000).toString());
        long limitAdmins = Long.parseLong(limitMap.getOrDefault("maxAdmins", 25).toString());
        long limitDepts = Long.parseLong(limitMap.getOrDefault("maxDepartments", 100).toString());
        double limitStorage = Double.parseDouble(limitMap.getOrDefault("maxStorageGB", 500).toString());

        double usedStorage = currentEmployees * 0.12; // Simulated storage usage

        Map<String, Object> limits = Map.of(
                "employees", limitEmployees,
                "admins", limitAdmins,
                "departments", limitDepts,
                "storageGB", limitStorage
        );

        Map<String, Object> usage = Map.of(
                "employees", currentEmployees,
                "admins", currentAdmins,
                "departments", currentDepts,
                "storageGB", usedStorage
        );

        Map<String, Object> remaining = Map.of(
                "employees", Math.max(0, limitEmployees - currentEmployees),
                "admins", Math.max(0, limitAdmins - currentAdmins),
                "departments", Math.max(0, limitDepts - currentDepts),
                "storageGB", Math.max(0.0, limitStorage - usedStorage)
        );

        Map<String, Double> utilization = Map.of(
                "employees", (double) currentEmployees * 100.0 / limitEmployees,
                "admins", (double) currentAdmins * 100.0 / limitAdmins,
                "departments", (double) currentDepts * 100.0 / limitDepts,
                "storage", usedStorage * 100.0 / limitStorage
        );

        return new SubscriptionUsageResponse(
                sub.getOrganization().getId(),
                sub.getId(),
                limits,
                usage,
                remaining,
                utilization,
                Instant.now().toString()
        );
    }

    public List<InvoiceResponse> getInvoices(Long subscriptionId) {
        List<InvoiceResponse> res = new ArrayList<>();
        invoiceRepository.findBySubscriptionId(subscriptionId).forEach(i ->
                res.add(new InvoiceResponse(
                        i.getId(), i.getSubscription().getId(), i.getInvoiceNumber(), i.getAmount(), i.getCurrency(),
                        i.getTax(), i.getDiscount(), i.getStatus() != null ? i.getStatus().name() : null, i.getIssuedAt().toString(),
                        i.getDueAt() != null ? i.getDueAt().toString() : null,
                        i.getPaidAt() != null ? i.getPaidAt().toString() : null
                ))
        );
        return res;
    }

    public InvoiceResponse getInvoiceDetails(Long invoiceId) {
        SubscriptionInvoice i = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        return new InvoiceResponse(
                i.getId(), i.getSubscription().getId(), i.getInvoiceNumber(), i.getAmount(), i.getCurrency(),
                i.getTax(), i.getDiscount(), i.getStatus() != null ? i.getStatus().name() : null, i.getIssuedAt().toString(),
                i.getDueAt() != null ? i.getDueAt().toString() : null,
                i.getPaidAt() != null ? i.getPaidAt().toString() : null
        );
    }

    public byte[] downloadInvoice(Long invoiceId) {
        SubscriptionInvoice i = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        if (i.getPdfContent() == null) {
            // Generate dummy PDF bytes for download
            return ("INVOICE " + i.getInvoiceNumber() + "\nAmount: " + i.getAmount() + " " + i.getCurrency() + "\nStatus: " + (i.getStatus() != null ? i.getStatus().name() : null)).getBytes();
        }
        return i.getPdfContent();
    }

    public List<SubscriptionHistoryResponse> getHistory(Long subscriptionId) {
        List<SubscriptionHistoryResponse> res = new ArrayList<>();
        historyRepository.findBySubscriptionId(subscriptionId).forEach(h ->
                res.add(new SubscriptionHistoryResponse(
                        h.getId(), h.getSubscriptionId(), h.getAction(), h.getOldPlan(), h.getNewPlan(),
                        h.getOldStatus(), h.getNewStatus(), h.getPerformedBy(), h.getPerformedAt().toString(), h.getRemarks()
                ))
        );
        return res;
    }

    public void deleteSubscription(Long id, String performedBy) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));
        subscriptionRepository.delete(sub);
    }

    public Page<SubscriptionResponse> searchSubscriptions(String status, String plan, Pageable pageable) {
        return subscriptionRepository.findAll(pageable).map(s -> mapToResponse(s, "system"));
    }

    // ── Internal Helpers ─────────────────────────────────────────────────────

    private void generateInvoice(Subscription sub, BillingDto billing) {
        SubscriptionInvoice inv = new SubscriptionInvoice();
        inv.setSubscription(sub);
        inv.setInvoiceNumber("INV-" + LocalDate.now().getYear() + "-" + String.format("%06d", new Random().nextInt(1000000)));
        inv.setAmount(billing != null && billing.amount() != null ? billing.amount() : BigDecimal.valueOf(999.00));
        inv.setCurrency(billing != null && billing.currency() != null ? billing.currency() : "USD");
        inv.setTax(billing != null && billing.taxAmount() != null ? billing.taxAmount() : BigDecimal.ZERO);
        inv.setDiscount(billing != null && billing.discountAmount() != null ? billing.discountAmount() : BigDecimal.ZERO);
        inv.setStatus(InvoiceStatus.ISSUED);
        inv.setIssuedAt(Instant.now());
        inv.setDueAt(LocalDate.now().plusDays(30));
        inv.setPaidAt(null);
        invoiceRepository.save(inv);
    }

    private void logHistory(Long subId, String action, String oldPlan, String newPlan, String oldStatus, String newStatus, String performedBy, String remarks) {
        SubscriptionHistory history = new SubscriptionHistory();
        history.setSubscriptionId(subId);
        history.setAction(action);
        history.setOldPlan(oldPlan);
        history.setNewPlan(newPlan);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setPerformedBy(performedBy);
        history.setPerformedAt(Instant.now());
        history.setRemarks(remarks);
        historyRepository.save(history);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapJson(Object obj) {
        try {
            if (obj == null) return new HashMap<>();
            return objectMapper.convertValue(obj, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private SubscriptionResponse mapToResponse(Subscription sub, String performedBy) {
        PlanDto plan = new PlanDto(sub.getPlanCode(), sub.getPlanName());
        
        // Mapped values back to DTOs safely
        BigDecimal amount = sub.getBillingInfo().get("amount") != null ? new BigDecimal(sub.getBillingInfo().get("amount").toString()) : BigDecimal.ZERO;
        String currency = sub.getBillingInfo().get("currency") != null ? sub.getBillingInfo().get("currency").toString() : "USD";
        BillingDto billing = new BillingDto(
                (String) sub.getBillingInfo().get("cycle"),
                amount,
                currency,
                sub.getBillingInfo().get("taxAmount") != null ? new BigDecimal(sub.getBillingInfo().get("taxAmount").toString()) : BigDecimal.ZERO,
                sub.getBillingInfo().get("discountAmount") != null ? new BigDecimal(sub.getBillingInfo().get("discountAmount").toString()) : BigDecimal.ZERO,
                sub.getBillingInfo().get("finalAmount") != null ? new BigDecimal(sub.getBillingInfo().get("finalAmount").toString()) : BigDecimal.ZERO
        );

        long days = ChronoUnit.DAYS.between(LocalDate.now(), sub.getExpiryDate());
        DurationDto duration = new DurationDto(
                sub.getStartDate(),
                sub.getExpiryDate(),
                sub.isAutoRenew(),
                Math.max(0L, days)
        );

        LimitsDto limits = new LimitsDto(
                (Integer) sub.getLimitsInfo().get("maxEmployees"),
                (Integer) sub.getLimitsInfo().get("maxAdmins"),
                (Integer) sub.getLimitsInfo().get("maxDepartments"),
                (Integer) sub.getLimitsInfo().get("maxStorageGB"),
                (Integer) sub.getLimitsInfo().get("maxApiRequestsPerMonth")
        );

        PaymentDto payment = new PaymentDto(
                (String) sub.getPaymentInfo().get("method"),
                (String) sub.getPaymentInfo().get("referenceNumber"),
                (String) sub.getPaymentInfo().get("paymentStatus")
        );

        return new SubscriptionResponse(
                sub.getId(),
                sub.getOrganization().getId(),
                sub.getStatus().name(),
                plan,
                billing,
                duration,
                limits,
                payment,
                sub.getCreatedAt().toString(),
                performedBy
        );
    }

    @org.springframework.transaction.annotation.Transactional
    public void markInvoiceAsPaid(Long invoiceId) {
        SubscriptionInvoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        if (InvoiceStatus.PAID == invoice.getStatus()) {
            return; // Level 2/3 Idempotency check
        }
        transitionInvoice(invoice, InvoiceStatus.PAID);
        invoice.setPaidAt(Instant.now());
        invoiceRepository.save(invoice);
    }

    @org.springframework.transaction.annotation.Transactional
    public void activateSubscription(Long subscriptionId, String gatewayPaymentId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + subscriptionId));
        if (SubscriptionStatus.ACTIVE == subscription.getStatus()) {
            return; // Level 2/3 Idempotency check
        }
        transitionSubscription(subscription, SubscriptionStatus.ACTIVE, "webhook-system", 
                "Decoupled Payment Succeeded verification handler for payment: " + gatewayPaymentId, "PAYMENT_VERIFIED");
    }

    private void transitionSubscription(Subscription sub, SubscriptionStatus newStatus, String performedBy, String remarks, String action) {
        SubscriptionStatus oldStatus = sub.getStatus();
        if (!SubscriptionStateMachine.canTransition(oldStatus, newStatus)) {
            throw new com.example.ems.subscription.exception.InvalidStateTransitionException(
                "Invalid subscription state transition from " + oldStatus + " to " + newStatus
            );
        }
        sub.setStatus(newStatus);
        sub.setUpdatedAt(Instant.now());
        subscriptionRepository.save(sub);
        logHistory(sub.getId(), action, sub.getPlanCode(), sub.getPlanCode(),
                oldStatus != null ? oldStatus.name() : null, newStatus.name(), performedBy, remarks);
    }

    private void transitionInvoice(SubscriptionInvoice invoice, InvoiceStatus newStatus) {
        InvoiceStatus oldStatus = invoice.getStatus();
        if (!InvoiceStateMachine.canTransition(oldStatus, newStatus)) {
            throw new com.example.ems.subscription.exception.InvalidStateTransitionException(
                "Invalid invoice state transition from " + oldStatus + " to " + newStatus
            );
        }
        invoice.setStatus(newStatus);
        
        try {
            jdbcTemplate.update(
                "INSERT INTO invoice_state_log (invoice_id, from_state, to_state, performed_by, remarks, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                invoice.getId(),
                oldStatus != null ? oldStatus.name() : null,
                newStatus.name(),
                "system",
                "FSM validated state transition",
                java.sql.Timestamp.from(Instant.now())
            );
            log.info("[SubscriptionService] Wrote state transition log for invoice id: {} from: {} to: {}", invoice.getId(), oldStatus, newStatus);
        } catch (Exception e) {
            log.error("[SubscriptionService] Failed to write state transition log for invoice id: {}: {}", invoice.getId(), e.getMessage(), e);
        }
    }
}
