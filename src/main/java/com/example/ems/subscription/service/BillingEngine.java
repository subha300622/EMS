package com.example.ems.subscription.service;

import com.example.ems.organization.dto.SubscriptionDtos.*;
import com.example.ems.organization.entity.*;
import com.example.ems.organization.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * BillingEngine is the central orchestrator responsible for pre-payment operations,
 * calculation of billing parameters (taxes, discounts, base rates), subscription draft (PENDING)
 * provisioning, and generation of immutable PENDING invoice snapshots.
 */
@Service
@Transactional
public class BillingEngine {

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionInvoiceRepository invoiceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Calculates final billing numbers and instantiates subscription & invoice in pending states.
     */
    public Subscription calculateAndCreateDraft(Long orgId, CreateSubscriptionRequest request, String performedBy) {
        Organization org = organizationRepository.findById(orgId)
                .filter(o -> !o.isDeleted())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));

        // Enforce only one active subscription
        List<Subscription> existing = subscriptionRepository.findAll();
        for (Subscription s : existing) {
            if (s.getOrganization().getId().equals(orgId) && s.getStatus() == SubscriptionStatus.ACTIVE) {
                throw new IllegalArgumentException("Organization already has an active subscription");
            }
        }

        Subscription sub = new Subscription();
        sub.setOrganization(org);
        sub.setPlanCode(request.plan().code());
        sub.setPlanName(request.plan().name() != null ? request.plan().name() : request.plan().code() + " Plan");
        sub.setStatus(SubscriptionStatus.PENDING_PAYMENT); // Draft state: PENDING_PAYMENT

        LocalDate start = request.duration() != null && request.duration().startDate() != null ? request.duration().startDate() : LocalDate.now();
        LocalDate end = request.duration() != null && request.duration().endDate() != null ? request.duration().endDate() : LocalDate.now().plusYears(1);
        sub.setStartDate(start);
        sub.setExpiryDate(end);
        sub.setAutoRenew(request.duration() != null && request.duration().autoRenew() != null ? request.duration().autoRenew() : false);

        // Core pricing calculations
        BigDecimal baseAmount = request.billing() != null && request.billing().amount() != null ? request.billing().amount() : BigDecimal.valueOf(50000.00);
        BigDecimal discount = request.billing() != null && request.billing().discountAmount() != null ? request.billing().discountAmount() : BigDecimal.ZERO;
        
        // 18% standard GST tax calculation
        BigDecimal taxableAmount = baseAmount.subtract(discount);
        BigDecimal tax = request.billing() != null && request.billing().taxAmount() != null ? request.billing().taxAmount() : taxableAmount.multiply(BigDecimal.valueOf(0.18));
        BigDecimal finalAmount = taxableAmount.add(tax);

        BillingDto calculatedBilling = new BillingDto(
                request.billing() != null ? request.billing().cycle() : "YEARLY",
                baseAmount,
                request.billing() != null ? request.billing().currency() : "INR",
                tax,
                discount,
                finalAmount
        );

        sub.setBillingInfo(mapJson(calculatedBilling));
        sub.setLimitsInfo(mapJson(request.limits()));
        sub.setFeaturesInfo(mapJson(request.features()));
        sub.setPaymentInfo(mapJson(request.payment()));
        sub.setNotes(request.notes());
        sub.setCreatedAt(Instant.now());
        sub.setUpdatedAt(Instant.now());

        sub = subscriptionRepository.save(sub);

        // Generate the immutable PENDING invoice snapshot
        generateInvoice(sub, calculatedBilling);

        return sub;
    }

    private void generateInvoice(Subscription sub, BillingDto billing) {
        SubscriptionInvoice inv = new SubscriptionInvoice();
        inv.setSubscription(sub);
        inv.setInvoiceNumber("INV-" + LocalDate.now().getYear() + "-" + String.format("%06d", new Random().nextInt(1000000)));
        inv.setAmount(billing.finalAmount() != null ? billing.finalAmount() : billing.amount());
        inv.setCurrency(billing.currency());
        inv.setTax(billing.taxAmount());
        inv.setDiscount(billing.discountAmount());
        inv.setStatus(InvoiceStatus.ISSUED);
        inv.setIssuedAt(Instant.now());
        inv.setDueAt(LocalDate.now().plusDays(30));
        inv.setPaidAt(null);
        invoiceRepository.save(inv);
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
}
