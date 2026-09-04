package com.example.ems.support.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.security.service.JwtService;
import com.example.ems.support.dto.*;
import com.example.ems.support.entity.MySupportCategory;
import com.example.ems.support.service.PlatformSupportCategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/platform/support/categories")
@CrossOrigin("*")
@Tag(name = "Platform Admin - Support Categories")
public class PlatformSupportCategoryController {

    @Autowired
    private PlatformSupportCategoryService categoryService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoleService roleService;

    // ── Auth & Permission Helpers ─────────────────────────────────────────────

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

    private boolean checkPermission(User user, String permission) {
        if (user == null) return false;
        return roleService.hasPermission(user.getWorkEmail(), permission) || roleService.isSuperAdmin(user.getWorkEmail());
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<?> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires '" + permission + "' permission.", "AUTH_002"));
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @Operation(summary = "Get Category Dashboard Statistics")
    public ResponseEntity<?> getStats(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.view")) return forbiddenResponse("support.category.view");

        try {
            PlatformCategoryStatsResponse stats = categoryService.getDashboardStats();
            return ResponseEntity.ok(ApiResponse.success("Category stats retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_001"));
        }
    }

    @GetMapping
    @Operation(summary = "Get Categories (Paginated/Filtered Table)")
    public ResponseEntity<?> getCategories(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String order) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.view")) return forbiddenResponse("support.category.view");

        try {
            Pageable pageable = PageRequest.of(page, limit);
            Page<MySupportCategory> resultPage = categoryService.getCategories(
                    search, status, createdBy, createdFrom, createdTo, sortBy, order, pageable
            );

            Map<String, Object> data = new HashMap<>();
            data.put("items", resultPage.getContent());
            
            Map<String, Object> pagination = new HashMap<>();
            pagination.put("page", resultPage.getNumber());
            pagination.put("limit", resultPage.getSize());
            pagination.put("totalItems", resultPage.getTotalElements());
            pagination.put("totalPages", resultPage.getTotalPages());
            data.put("pagination", pagination);

            return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", data));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_002"));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Single Category")
    public ResponseEntity<?> getCategory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.view")) return forbiddenResponse("support.category.view");

        try {
            MySupportCategory cat = categoryService.getCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Category details retrieved", cat));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_003"));
        }
    }

    @PostMapping
    @Operation(summary = "Create Category")
    public ResponseEntity<?> createCategory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody PlatformCategoryRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.create")) return forbiddenResponse("support.category.create");

        try {
            MySupportCategory created = categoryService.createCategory(req, user.getWorkEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Category created successfully", created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_004"));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Category")
    public ResponseEntity<?> updateCategory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody PlatformCategoryRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.update")) return forbiddenResponse("support.category.update");

        try {
            MySupportCategory updated = categoryService.updateCategory(id, req, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Category updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_005"));
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change Category Status")
    public ResponseEntity<?> changeStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long id,
            @RequestParam String status) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.update")) return forbiddenResponse("support.category.update");

        try {
            MySupportCategory updated = categoryService.changeStatus(id, status, user.getWorkEmail());
            return ResponseEntity.ok(ApiResponse.success("Category status changed successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_006"));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Category")
    public ResponseEntity<?> deleteCategory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.delete")) return forbiddenResponse("support.category.delete");

        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_007"));
        }
    }

    @PatchMapping("/reorder")
    @Operation(summary = "Reorder Categories Display Orders")
    public ResponseEntity<?> reorderCategories(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody PlatformCategoryReorderRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.reorder")) return forbiddenResponse("support.category.reorder");

        try {
            List<MySupportCategory> list = categoryService.reorderCategories(req);
            return ResponseEntity.ok(ApiResponse.success("Categories reordered successfully", list));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_008"));
        }
    }

    @GetMapping("/analytics")
    @Operation(summary = "Category Usage Analytics")
    public ResponseEntity<?> getAnalytics(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.view")) return forbiddenResponse("support.category.view");

        try {
            List<PlatformCategoryAnalyticsResponse> analytics = categoryService.getAnalytics();
            return ResponseEntity.ok(ApiResponse.success("Category usage analytics retrieved", analytics));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_009"));
        }
    }

    @GetMapping("/options")
    @Operation(summary = "Category Options Dropdown (Lightweight)")
    public ResponseEntity<?> getOptions(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.view")) return forbiddenResponse("support.category.view");

        try {
            List<PlatformCategoryOption> options = categoryService.getOptions();
            return ResponseEntity.ok(ApiResponse.success("Category dropdown options retrieved", options));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_010"));
        }
    }
}
