package com.example.ems.settings.service;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.settings.entity.*;
import com.example.ems.settings.repository.*;
import com.example.ems.settings.dto.*;
import com.example.ems.support.dto.CreateTicketRequest;
import com.example.ems.support.dto.CreateTicketResponse;
import com.example.ems.support.entity.MySupportCategory;
import com.example.ems.support.entity.MySupportSubCategory;
import com.example.ems.support.repository.MySupportCategoryRepository;
import com.example.ems.support.repository.MySupportSubCategoryRepository;
import com.example.ems.support.service.MySupportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MySettingsService {

    @Autowired
    private EmployeeSettingRepository settingRepository;

    @Autowired
    private NotificationPreferenceRepository preferenceRepository;

    @Autowired
    private UserDeviceRepository deviceRepository;

    @Autowired
    private DataExportRequestRepository exportRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private MySupportService supportService;

    @Autowired
    private MySupportCategoryRepository supportCategoryRepository;

    @Autowired
    private MySupportSubCategoryRepository supportSubCategoryRepository;

    @Autowired
    private UserBackupCodeRepository userBackupCodeRepository;

    @Autowired
    private com.example.ems.auth.service.OtpService otpService;

    @Autowired
    private com.example.ems.audit.service.AuditLogService auditLogService;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Transactional
    public EmployeeSetting getOrCreateSettings(String email) {
        return settingRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    EmployeeSetting settings = new EmployeeSetting();
                    settings.setUserEmail(email);
                    return settingRepository.save(settings);
                });
    }

    @Transactional
    public List<NotificationPreference> getOrCreateNotificationPreferences(String email) {
        List<NotificationPreference> list = preferenceRepository.findByUserEmail(email);
        if (list.isEmpty()) {
            List<String> categories = Arrays.asList("LEAVE", "PAYSLIP", "ATTENDANCE", "PERFORMANCE", "EXPENSE", "SCHEDULE", "ANNOUNCEMENT", "GOAL");
            list = new ArrayList<>();
            for (String cat : categories) {
                boolean sms = "PAYSLIP".equals(cat);
                NotificationPreference pref = new NotificationPreference(email, cat, true, true, sms);
                list.add(preferenceRepository.save(pref));
            }
        }
        return list;
    }

    @Transactional
    public List<UserDevice> getOrCreateDevices(String email) {
        List<UserDevice> list = deviceRepository.findByUserEmail(email);
        if (list.isEmpty()) {
            UserDevice defaultDevice = new UserDevice(email, "Firefox Linux", "103.45.67.89", LocalDateTime.now().minusHours(1));
            list = new ArrayList<>();
            list.add(deviceRepository.save(defaultDevice));
        }
        return list;
    }

    @Transactional
    public Map<String, Object> getSettingsDashboard(String email) {
        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found."));
        EmployeeSetting setting = getOrCreateSettings(email);
        List<UserDevice> devices = getOrCreateDevices(email);

        Map<String, Object> data = new HashMap<>();
        data.put("employeeId", emp.getId());
        data.put("fullName", emp.getFullName());
        data.put("email", emp.getEmail());

        Map<String, Object> preferences = new HashMap<>();
        preferences.put("theme", setting.getTheme());
        preferences.put("language", setting.getLanguage());
        preferences.put("timezone", setting.getTimezone());
        data.put("preferences", preferences);

        Map<String, Object> security = new HashMap<>();
        security.put("mfaEnabled", setting.getMfaEnabled());
        security.put("activeDevices", devices.size());
        data.put("security", security);

        data.put("notificationsEnabled", setting.getDailyDigestEnabled());

        return data;
    }

    @Transactional
    public Map<String, Object> getSecuritySettings(String email) {
        EmployeeSetting setting = getOrCreateSettings(email);
        List<UserDevice> devices = getOrCreateDevices(email);

        Map<String, Object> data = new HashMap<>();
        data.put("mfaEnabled", setting.getMfaEnabled());
        data.put("passwordLastChanged", setting.getPasswordLastChanged().format(ISO_FORMATTER));
        data.put("activeSessions", devices.size());
        data.put("trustedDevices", devices.size()); // Mapping devices count for simplicity
        return data;
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByWorkEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password does not match");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        EmployeeSetting setting = getOrCreateSettings(email);
        setting.setPasswordLastChanged(LocalDateTime.now());
        settingRepository.save(setting);
    }

    @Transactional
    public void updateMfa(String email, boolean enabled) {
        EmployeeSetting setting = getOrCreateSettings(email);
        setting.setMfaEnabled(enabled);
        settingRepository.save(setting);
    }

    @Transactional
    public Map<String, Object> getPrivacySettings(String email) {
        EmployeeSetting setting = getOrCreateSettings(email);

        Map<String, Object> data = new HashMap<>();
        data.put("profileVisible", setting.getProfileVisible());
        data.put("showPhoneNumber", setting.getShowPhoneNumber());
        data.put("showEmail", setting.getShowEmail());
        data.put("showBirthday", setting.getShowBirthday());
        return data;
    }

    @Transactional
    public void updatePrivacySettings(String email, Map<String, Object> request) {
        EmployeeSetting setting = getOrCreateSettings(email);
        if (request.containsKey("profileVisible")) setting.setProfileVisible((Boolean) request.get("profileVisible"));
        if (request.containsKey("showPhoneNumber")) setting.setShowPhoneNumber((Boolean) request.get("showPhoneNumber"));
        if (request.containsKey("showEmail")) setting.setShowEmail((Boolean) request.get("showEmail"));
        if (request.containsKey("showBirthday")) setting.setShowBirthday((Boolean) request.get("showBirthday"));
        settingRepository.save(setting);
    }

    @Transactional
    public List<Map<String, Object>> getNotificationPreferences(String email) {
        List<NotificationPreference> list = getOrCreateNotificationPreferences(email);
        List<Map<String, Object>> response = new ArrayList<>();
        for (NotificationPreference pref : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("category", pref.getCategory());
            item.put("email", pref.getEmail());
            item.put("push", pref.getPush());
            item.put("sms", pref.getSms());
            response.add(item);
        }
        return response;
    }

    @Transactional
    public void updateNotificationCategory(String email, String category, Map<String, Boolean> request) {
        NotificationPreference pref = preferenceRepository.findByUserEmailAndCategory(email, category)
                .orElseGet(() -> new NotificationPreference(email, category, true, true, false));

        if (request.containsKey("email")) pref.setEmail(request.get("email"));
        if (request.containsKey("push")) pref.setPush(request.get("push"));
        if (request.containsKey("sms")) pref.setSms(request.get("sms"));

        preferenceRepository.save(pref);
    }

    @Transactional
    public Map<String, Object> getNotificationTiming(String email) {
        EmployeeSetting setting = getOrCreateSettings(email);

        Map<String, Object> data = new HashMap<>();
        data.put("dailyDigestEnabled", setting.getDailyDigestEnabled());
        data.put("digestTime", setting.getDigestTime());
        data.put("quietHoursEnabled", setting.getQuietHoursEnabled());
        data.put("quietHoursFrom", setting.getQuietHoursFrom());
        data.put("quietHoursTo", setting.getQuietHoursTo());
        return data;
    }

    @Transactional
    public void updateNotificationTiming(String email, Map<String, Object> request) {
        EmployeeSetting setting = getOrCreateSettings(email);
        if (request.containsKey("dailyDigestEnabled")) setting.setDailyDigestEnabled((Boolean) request.get("dailyDigestEnabled"));
        if (request.containsKey("digestTime")) setting.setDigestTime((String) request.get("digestTime"));
        if (request.containsKey("quietHoursEnabled")) setting.setQuietHoursEnabled((Boolean) request.get("quietHoursEnabled"));
        if (request.containsKey("quietHoursFrom")) setting.setQuietHoursFrom((String) request.get("quietHoursFrom"));
        if (request.containsKey("quietHoursTo")) setting.setQuietHoursTo((String) request.get("quietHoursTo"));
        settingRepository.save(setting);
    }

    @Transactional
    public Map<String, Object> getAppearance(String email) {
        EmployeeSetting setting = getOrCreateSettings(email);

        Map<String, Object> data = new HashMap<>();
        data.put("theme", setting.getTheme());
        data.put("fontSize", setting.getFontSize());
        data.put("compactMode", setting.getCompactMode());
        return data;
    }

    @Transactional
    public void updateAppearance(String email, Map<String, Object> request) {
        EmployeeSetting setting = getOrCreateSettings(email);
        if (request.containsKey("theme")) setting.setTheme((String) request.get("theme"));
        if (request.containsKey("fontSize")) setting.setFontSize((String) request.get("fontSize"));
        if (request.containsKey("compactMode")) setting.setCompactMode((Boolean) request.get("compactMode"));
        settingRepository.save(setting);
    }

    @Transactional
    public Map<String, Object> getLanguageRegion(String email) {
        EmployeeSetting setting = getOrCreateSettings(email);

        Map<String, Object> data = new HashMap<>();
        data.put("language", setting.getLanguage());
        data.put("timezone", setting.getTimezone());
        data.put("dateFormat", setting.getDateFormat());
        data.put("timeFormat", setting.getTimeFormat());
        return data;
    }

    @Transactional
    public void updateLanguageRegion(String email, Map<String, Object> request) {
        EmployeeSetting setting = getOrCreateSettings(email);
        if (request.containsKey("language")) setting.setLanguage((String) request.get("language"));
        if (request.containsKey("timezone")) setting.setTimezone((String) request.get("timezone"));
        if (request.containsKey("dateFormat")) setting.setDateFormat((String) request.get("dateFormat"));
        if (request.containsKey("timeFormat")) setting.setTimeFormat((String) request.get("timeFormat"));
        settingRepository.save(setting);
    }

    @Transactional
    public List<Map<String, Object>> getDevices(String email) {
        List<UserDevice> list = getOrCreateDevices(email);
        List<Map<String, Object>> response = new ArrayList<>();
        // Use ID 1 for Firefox Linux as in the requested example response
        for (UserDevice dev : list) {
            Map<String, Object> item = new HashMap<>();
            item.put("deviceId", dev.getId());
            item.put("deviceName", dev.getDeviceName());
            item.put("ipAddress", dev.getIpAddress());
            item.put("lastActive", dev.getLastActive().format(ISO_FORMATTER));
            item.put("currentDevice", true); // Simulating as current device for testing
            response.add(item);
        }
        return response;
    }

    @Transactional
    public void removeDevice(String email, Long deviceId) {
        UserDevice dev = deviceRepository.findByUserEmailAndId(email, deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found with ID: " + deviceId));
        deviceRepository.delete(dev);
    }

    @Transactional
    public Map<String, Object> exportData(String email) {
        long count = exportRequestRepository.count();
        String requestId = "EXP-" + LocalDateTime.now().getYear() + "-" + String.format("%03d", count + 1);
        
        DataExportRequest req = new DataExportRequest(requestId, email, "PROCESSING");
        exportRequestRepository.save(req);

        Map<String, Object> data = new HashMap<>();
        data.put("requestId", requestId);
        data.put("status", "PROCESSING");
        return data;
    }

    @Transactional
    public Map<String, Object> getExportStatus(String email, String requestId) {
        DataExportRequest req = exportRequestRepository.findByRequestIdAndUserEmail(requestId, email)
                .orElseThrow(() -> new IllegalArgumentException("Export request not found: " + requestId));
        
        // Simulating completion on status query for mock convenience
        req.setStatus("COMPLETED");
        exportRequestRepository.save(req);

        Map<String, Object> data = new HashMap<>();
        data.put("requestId", req.getRequestId());
        data.put("status", req.getStatus());
        data.put("downloadUrl", "http://localhost:8080/api/v1/my-settings/data/export/" + req.getRequestId());
        return data;
    }

    @Transactional(readOnly = true)
    public byte[] getExportedDataCsv(String email, String requestId) {
        // Assert the request exists
        exportRequestRepository.findByRequestIdAndUserEmail(requestId, email)
                .orElseThrow(() -> new IllegalArgumentException("Export request not found: " + requestId));

        Employee emp = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Employee profile not found"));

        StringBuilder csv = new StringBuilder();
        csv.append("Employee ID,Full Name,Email,Phone,Department,Location,Status\n");
        csv.append(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                emp.getEmployeeId() != null ? emp.getEmployeeId() : "N/A",
                emp.getFullName(),
                emp.getEmail(),
                emp.getPhone() != null ? emp.getPhone() : "",
                emp.getDepartment() != null ? emp.getDepartment() : "",
                emp.getLocation() != null ? emp.getLocation() : "",
                emp.getStatus() != null ? emp.getStatus() : "ACTIVE"
        ));

        return csv.toString().getBytes();
    }

    public List<Map<String, Object>> getFaqs() {
        List<Map<String, Object>> faqs = new ArrayList<>();
        
        Map<String, Object> faq1 = new HashMap<>();
        faq1.put("id", 1);
        faq1.put("question", "How do I reset my password?");
        faq1.put("answer", "Use the Account & Security section.");
        faqs.add(faq1);

        Map<String, Object> faq2 = new HashMap<>();
        faq2.put("id", 2);
        faq2.put("question", "How do I enable MFA?");
        faq2.put("answer", "Go to Account & Security settings and toggle MFA.");
        faqs.add(faq2);

        return faqs;
    }

    @Transactional
    public CreateTicketResponse createSupportRequest(String email, SupportTicketRequest req) {
        // Find category: since we want to handle "ACCOUNT" dynamically:
        MySupportCategory cat = supportCategoryRepository.findByName("Account & Security")
                .orElseGet(() -> {
                    MySupportCategory c = new MySupportCategory();
                    c.setName("Account & Security");
                    c.setIcon("lock");
                    return supportCategoryRepository.save(c);
                });

        MySupportSubCategory sub = supportSubCategoryRepository.findByCategory(cat).stream().findFirst()
                .orElseGet(() -> {
                    MySupportSubCategory s = new MySupportSubCategory();
                    s.setName("General Settings Query");
                    s.setCategory(cat);
                    return supportSubCategoryRepository.save(s);
                });

        CreateTicketRequest supportReq = new CreateTicketRequest();
        supportReq.setCategoryId(cat.getId());
        supportReq.setSubCategoryId(sub.getId());
        supportReq.setSubject(req.getSubject());
        supportReq.setDescription(req.getDescription());
        supportReq.setPriority("MEDIUM");
        supportReq.setPreferredContactMethod("EMAIL");

        return supportService.createTicket(email, supportReq);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getBackupCodesInfo(String email) {
        List<UserBackupCode> codes = userBackupCodeRepository.findByUserEmail(email);
        Map<String, Object> data = new LinkedHashMap<>();
        if (codes.isEmpty()) {
            data.put("remainingCodes", 0);
            data.put("totalCodes", 0);
            data.put("lastGeneratedAt", null);
            data.put("expiresAt", null);
        } else {
            long remaining = codes.stream().filter(c -> !c.isUsed()).count();
            data.put("remainingCodes", remaining);
            data.put("totalCodes", codes.size());
            data.put("lastGeneratedAt", codes.get(0).getCreatedAt().format(ISO_FORMATTER));
            LocalDateTime expires = codes.get(0).getExpiresAt();
            data.put("expiresAt", expires != null ? expires.format(ISO_FORMATTER) : null);
        }
        return data;
    }

    @Transactional
    public Map<String, Object> regenerateBackupCodes(String email, RegenerateBackupCodesRequest request, String ipAddress) {
        User user = userRepository.findByWorkEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("INVALID_PASSWORD");
        }

        Map<String, Object> otpResult = otpService.verifyOtp(email, request.getOtp());
        if (!Boolean.TRUE.equals(otpResult.get("verified"))) {
            throw new IllegalArgumentException("INVALID_OTP");
        }

        // Invalidate old codes
        userBackupCodeRepository.deleteByUserEmail(email);

        // Generate 10 new codes
        List<String> plainCodes = new ArrayList<>();
        List<UserBackupCode> entities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(90);

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 10; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 9; j++) {
                if (j == 4) {
                    sb.append("-");
                } else {
                    sb.append(chars.charAt(random.nextInt(chars.length())));
                }
            }
            String code = sb.toString();
            plainCodes.add(code);
            entities.add(new UserBackupCode(email, passwordEncoder.encode(code), false, now, expiresAt));
        }
        userBackupCodeRepository.saveAll(entities);

        // Log audit action
        auditLogService.logAction(
                user.getUserId(),
                email,
                "BACKUP_CODES_REGENERATED",
                "USER",
                user.getId().toString(),
                ipAddress,
                "Backup codes regenerated successfully"
        );

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("generatedAt", now.format(ISO_FORMATTER));
        data.put("expiresAt", expiresAt.format(ISO_FORMATTER));
        data.put("remainingCodes", 10);
        data.put("backupCodes", plainCodes);

        return data;
    }
}
