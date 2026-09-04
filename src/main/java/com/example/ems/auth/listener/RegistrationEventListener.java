package com.example.ems.auth.listener;

import com.example.ems.auth.event.UserRegisteredEvent;
import com.example.ems.mail.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RegistrationEventListener {

    private static final Logger log = LoggerFactory.getLogger(RegistrationEventListener.class);
    private final EmailService emailService;

    public RegistrationEventListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Handling user registered event for user: {} with token: {}", event.getUser().getWorkEmail(), event.getToken());
        try {
            emailService.sendVerificationEmail(
                event.getUser().getWorkEmail(),
                event.getUser().getFullName(),
                event.getToken()
            );
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", event.getUser().getWorkEmail(), e.getMessage());
        }
    }
}
