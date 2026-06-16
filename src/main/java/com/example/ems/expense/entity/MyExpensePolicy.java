package com.example.ems.expense.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "my_expense_policies")
public class MyExpensePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    @Column(nullable = false)
    private String version;

    public MyExpensePolicy() {}

    public MyExpensePolicy(String title, String description, LocalDate effectiveFrom, String version) {
        this.title = title;
        this.description = description;
        this.effectiveFrom = effectiveFrom;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
