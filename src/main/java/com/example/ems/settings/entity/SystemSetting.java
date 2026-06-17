package com.example.ems.settings.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "system_settings", indexes = {
    @Index(name = "idx_setting_key", columnList = "settingKey"),
    @Index(name = "idx_setting_category", columnList = "category")
})
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String settingKey;

    @Column(length = 1000)
    private String settingValue;

    @Column(nullable = false)
    private String category;

    public SystemSetting() {}

    public SystemSetting(String settingKey, String settingValue, String category) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
