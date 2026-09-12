package com.example.ems.auth.service;

import com.example.ems.auth.entity.EmailVerification;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.entity.UserStatus;
import com.example.ems.auth.repository.EmailVerificationRepository;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.organization.entity.OrganizationStatus;
import com.example.ems.organization.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class VerificationService {

    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;

    public VerificationService(EmailVerificationRepository verificationRepository,
                               UserRepository userRepository,
                               OrganizationRepository organizationRepository) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
    }

    @Transactional
    public EmailVerification createVerificationToken(User user) {
        // Invalidate existing token if present
        Optional<EmailVerification> existingToken = verificationRepository.findByUserId(user.getId());
        existingToken.ifPresent(verificationRepository::delete);

        // Generate token valid for 24 hours
        EmailVerification verification = new EmailVerification();
        verification.setUser(user);
        verification.setToken(UUID.randomUUID().toString());
        verification.setExpiresAt(LocalDateTime.now().plusHours(24));
        verification.setAttempts(0);

        return verificationRepository.save(verification);
    }

    @Transactional
    public void verifyEmailToken(String token) {
        EmailVerification verification = verificationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email verification token."));

        if (verification.getVerifiedAt() != null) {
            throw new IllegalStateException("Email has already been verified.");
        }

        if (LocalDateTime.now().isAfter(verification.getExpiresAt())) {
            verification.setAttempts(verification.getAttempts() + 1);
            verificationRepository.save(verification);
            throw new IllegalArgumentException("Verification token has expired.");
        }

        // Complete Verification
        verification.setVerifiedAt(LocalDateTime.now());
        verificationRepository.save(verification);

        // Activate User
        User user = verification.getUser();
        user.setStatus(UserStatus.ACTIVE.name());
        userRepository.save(user);

        // Activate Organization
        Organization organization = user.getOrganization();
        if (organization != null) {
            organization.setStatus(OrganizationStatus.ACTIVE);
            organizationRepository.save(organization);
        }
    }
}
