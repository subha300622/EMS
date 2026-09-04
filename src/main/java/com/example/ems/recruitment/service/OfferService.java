package com.example.ems.recruitment.service;

import com.example.ems.audit.service.AuditLogService;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.recruitment.dto.OfferGenerateRequest;
import com.example.ems.recruitment.dto.OfferResponse;
import com.example.ems.recruitment.entity.*;
import com.example.ems.recruitment.repository.ApplicationRepository;
import com.example.ems.recruitment.repository.OfferRepository;
import com.example.ems.security.context.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OfferService {

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private AuditLogService auditLogService;

    public OfferResponse generateOffer(Long applicationId, OfferGenerateRequest request) {
        Long orgId = TenantContext.requireOrganizationId();
        Application app = applicationRepository.findByOrganizationIdAndId(orgId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + applicationId));

        if (app.getStatus() != ApplicationStatus.SELECTED) {
            throw new BadRequestException("Offers can only be generated for candidates in SELECTED status");
        }

        boolean activeOfferExists = offerRepository.existsByOrganizationIdAndApplicationIdAndStatusIn(
                orgId, applicationId, List.of(OfferStatus.DRAFT, OfferStatus.SENT, OfferStatus.ACCEPTED));

        if (activeOfferExists) {
            throw new ConflictException("An active or accepted offer already exists for this application");
        }

        if (request.getAnnualSalary() == null || request.getAnnualSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Annual salary must be greater than zero");
        }

        if (request.getJoiningDate() != null && request.getJoiningDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Joining date cannot be in the past");
        }

        Offer offer = new Offer();
        offer.setOrganizationId(orgId);
        offer.setApplication(app);
        offer.setOfferNumber("OFFER-" + System.currentTimeMillis());
        offer.setDesignation(request.getDesignation());
        offer.setAnnualSalary(request.getAnnualSalary());
        offer.setJoiningDate(request.getJoiningDate());
        offer.setProbationMonths(request.getProbationMonths() != null ? request.getProbationMonths() : 6);
        offer.setStatus(OfferStatus.DRAFT);

        Offer saved = offerRepository.save(offer);

        auditLogService.logAction("HR", "hr@company.com", "CREATE_OFFER", "Offer",
                saved.getId().toString(), getCurrentClientIp(), "Generated offer " + saved.getOfferNumber() + " for application " + app.getApplicationNumber());

        return new OfferResponse(saved);
    }

    public OfferResponse sendOffer(Long offerId) {
        Long orgId = TenantContext.requireOrganizationId();
        Offer offer = offerRepository.findByOrganizationIdAndId(orgId, offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with ID: " + offerId));

        if (offer.getStatus() != OfferStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT offers can be sent to candidates");
        }

        Application app = offer.getApplication();
        if (app == null || app.getCandidate() == null || app.getCandidate().getEmail() == null) {
            throw new BadRequestException("Candidate email is required to send offer");
        }

        String token = UUID.randomUUID().toString();
        offer.setAcceptanceToken(token);
        offer.setStatus(OfferStatus.SENT);
        offer.setSentAt(LocalDateTime.now());

        Offer saved = offerRepository.save(offer);

        if (app.getStatus().isValidTransition(ApplicationStatus.OFFER_SENT)) {
            ApplicationStatus oldStatus = app.getStatus();
            app.setStatus(ApplicationStatus.OFFER_SENT);
            applicationRepository.save(app);
            applicationService.recordStatusHistory(app, oldStatus, ApplicationStatus.OFFER_SENT, "HR", "Offer sent to candidate");
        }

        auditLogService.logAction("HR", "hr@company.com", "SEND_OFFER", "Offer",
                saved.getId().toString(), getCurrentClientIp(), "Sent offer " + saved.getOfferNumber() + " with acceptance token");

        return new OfferResponse(saved);
    }

    public OfferResponse acceptOfferPublic(String token) {
        Offer offer = offerRepository.findByAcceptanceToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired offer acceptance token"));

        if (offer.getStatus() != OfferStatus.SENT) {
            throw new BadRequestException("Offer is not in SENT status or has already been processed");
        }

        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setAcceptedAt(LocalDateTime.now());

        Offer saved = offerRepository.save(offer);

        Application app = offer.getApplication();
        if (app != null && app.getStatus().isValidTransition(ApplicationStatus.OFFER_ACCEPTED)) {
            ApplicationStatus oldStatus = app.getStatus();
            app.setStatus(ApplicationStatus.OFFER_ACCEPTED);
            applicationRepository.save(app);
            applicationService.recordStatusHistory(app, oldStatus, ApplicationStatus.OFFER_ACCEPTED, "CANDIDATE", "Candidate accepted offer online");
        }

        auditLogService.logAction("CANDIDATE", app.getCandidate().getEmail(), "ACCEPT_OFFER", "Offer",
                saved.getId().toString(), getCurrentClientIp(), "Candidate accepted offer " + saved.getOfferNumber());

        return new OfferResponse(saved);
    }

    public OfferResponse declineOfferPublic(String token) {
        Offer offer = offerRepository.findByAcceptanceToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired offer token"));

        if (offer.getStatus() != OfferStatus.SENT) {
            throw new BadRequestException("Offer is not in SENT status or has already been processed");
        }

        offer.setStatus(OfferStatus.REJECTED);
        Offer saved = offerRepository.save(offer);

        Application app = offer.getApplication();
        if (app != null) {
            ApplicationStatus oldStatus = app.getStatus();
            app.setStatus(ApplicationStatus.REJECTED);
            applicationRepository.save(app);
            applicationService.recordStatusHistory(app, oldStatus, ApplicationStatus.REJECTED, "CANDIDATE", "Candidate declined offer online");
        }

        auditLogService.logAction("PUBLIC_USER", app != null && app.getCandidate() != null ? app.getCandidate().getEmail() : "candidate",
                "DECLINE_OFFER", "Offer", saved.getId().toString(), getCurrentClientIp(), "Candidate declined offer " + saved.getOfferNumber());

        return new OfferResponse(saved);
    }

    public OfferResponse withdrawOffer(Long offerId) {
        Long orgId = TenantContext.requireOrganizationId();
        Offer offer = offerRepository.findByOrganizationIdAndId(orgId, offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with ID: " + offerId));

        if (offer.getStatus() != OfferStatus.DRAFT && offer.getStatus() != OfferStatus.SENT) {
            throw new BadRequestException("Only DRAFT or SENT offers can be withdrawn");
        }

        offer.setStatus(OfferStatus.WITHDRAWN);
        Offer saved = offerRepository.save(offer);

        auditLogService.logAction("HR", "hr@company.com", "WITHDRAW_OFFER", "Offer",
                saved.getId().toString(), getCurrentClientIp(), "Withdrew offer " + saved.getOfferNumber());

        return new OfferResponse(saved);
    }

    @Transactional(readOnly = true)
    public OfferResponse getOfferByApplicationId(Long applicationId) {
        Long orgId = TenantContext.requireOrganizationId();
        Offer offer = offerRepository.findByOrganizationIdAndApplicationId(orgId, applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("No offer found for application ID: " + applicationId));
        return new OfferResponse(offer);
    }

    private String getCurrentClientIp() {
        try {
            org.springframework.web.context.request.ServletRequestAttributes attrs =
                    (org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return com.example.ems.common.util.ClientIpResolver.getClientIp(attrs.getRequest());
            }
        } catch (Exception ignored) {}
        return "0.0.0.0";
    }
}
