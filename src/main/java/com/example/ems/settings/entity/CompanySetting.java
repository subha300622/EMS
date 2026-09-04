package com.example.ems.settings.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "company_settings")
public class CompanySetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String address;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "fiscal_year_start")
    private String fiscalYearStart;

    @Column(name = "logo_url")
    private String logoUrl;

    private String phone;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "tax_id")
    private String taxId;

    private String website;

    public CompanySetting() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getFiscalYearStart() { return fiscalYearStart; }
    public void setFiscalYearStart(String fiscalYearStart) { this.fiscalYearStart = fiscalYearStart; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getTaxId() { return taxId; }
    public void setTaxId(String taxId) { this.taxId = taxId; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
}
