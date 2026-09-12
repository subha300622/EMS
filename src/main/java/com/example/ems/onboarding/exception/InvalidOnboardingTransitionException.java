package com.example.ems.onboarding.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidOnboardingTransitionException extends RuntimeException {

    public InvalidOnboardingTransitionException(String message) {
        super(message);
    }

    public InvalidOnboardingTransitionException(String currentStatus, String targetStatus) {
        super(String.format("Invalid status transition from '%s' to '%s'", currentStatus, targetStatus));
    }
}
