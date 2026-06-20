package com.example.ems.expense.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.expense.dto.ExpenseCategoryRequest;
import com.example.ems.expense.entity.ExpenseCategory;
import com.example.ems.expense.service.ExpenseCategoryService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expense-categories")
@CrossOrigin("*")
@Tag(name = "Finance Management")
public class ExpenseCategoryController {

    @Autowired
    private ExpenseCategoryService expenseCategoryService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        return null;
    }

    @PostMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid ExpenseCategoryRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "expense.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'expense.manage' permission.", "AUTH_002"));
        }

        try {
            ExpenseCategory created = expenseCategoryService.createCategory(request);
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Expense category created successfully", created));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "EXP_001"));
        }
    }

    @GetMapping
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getAllCategories(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        List<ExpenseCategory> list = expenseCategoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Expense categories retrieved successfully", list));
    }

    @GetMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getCategoryById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        return (ResponseEntity) expenseCategoryService.getCategoryById(id)
                .<ResponseEntity<?>>map(cat -> ResponseEntity.ok(ApiResponse.success("Expense category retrieved successfully", cat)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponse.error("Expense category not found with ID: " + id, "EXP_002")));
    }

    @PutMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid ExpenseCategoryRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "expense.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'expense.manage' permission.", "AUTH_002"));
        }

        try {
            ExpenseCategory updated = expenseCategoryService.updateCategory(id, request);
            return ResponseEntity.ok(ApiResponse.success("Expense category updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "EXP_003"));
        }
    }

    @DeleteMapping("/{id}")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "expense.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'expense.manage' permission.", "AUTH_002"));
        }

        boolean deleted = expenseCategoryService.deleteCategory(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Expense category deleted successfully"));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Expense category not found with ID: " + id, "EXP_002"));
        }
    }
}
