package com.example.ems.common.service;





import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-email}")
    private String fromEmail;

    /**
     * Sends a 6-digit OTP email for password reset.
     */
    public void sendOtpEmail(String toEmail, String otp) {
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 520px; margin: auto;
                            padding: 32px; border: 1px solid #e5e7eb; border-radius: 10px; background: #fff;">
                    <h2 style="color: #1e293b; margin-bottom: 4px;">🔐 Password Reset OTP</h2>
                    <p style="color: #64748b; margin-top: 0;">Use the code below to reset your EMS account password.</p>

                    <div style="background: #f1f5f9; border-radius: 8px; padding: 24px; text-align: center; margin: 24px 0;">
                        <span style="font-size: 42px; font-weight: bold; letter-spacing: 12px; color: #0f172a;">
                            %s
                        </span>
                    </div>

                    <p style="color: #64748b; font-size: 14px;">
                        ⏱️ This OTP is valid for <strong>10 minutes</strong>.<br/>
                        ⚠️ Do not share this OTP with anyone.<br/>
                        🔒 Max 5 attempts allowed.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;"/>
                    <p style="color: #94a3b8; font-size: 12px;">
                        If you didn't request this, please ignore this email. Your account is safe.
                    </p>
                    <p style="color: #94a3b8; font-size: 12px;">— EMS Support Team</p>
                </div>
                """.formatted(otp);

        sendEmail(toEmail, "EMS – Your Password Reset OTP", html);
    }

    /**
     * Sends an invitation email to a new employee.
     */
    public void sendInvitationEmail(String toEmail, String name, String role, String token) {
        String invitationUrl = "http://localhost:3000/register?token=" + token;
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 520px; margin: auto;
                            padding: 32px; border: 1px solid #e5e7eb; border-radius: 10px; background: #fff;">
                    <h2 style="color: #1e293b; margin-bottom: 4px;">✉️ Employee Invitation</h2>
                    <p style="color: #64748b; margin-top: 0;">Hi %s,</p>
                    <p style="color: #64748b;">You have been invited to join the EMS platform as an <strong>%s</strong>.</p>
                    <p style="color: #64748b;">Click the link below to accept the invitation and set your password:</p>

                    <div style="text-align: center; margin: 24px 0;">
                        <a href="%s" style="background: #2563eb; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">
                            Accept Invitation
                        </a>
                    </div>

                    <p style="color: #64748b; font-size: 14px;">
                        ⏱️ This invitation token is valid for <strong>24 hours</strong>.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;"/>
                    <p style="color: #94a3b8; font-size: 12px;">— EMS Onboarding Team</p>
                </div>
                """.formatted(name, role, invitationUrl);

        try {
            sendEmail(toEmail, "EMS – You are Invited!", html);
        } catch (Exception e) {
            log.warn("Failed to send Resend API invitation email to {} (keeping invitation active for testing): {}", toEmail, e.getMessage());
        }
    }

    // ── Internal helper ──────────────────────────────────────────────────────

    public void sendEmail(String toEmail, String subject, String html) {
        Resend resend = new Resend(apiKey);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(toEmail)
                .subject(subject)
                .html(html)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            log.info("Email sent via Resend. ID: {}", response.getId());
        } catch (ResendException e) {
            log.error("Resend API error sending to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
