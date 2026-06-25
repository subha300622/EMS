package com.example.ems.auth.service;

import com.example.ems.common.service.EmailService;
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
        // Bypass Resend API logic to prevent external network calls and failures
    }

    @Override
    public void sendInvitationEmail(String toEmail, String name, String role, String token) {
        // Bypass Resend API
    }

    @Override
    public void sendEmail(String toEmail, String subject, String html) {
        // Bypass Resend API
    }

    public String getLastSentOtp(String email) {
        return sentOtps.get(email);
    }

    public void clear() {
        sentOtps.clear();
    }
}
