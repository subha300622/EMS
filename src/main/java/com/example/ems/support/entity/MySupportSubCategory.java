package com.example.ems.support.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "my_support_sub_categories")
public class MySupportSubCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private MySupportCategory category;

    public MySupportSubCategory() {}

    public MySupportSubCategory(Long id, String name, MySupportCategory category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public MySupportCategory getCategory() { return category; }
    public void setCategory(MySupportCategory category) { this.category = category; }
}
