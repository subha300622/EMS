package com.example.ems.expense.service;

import com.example.ems.expense.dto.ExpenseCategoryRequest;
import com.example.ems.expense.entity.ExpenseCategory;
import com.example.ems.expense.repository.ExpenseCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ExpenseCategoryService {

    @Autowired
    private ExpenseCategoryRepository expenseCategoryRepository;

    @Transactional
    public ExpenseCategory createCategory(ExpenseCategoryRequest request) {
        if (expenseCategoryRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Expense category with name '" + request.getName() + "' already exists");
        }
        ExpenseCategory category = new ExpenseCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return expenseCategoryRepository.save(category);
    }

    public List<ExpenseCategory> getAllCategories() {
        return expenseCategoryRepository.findAll();
    }

    public Optional<ExpenseCategory> getCategoryById(Long id) {
        return expenseCategoryRepository.findById(id);
    }

    @Transactional
    public ExpenseCategory updateCategory(Long id, ExpenseCategoryRequest request) {
        ExpenseCategory category = expenseCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Expense category not found with ID: " + id));

        Optional<ExpenseCategory> existing = expenseCategoryRepository.findByName(request.getName());
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new IllegalArgumentException("Expense category with name '" + request.getName() + "' already exists");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        return expenseCategoryRepository.save(category);
    }

    @Transactional
    public boolean deleteCategory(Long id) {
        if (expenseCategoryRepository.existsById(id)) {
            expenseCategoryRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
