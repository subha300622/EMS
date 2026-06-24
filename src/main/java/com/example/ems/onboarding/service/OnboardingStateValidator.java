package com.example.ems.onboarding.service;

import com.example.ems.common.exception.BadRequestException;

public class OnboardingStateValidator {

    public static void validateTransition(String currentStatus, String targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            return;
        }
        if (currentStatus.equalsIgnoreCase(targetStatus)) {
            return; // No transition
        }
        if ("COMPLETED".equalsIgnoreCase(currentStatus)) {
            throw new BadRequestException("Onboarding is in terminal COMPLETED state. No further updates are permitted.");
        }
        if ("INITIATED".equalsIgnoreCase(currentStatus)) {
            if (!"IN_PROGRESS".equalsIgnoreCase(targetStatus)) {
                throw new BadRequestException("Invalid transition from INITIATED: Can only transition to IN_PROGRESS.");
            }
        } else if ("IN_PROGRESS".equalsIgnoreCase(currentStatus)) {
            if (!"ON_HOLD".equalsIgnoreCase(targetStatus) && !"COMPLETED".equalsIgnoreCase(targetStatus)) {
                throw new BadRequestException("Invalid transition from IN_PROGRESS: Can only transition to ON_HOLD or COMPLETED.");
            }
        } else if ("ON_HOLD".equalsIgnoreCase(currentStatus)) {
            if (!"IN_PROGRESS".equalsIgnoreCase(targetStatus)) {
                throw new BadRequestException("Invalid transition from ON_HOLD: Can only transition back to IN_PROGRESS.");
            }
        }
    }
}
