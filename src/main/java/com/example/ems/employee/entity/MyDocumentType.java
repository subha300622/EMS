package com.example.ems.employee.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "my_document_types")
public class MyDocumentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private MyDocumentCategory category;

    @Column(nullable = false)
    private boolean mandatory;

    @Column(nullable = false)
    private boolean requiresExpiryDate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "my_document_type_formats", joinColumns = @JoinColumn(name = "document_type_id"))
    @Column(name = "format")
    private List<String> allowedFormats;

    @Column(nullable = false)
    private int maxFileSizeInMB;

    public MyDocumentType() {}

    public MyDocumentType(String code, String name, MyDocumentCategory category, boolean mandatory, boolean requiresExpiryDate, List<String> allowedFormats, int maxFileSizeInMB) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.mandatory = mandatory;
        this.requiresExpiryDate = requiresExpiryDate;
        this.allowedFormats = allowedFormats;
        this.maxFileSizeInMB = maxFileSizeInMB;
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

    public MyDocumentCategory getCategory() {
        return category;
    }

    public void setCategory(MyDocumentCategory category) {
        this.category = category;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isRequiresExpiryDate() {
        return requiresExpiryDate;
    }

    public void setRequiresExpiryDate(boolean requiresExpiryDate) {
        this.requiresExpiryDate = requiresExpiryDate;
    }

    public List<String> getAllowedFormats() {
        return allowedFormats;
    }

    public void setAllowedFormats(List<String> allowedFormats) {
        this.allowedFormats = allowedFormats;
    }

    public int getMaxFileSizeInMB() {
        return maxFileSizeInMB;
    }

    public void setMaxFileSizeInMB(int maxFileSizeInMB) {
        this.maxFileSizeInMB = maxFileSizeInMB;
    }
}
