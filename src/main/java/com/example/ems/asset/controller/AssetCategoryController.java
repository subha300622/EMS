package com.example.ems.asset.controller;

import com.example.ems.asset.dto.AssetDtos.CategoryResponse;
import com.example.ems.asset.dto.AssetDtos.CreateCategoryRequest;
import com.example.ems.asset.service.AssetCategoryService;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.security.service.JwtService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/asset-categories")
@CrossOrigin("*")
@Tag(name = "Asset Category Operations")
public class AssetCategoryController {

    @Autowired
    private AssetCategoryService categoryService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private User resolveUser(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                return userRepository.findByWorkEmail(email).orElse(null);
            }
        }
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            String principal = SecurityContextHolder.getContext().getAuthentication().getName();
            if (principal != null && !principal.isBlank()) {
                return userRepository.findByWorkEmail(principal).orElse(null);
            }
        }
        return null;
    }

    private Long resolveOrgId(User user) {
        if (user == null || user.getOrganizationId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized or missing organization context");
        }
        return user.getOrganizationId();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ASSET_CATEGORY_CREATE')")
    public ResponseEntity<CategoryResponse> createCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody CreateCategoryRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        CategoryResponse response = categoryService.createCategory(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ASSET_CATEGORY_VIEW')")
    public ResponseEntity<List<CategoryResponse>> getCategories(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        List<CategoryResponse> list = categoryService.getCategories(orgId, activeOnly);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_CATEGORY_VIEW')")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        CategoryResponse response = categoryService.getCategoryById(orgId, id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_CATEGORY_UPDATE')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @Valid @RequestBody CreateCategoryRequest request) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        CategoryResponse response = categoryService.updateCategory(orgId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ASSET_CATEGORY_DELETE')")
    public ResponseEntity<Void> deleteCategory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User user = resolveUser(authHeader);
        Long orgId = resolveOrgId(user);
        categoryService.deleteCategory(orgId, id);
        return ResponseEntity.noContent().build();
    }
}
