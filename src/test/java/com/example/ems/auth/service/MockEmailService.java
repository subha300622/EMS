package com.example.ems.auth.service;

import com.example.ems.mail.service.EmailService;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Primary
@Profile("test")
public class MockEmailService extends EmailService {

    private final Map<String, String> sentOtps = new ConcurrentHashMap<>();

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        sentOtps.put(toEmail, otp);
    }

    @Override
    public void sendInvitationEmail(String toEmail, String name, String role, String token) {
        // Bypass
    }

    @Override
    public void sendInvitationEmail(String toEmail, String name, String role, String token, String hrEmail) {
        // Bypass
    }

    @Override
    public void sendInvitationEmail(
            String toEmail,
            String name,
            String role,
            String token,
            String hrEmail,
            String orgName,
            String employeeId,
            String department,
            String designation,
            String joiningDate) {
        // Bypass
    }

    @Override
    public void sendEmail(String toEmail, String subject, String html) {
        // Bypass
    }

    @Override
    public void sendEmail(String toEmail, String subject, String html, String ccEmail) {
        // Bypass
    }

    public String getLastSentOtp(String email) {
        return sentOtps.get(email);
    }

    public void clear() {
        sentOtps.clear();
    }
}
