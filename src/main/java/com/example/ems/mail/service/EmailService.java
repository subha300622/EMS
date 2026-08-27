package com.example.ems.mail.service;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.Message;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String smtpHost;

    @Value("${spring.mail.port:587}")
    private String smtpPort;

    @Value("${spring.mail.username:subashinibalu30@gmail.com}")
    private String smtpUsername;

    @Value("${spring.mail.password:nqokfqkbsychvhfq}")
    private String smtpPassword;

    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

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
                """
                .formatted(otp);

        sendEmail(toEmail, "EMS – Your Password Reset OTP", html);
    }

    /**
     * Sends an invitation email to a new employee.
     */
    public void sendInvitationEmail(String toEmail, String name, String role, String token) {
        sendInvitationEmail(toEmail, name, role, token, null);
    }

    public void sendInvitationEmail(String toEmail, String name, String role, String token, String hrEmail) {
        sendInvitationEmail(toEmail, name, role, token, hrEmail, null, "N/A", "N/A", "N/A", "N/A");
    }

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

        String invitationUrl = "http://localhost:3000/register?token=" + token;
        String resolvedOrgName = (orgName != null && !orgName.isBlank()) ? orgName : "our company";

        String html = """
                <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 550px; margin: auto;
                            padding: 40px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);">
                    <div style="text-align: center; margin-bottom: 24px;">
                        <h2 style="color: #1e3a8a; margin: 0; font-size: 24px; font-weight: 700;">Welcome to %s</h2>
                        <p style="color: #64748b; font-size: 14px; margin-top: 4px;">Activate your employee account</p>
                    </div>

                    <p style="color: #334155; font-size: 16px; line-height: 1.6;">Hello <strong>%s</strong>,</p>
                    <p style="color: #334155; font-size: 16px; line-height: 1.6;">Welcome to <strong>%s</strong>. Your employee account has been created successfully.</p>

                    <div style="background-color: #f8fafc; border-left: 4px solid #3b82f6; padding: 20px; border-radius: 6px; margin: 24px 0;">
                        <table style="width: 100%%; border-collapse: collapse; font-size: 15px; color: #475569;">
                            <tr>
                                <td style="padding: 6px 0; font-weight: 600; width: 130px; color: #1e293b;">Employee ID:</td>
                                <td style="padding: 6px 0;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 6px 0; font-weight: 600; color: #1e293b;">Department:</td>
                                <td style="padding: 6px 0;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 6px 0; font-weight: 600; color: #1e293b;">Designation:</td>
                                <td style="padding: 6px 0;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 6px 0; font-weight: 600; color: #1e293b;">Joining Date:</td>
                                <td style="padding: 6px 0;">%s</td>
                            </tr>
                        </table>
                    </div>

                    <p style="color: #334155; font-size: 16px; line-height: 1.6;">Please activate your account and set up your password using the link below:</p>

                    <div style="text-align: center; margin: 32px 0;">
                        <a href="%s" style="background-color: #1d4ed8; color: #ffffff; padding: 14px 28px; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 16px; display: inline-block; box-shadow: 0 4px 6px -1px rgba(29, 78, 216, 0.3);">
                            Activate Account
                        </a>
                    </div>

                    <p style="color: #64748b; font-size: 14px; line-height: 1.5; text-align: center;">
                        ⏱️ This activation link is valid for <strong>24 hours</strong>.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 32px 0;"/>
                    <p style="color: #64748b; font-size: 13px; line-height: 1.5;">
                        If you have any questions, please contact your organization's HR representative.
                    </p>
                    <p style="color: #64748b; font-size: 13px; margin-top: 8px;">Regards,<br/><strong>%s Team</strong></p>
                </div>
                """
                .formatted(resolvedOrgName, name, resolvedOrgName, employeeId, department, designation, joiningDate,
                        invitationUrl, resolvedOrgName);

        try {
            sendEmail(toEmail, "Welcome to " + resolvedOrgName + " – Activate Your Employee Account", html, hrEmail);
        } catch (Exception e) {
            log.warn("Failed to send JavaMailSender invitation email to {} (keeping invitation active for testing): {}",
                    toEmail, e.getMessage());
        }
    }

    /**
     * Sends verification email to a newly signed up administrator.
     */
    public void sendVerificationEmail(String toEmail, String name, String token) {
        String verificationUrl = baseUrl + "/verify-email?token=" + token;
        String html = """
                <div style="font-family: Arial, sans-serif; max-width: 520px; margin: auto;
                            padding: 32px; border: 1px solid #e5e7eb; border-radius: 10px; background: #fff;">
                    <h2 style="color: #1e293b; margin-bottom: 4px;">✉️ Verify Your Email</h2>
                    <p style="color: #64748b; margin-top: 0;">Hi %s,</p>
                    <p style="color: #64748b;">Welcome to EMS. Click the link below to verify your email and activate your account:</p>

                    <div style="text-align: center; margin: 24px 0;">
                        <a href="%s" style="background: #2563eb; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">
                            Verify Email
                        </a>
                    </div>

                    <p style="color: #64748b; font-size: 14px;">
                        ⏱️ This verification link is valid for <strong>24 hours</strong>.
                    </p>

                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;"/>
                    <p style="color: #94a3b8; font-size: 12px;">— EMS Onboarding Team</p>
                </div>
                """
                .formatted(name, verificationUrl);

        try {
            sendEmail(toEmail, "EMS – Verify your email", html);
        } catch (Exception e) {
            log.warn(
                    "Failed to send JavaMailSender verification email to {} (keeping registration active for testing): {}",
                    toEmail, e.getMessage());
        }
    }

    public void sendEmail(String toEmail, String subject, String html) {
        sendEmail(toEmail, subject, html, null);
    }

    public void sendEmail(String toEmail, String subject, String html, String ccEmail) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Email to {} was not sent: {}", toEmail, subject);
            return;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(smtpUsername);
            helper.setTo(toEmail);
            if (ccEmail != null && !ccEmail.isBlank()) {
                helper.setCc(ccEmail);
            }
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to {} (CC: {}) via JavaMailSender", toEmail, ccEmail);
        } catch (Exception e) {
            log.warn("JavaMailSender error sending to {} (bypassing email transport for local testing): {}", toEmail, e.getMessage());
        }
    }

    /**
     * Sends activation email using Gmail SMTP via JavaMail API.
     */
    public void sendActivationEmailJavaMail(String toEmail, String name, String ccEmail, String token) {
        String from = smtpUsername; // Uses configured username from properties
        String subject = "Activate Your EMS Account";
        String activationLink = "https://ems.company.com/activate?token=" + token;

        String body = "Hello " + name + ",\n\n"
                + "Your EMS account has been created successfully.\n\n"
                + "Please click the link below to activate your account.\n\n"
                + activationLink + "\n\n"
                + "This activation link is valid for 24 hours.\n\n"
                + "If you did not request this account, please ignore this email.\n\n"
                + "Regards,\n"
                + "EMS Team";

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.transport.protocol", "smtp");

        final String password = smtpPassword;

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            if (ccEmail != null && !ccEmail.isBlank()) {
                message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(ccEmail));
            }
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            log.info("Gmail SMTP sent activation email to {} (CC: {}) via JavaMail", toEmail, ccEmail);
        } catch (Exception e) {
            log.warn("Gmail SMTP failed to send activation email to {} (keeping invitation active for testing): {}", toEmail, e.getMessage());
        }
    }
}
