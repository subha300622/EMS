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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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
        return roleService.hasPermission(user.getWorkEmail(), permission)
                || roleService.isSuperAdmin(user.getWorkEmail())
                || (user.getRole() != null && "PLATFORM_ADMIN".equalsIgnoreCase(user.getRole().getName()));
    }

    private ResponseEntity<?> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.error("Unauthorized", "AUTH_014"));
    }

    private ResponseEntity<?> forbiddenResponse(String permission) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.error("Access Denied: Requires '" + permission + "' permission.", "AUTH_002"));
    }

    private String formatOffsetDateTime(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private PlatformCategoryResponse toDetailResponse(MySupportCategory cat) {
        return new PlatformCategoryResponse(
                cat.getId(),
                cat.getName(),
                cat.getDescription(),
                cat.getStatus() != null ? cat.getStatus().name() : null,
                cat.getDisplayOrder(),
                cat.getIsDefault() != null ? cat.getIsDefault() : false,
                cat.getIsSystem() != null ? cat.getIsSystem() : false,
                formatOffsetDateTime(cat.getCreatedAt()),
                formatOffsetDateTime(cat.getUpdatedAt())
        );
    }

    private PlatformCategoryUpdateResponse toUpdateResponse(MySupportCategory cat) {
        return new PlatformCategoryUpdateResponse(
                cat.getId(),
                cat.getName(),
                cat.getDescription(),
                cat.getStatus() != null ? cat.getStatus().name() : null,
                cat.getDisplayOrder(),
                cat.getIsDefault() != null ? cat.getIsDefault() : false,
                cat.getIsSystem() != null ? cat.getIsSystem() : false,
                formatOffsetDateTime(cat.getUpdatedAt())
        );
    }

    private PlatformCategoryListItemResponse toListItemResponse(MySupportCategory cat) {
        return new PlatformCategoryListItemResponse(
                cat.getId(),
                cat.getName(),
                cat.getDescription(),
                cat.getStatus() != null ? cat.getStatus().name() : null,
                cat.getDisplayOrder(),
                cat.getIsDefault() != null ? cat.getIsDefault() : false,
                cat.getIsSystem() != null ? cat.getIsSystem() : false
        );
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 1. Create Category (POST /api/platform/support/categories)
    // ─────────────────────────────────────────────────────────────────────────────
    @PostMapping
    @Operation(summary = "Create Category", description = "Creates a new support category")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Category created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlatformCategoryResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request or duplicate category name",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<?> createCategory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody PlatformCategoryRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.create")) return forbiddenResponse("support.category.create");

        try {
            MySupportCategory created = categoryService.createCategory(req, user.getWorkEmail());
            PlatformCategoryResponse response = toDetailResponse(created);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_004"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 2. Get Categories (GET /api/platform/support/categories)
    // ─────────────────────────────────────────────────────────────────────────────
    @GetMapping
    @Operation(summary = "Get Categories", description = "Get paginated categories list")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Categories retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlatformCategoryPageResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid parameters",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<?> getCategories(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", required = false) Integer size,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "createdBy", required = false) String createdBy,
            @RequestParam(value = "createdFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
            @RequestParam(value = "createdTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "order", required = false) String order) {

        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.view")) return forbiddenResponse("support.category.view");

        try {
            int pageSize = (size != null) ? size : ((limit != null) ? limit : 20);
            Pageable pageable = PageRequest.of(page, pageSize);
            Page<MySupportCategory> resultPage = categoryService.getCategories(
                    search, status, createdBy, createdFrom, createdTo, sortBy, order, pageable
            );

            List<PlatformCategoryListItemResponse> content = resultPage.getContent().stream()
                    .map(this::toListItemResponse)
                    .collect(Collectors.toList());

            PlatformCategoryPageResponse pageResponse = new PlatformCategoryPageResponse(
                    content,
                    resultPage.getNumber(),
                    resultPage.getSize(),
                    resultPage.getTotalElements(),
                    resultPage.getTotalPages()
            );

            return ResponseEntity.ok(pageResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_002"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 3. Get Single Category (GET /api/platform/support/categories/{id})
    // ─────────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Get Single Category", description = "Retrieves category by ID")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Category details retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlatformCategoryResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "Category not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<?> getCategory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.view")) return forbiddenResponse("support.category.view");

        try {
            MySupportCategory cat = categoryService.getCategory(id);
            return ResponseEntity.ok(toDetailResponse(cat));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.error(e.getMessage(), "CAT_003"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 4. Update Category (PUT /api/platform/support/categories/{id})
    // ─────────────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @Operation(summary = "Update Category", description = "Updates category name and description only")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Category updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlatformCategoryUpdateResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<?> updateCategory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id,
            @Valid @RequestBody PlatformCategoryRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.update")) return forbiddenResponse("support.category.update");

        try {
            MySupportCategory updated = categoryService.updateCategory(id, req, user.getWorkEmail());
            PlatformCategoryUpdateResponse response = toUpdateResponse(updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_005"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 5. Change Category Status (PATCH /api/platform/support/categories/{id}/status)
    // ─────────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/status")
    @Operation(summary = "Change Category Status", description = "Change category status to ACTIVE or INACTIVE")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Category status updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlatformCategoryStatusResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid status value",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<?> changeStatus(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id,
            @RequestBody(required = false) CategoryStatusUpdateRequest body,
            @Parameter(hidden = true) @RequestParam(value = "status", required = false) String statusParam) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.update")) return forbiddenResponse("support.category.update");

        String status = (body != null && body.getStatus() != null && !body.getStatus().isBlank())
                ? body.getStatus() : statusParam;

        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Status is required", "CAT_006"));
        }

        try {
            MySupportCategory updated = categoryService.changeStatus(id, status, user.getWorkEmail());
            PlatformCategoryStatusResponse response = new PlatformCategoryStatusResponse(
                    updated.getId(),
                    updated.getName(),
                    updated.getStatus() != null ? updated.getStatus().name() : null,
                    formatOffsetDateTime(updated.getUpdatedAt())
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_006"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 6. Delete Category (DELETE /api/platform/support/categories/{id})
    // ─────────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Category", description = "Soft deletes a category if not in use by tickets")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Category deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Category is assigned to tickets or is system protected",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<?> deleteCategory(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @PathVariable("id") Long id) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.delete")) return forbiddenResponse("support.category.delete");

        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_007"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 7. Category Options (GET /api/platform/support/categories/options)
    // ─────────────────────────────────────────────────────────────────────────────
    @GetMapping("/options")
    @Operation(summary = "Category Options", description = "Optimized category list for ticket creation form")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Category options retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = PlatformCategoryOption.class))
            )
        )
    })
    public ResponseEntity<?> getOptions(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.view")) return forbiddenResponse("support.category.view");

        try {
            List<PlatformCategoryOption> options = categoryService.getOptions();
            return ResponseEntity.ok(options);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_010"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // 8. Reorder Categories (PATCH /api/platform/support/categories/reorder)
    // ─────────────────────────────────────────────────────────────────────────────
    @PatchMapping("/reorder")
    @Operation(summary = "Reorder Categories", description = "Reorders category display order")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Categories reordered successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PlatformCategoryReorderResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Invalid order or duplicate IDs",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    public ResponseEntity<?> reorderCategories(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody PlatformCategoryReorderRequest req) {
        User user = resolveUser(authHeader);
        if (user == null) return unauthorizedResponse();
        if (!checkPermission(user, "support.category.reorder")) return forbiddenResponse("support.category.reorder");

        try {
            categoryService.reorderCategories(req);
            return ResponseEntity.ok(new PlatformCategoryReorderResponse("Categories reordered successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ErrorResponse.error(e.getMessage(), "CAT_008"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Auxiliary: Stats & Analytics (Preserved)
    // ─────────────────────────────────────────────────────────────────────────────
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
}
