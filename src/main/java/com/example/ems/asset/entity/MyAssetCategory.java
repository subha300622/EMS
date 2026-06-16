package com.example.ems.asset.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "my_asset_categories")
public class MyAssetCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int maximumAllowed = 1;

    @Column(nullable = false)
    private boolean requestEnabled = true;

    public MyAssetCategory() {}

    public MyAssetCategory(String code, String name, int maximumAllowed, boolean requestEnabled) {
        this.code = code;
        this.name = name;
        this.maximumAllowed = maximumAllowed;
        this.requestEnabled = requestEnabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaximumAllowed() {
        return maximumAllowed;
    }

    public void setMaximumAllowed(int maximumAllowed) {
        this.maximumAllowed = maximumAllowed;
    }

    public boolean isRequestEnabled() {
        return requestEnabled;
    }

    public void setRequestEnabled(boolean requestEnabled) {
        this.requestEnabled = requestEnabled;
    }
}
