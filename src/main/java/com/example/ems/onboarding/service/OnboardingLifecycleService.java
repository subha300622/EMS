package com.example.ems.onboarding.service;

import com.example.ems.onboarding.entity.Onboarding;
import com.example.ems.onboarding.exception.InvalidOnboardingTransitionException;
import com.example.ems.onboarding.repository.OnboardingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class OnboardingLifecycleService {

    @Autowired
    private OnboardingRepository onboardingRepository;

    @Autowired
    private OnboardingSecurityValidator securityValidator;

    public void validateTransition(String currentStatusRaw, String targetStatusRaw) {
        if (currentStatusRaw == null || targetStatusRaw == null) {
            throw new InvalidOnboardingTransitionException("Status cannot be null");
        }

        String current = currentStatusRaw.toUpperCase().trim();
        String target = targetStatusRaw.toUpperCase().trim();

        if (current.equals(target)) {
            return; // Idempotent same status
        }

        // Terminal states cannot be transitioned away from
        if ("COMPLETED".equals(current) || "CANCELLED".equals(current)) {
            throw new InvalidOnboardingTransitionException(
                    String.format("Cannot transition from terminal state '%s' to '%s'", current, target));
        }

        boolean isValid = false;

        switch (current) {
            case "PRE_JOINING":
            case "PENDING":
            case "INITIATED":
                isValid = Set.of("IN_PROGRESS", "CANCELLED").contains(target);
                break;
            case "IN_PROGRESS":
                isValid = Set.of("PENDING_APPROVAL", "CANCELLED").contains(target);
                break;
            case "PENDING_APPROVAL":
                isValid = Set.of("APPROVED", "IN_PROGRESS", "CANCELLED").contains(target);
                break;
            case "APPROVED":
                isValid = Set.of("COMPLETED", "CANCELLED").contains(target);
                break;
            default:
                isValid = false;
                break;
        }

        if (!isValid) {
            throw new InvalidOnboardingTransitionException(current, target);
        }
    }

    @Transactional
    public Onboarding updateStatus(Long onboardingId, String targetStatus, String remarks) {
        Onboarding onboarding = securityValidator.validateAndGetOnboarding(onboardingId);
        String currentStatus = onboarding.getStatus();

        validateTransition(currentStatus, targetStatus);

        String normalizedTarget = targetStatus.toUpperCase().trim();
        onboarding.setStatus(normalizedTarget);
        onboarding.setUpdatedAt(LocalDateTime.now());

        if ("COMPLETED".equals(normalizedTarget)) {
            onboarding.setCompletionDate(LocalDate.now());
            onboarding.setProgress(100);
        }

        return onboardingRepository.save(onboarding);
    }
}
