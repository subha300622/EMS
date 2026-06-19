package com.example.ems.settings.service;

import com.example.ems.settings.entity.SystemSetting;
import com.example.ems.settings.repository.SystemSettingRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class SystemSettingService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    @org.springframework.beans.factory.annotation.Value("${resend.from-email:EMS System <noreply@company.com>}")
    private String fromEmailProperty;

    @org.springframework.beans.factory.annotation.Value("${app.email.sender-address:noreply@company.com}")
    private String defaultSenderAddress;

    @PostConstruct
    public void initDefaultSettings() {
        // Seed default system configurations if they do not exist
        Map<String, String[]> defaults = new HashMap<>();
        defaults.put("company.name", new String[]{"Enterprise EMS Inc.", "company"});
        defaults.put("company.address", new String[]{"123 Tech Corporate Park", "company"});
        defaults.put("company.country", new String[]{"United States", "company"});
        defaults.put("security.mfa_enabled", new String[]{"false", "security"});
        defaults.put("security.session_timeout", new String[]{"3600", "security"});
        defaults.put("email.smtp_host", new String[]{"smtp.resend.com", "email"});
        defaults.put("email.smtp_port", new String[]{"587", "email"});
        String senderEmail = defaultSenderAddress;
        if (fromEmailProperty != null) {
            if (fromEmailProperty.contains("<") && fromEmailProperty.contains(">")) {
                senderEmail = fromEmailProperty.substring(fromEmailProperty.indexOf("<") + 1, fromEmailProperty.indexOf(">")).trim();
            } else {
                senderEmail = fromEmailProperty.trim();
            }
        }
        defaults.put("email.sender_address", new String[]{senderEmail, "email"});
        defaults.put("integrations.slack_webhook", new String[]{"https://hooks.slack.com/services/...", "integrations"});
        defaults.put("integrations.jira_base_url", new String[]{"https://jira.enterprise.com", "integrations"});
        defaults.put("password-policy.min_length", new String[]{"8", "password-policy"});
        defaults.put("password-policy.require_special_char", new String[]{"true", "password-policy"});

        for (Map.Entry<String, String[]> entry : defaults.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue()[0];
            String cat = entry.getValue()[1];
            if (systemSettingRepository.findBySettingKey(key).isEmpty()) {
                systemSettingRepository.save(new SystemSetting(key, val, cat));
            }
        }
    }

    @Transactional(readOnly = true)
    public List<SystemSetting> getSettingsByCategory(String category) {
        return systemSettingRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<SystemSetting> getAllSettings() {
        return systemSettingRepository.findAll();
    }

    public SystemSetting updateSetting(String key, String value, String category) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElse(new SystemSetting(key, value, category));
        setting.setSettingValue(value);
        setting.setCategory(category);
        return systemSettingRepository.save(setting);
    }

    @Transactional(readOnly = true)
    public String getSettingValue(String key, String defaultValue) {
        return systemSettingRepository.findBySettingKey(key)
                .map(SystemSetting::getSettingValue)
                .orElse(defaultValue);
    }
}
