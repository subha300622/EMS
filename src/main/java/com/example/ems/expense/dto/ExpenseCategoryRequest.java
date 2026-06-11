package com.example.ems.expense.dto;

import jakarta.validation.constraints.NotBlank;

public class ExpenseCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;

    public ExpenseCategoryRequest() {}

    public ExpenseCategoryRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
