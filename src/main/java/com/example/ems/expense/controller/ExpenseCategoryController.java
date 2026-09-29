package com.example.ems.expense.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.expense.dto.ExpenseCategoryRequest;
import com.example.ems.expense.entity.ExpenseCategory;
import com.example.ems.expense.service.ExpenseCategoryService;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expense-categories")
@CrossOrigin("*")
@Tag(name = "Expense Management")
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

    @Operation(summary = "Create Expense Category", description = "Creates a new category category (e.g., Travel, Meals) with monthly or yearly budget limits.")
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseCategory>> createCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody @Valid ExpenseCategoryRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "expense.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'expense.manage' permission.", "AUTH_002"));
        }

        try {
            ExpenseCategory created = expenseCategoryService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Expense category created successfully", created));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), "EXP_001"));
        }
    }

    @Operation(summary = "Get All Expense Categories", description = "Retrieves a list of all configured expense categories in the system.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseCategory>>> getAllCategories(
            @RequestHeader(value = "Authorization", required = false) String authHeader){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        List<ExpenseCategory> list = expenseCategoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success("Expense categories retrieved successfully", list));
    }

    @Operation(summary = "Get Expense Category by ID", description = "Retrieves details of a specific expense category by ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseCategory>> getCategoryById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        return expenseCategoryService.getCategoryById(id)
                .map(cat -> ResponseEntity.ok(ApiResponse.success("Expense category retrieved successfully", cat)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Expense category not found with ID: " + id, "EXP_002")));
    }

    @Operation(summary = "Update Expense Category", description = "Updates configurations and budget limits on an existing expense category.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseCategory>> updateCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid ExpenseCategoryRequest request){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "expense.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'expense.manage' permission.", "AUTH_002"));
        }

        try {
            ExpenseCategory updated = expenseCategoryService.updateCategory(id, request);
            return ResponseEntity.ok(ApiResponse.success("Expense category updated successfully", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage(), "EXP_003"));
        }
    }

    @Operation(summary = "Delete Expense Category", description = "Deletes an expense category configuration from the system.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){

        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "expense.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access Denied: Requires 'expense.manage' permission.", "AUTH_002"));
        }

        boolean deleted = expenseCategoryService.deleteCategory(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Expense category deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Expense category not found with ID: " + id, "EXP_002"));
        }
    }
}
