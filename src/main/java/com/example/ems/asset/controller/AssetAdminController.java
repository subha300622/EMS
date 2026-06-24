package com.example.ems.asset.controller;

import com.example.ems.asset.dto.AssetDto;
import com.example.ems.asset.dto.AssetStatusRequest;
import com.example.ems.asset.entity.MyAsset;
import com.example.ems.asset.repository.MyAssetRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.security.service.JwtService;

import com.example.ems.asset.dto.AssetDetailResponse;
import com.example.ems.asset.dto.AssetTimelineResponse;
import com.example.ems.asset.entity.MyAssetAssignment;
import com.example.ems.asset.entity.MyAssetMaintenance;
import com.example.ems.asset.entity.MyAssetDocument;
import com.example.ems.asset.entity.MyAssetActivity;
import com.example.ems.asset.entity.MyAssetIssue;
import com.example.ems.asset.repository.MyAssetAssignmentRepository;
import com.example.ems.asset.repository.MyAssetMaintenanceRepository;
import com.example.ems.asset.repository.MyAssetDocumentRepository;
import com.example.ems.asset.repository.MyAssetActivityRepository;
import com.example.ems.asset.repository.MyAssetIssueRepository;
import com.example.ems.asset.repository.MyAssetReturnRequestRepository;
import com.example.ems.asset.service.MyAssetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/assets")
@CrossOrigin("*")
@Tag(name = "Asset Management")
public class AssetAdminController {

    @Autowired
    private MyAssetRepository myAssetRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private MyAssetAssignmentRepository myAssetAssignmentRepository;

    @Autowired
    private MyAssetMaintenanceRepository myAssetMaintenanceRepository;

    @Autowired
    private MyAssetDocumentRepository myAssetDocumentRepository;

    @Autowired
    private MyAssetActivityRepository myAssetActivityRepository;

    @Autowired
    private MyAssetIssueRepository myAssetIssueRepository;

    @Autowired
    private MyAssetReturnRequestRepository myAssetReturnRequestRepository;

    @Autowired
    private MyAssetService assetService;

    @GetMapping("/dashboard")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getAssetDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        List<MyAsset> allAssets = myAssetRepository.findAll();

        long totalAssets = 0;
        long assignedAssets = 0;
        long availableAssets = 0;
        long maintenanceAssets = 0;
        long retiredAssets = 0;
        BigDecimal totalValue = BigDecimal.ZERO;

        for (MyAsset asset : allAssets) {
            String status = asset.getStatus();
            if ("DISPOSED".equalsIgnoreCase(status)) {
                continue;
            }
            totalAssets++;
            if ("ASSIGNED".equalsIgnoreCase(status) || "RETURN_REQUESTED".equalsIgnoreCase(status)) {
                assignedAssets++;
            } else if ("UNASSIGNED".equalsIgnoreCase(status) || "RETURNED".equalsIgnoreCase(status)
                    || "AVAILABLE".equalsIgnoreCase(status)) {
                availableAssets++;
            } else if ("UNDER_MAINTENANCE".equalsIgnoreCase(status) || "MAINTENANCE".equalsIgnoreCase(status)) {
                maintenanceAssets++;
            } else if ("RETIRED".equalsIgnoreCase(status)) {
                retiredAssets++;
            }

            BigDecimal val = asset.getCurrentValue();
            if (val == null)
                val = asset.getPurchasePrice();
            if (val != null) {
                totalValue = totalValue.add(val);
            }
        }

        Map<String, Object> dashboard = new LinkedHashMap<>();
        dashboard.put("totalAssets", totalAssets);
        dashboard.put("assignedAssets", assignedAssets);
        dashboard.put("availableAssets", availableAssets);
        dashboard.put("maintenanceAssets", maintenanceAssets);
        dashboard.put("retiredAssets", retiredAssets);
        dashboard.put("totalValue", totalValue);

        return ResponseEntity.ok(ApiResponse.success("Dashboard metrics retrieved successfully", dashboard));
    }

    @GetMapping
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getAllAssets(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        String statusParam = (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status))
                ? status.trim().toUpperCase()
                : null;
        String categoryParam = (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category))
                ? category.trim()
                : null;
        String deptParam = (department != null && !department.trim().isEmpty() && !"ALL".equalsIgnoreCase(department))
                ? department.trim()
                : null;
        String searchParam = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Pageable pageable = PageRequest.of(page, size);
        Page<MyAsset> assetPage = myAssetRepository.findFiltered(statusParam, categoryParam, deptParam, searchParam,
                pageable);

        Page<AssetDetailResponse> dtoPage = assetPage.map(AssetDetailResponse::new);

        return ResponseEntity.ok(ApiResponse.success("Assets list retrieved successfully", dtoPage));
    }

    @GetMapping("/{id}")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getAssetById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(id).orElse(null);
        if (asset == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }

        return ResponseEntity
                .ok(ApiResponse.success("Asset details retrieved successfully", new AssetDetailResponse(asset)));
    }

    @PostMapping
    @Transactional
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> createAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AssetDto request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = new MyAsset();
        asset.setAssetCode(request.getAssetCode());
        asset.setAssetName(request.getAssetName());
        asset.setCategory(request.getCategory());
        asset.setBrand(request.getBrand());
        asset.setModel(request.getModel());
        asset.setSerialNumber(request.getSerialNumber());
        asset.setPurchaseDate(request.getPurchaseDate() != null ? request.getPurchaseDate() : LocalDate.now());
        asset.setPurchasePrice(request.getPurchasePrice());
        asset.setCurrentValue(request.getCurrentValue());
        asset.setLocation(request.getLocation());
        asset.setCondition(request.getCondition() != null ? request.getCondition() : "GOOD");
        asset.setWarrantyStatus(request.getWarrantyStatus() != null ? request.getWarrantyStatus() : "ACTIVE");
        asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        asset.setVendor(request.getVendor());
        asset.setDepreciationPercentage(
                request.getDepreciationPercentage() != null ? request.getDepreciationPercentage() : BigDecimal.ZERO);
        asset.setStatus("AVAILABLE");

        if (request.getAssignedToEmployeeId() != null) {
            Employee emp = employeeRepository.findById(request.getAssignedToEmployeeId()).orElse(null);
            if (emp != null) {
                asset.setAssignedTo(emp);
                asset.setAssignedDate(LocalDate.now());
                asset.setStatus("ASSIGNED");
            }
        }

        asset.setCreatedAt(LocalDateTime.now());
        asset.setUpdatedAt(LocalDateTime.now());

        MyAsset saved = myAssetRepository.save(asset);

        if (saved.getAssignedTo() != null) {
            MyAssetAssignment assignment = new MyAssetAssignment(saved, saved.getAssignedTo(), LocalDate.now(), null,
                    "ACTIVE", "Initial Assignment");
            myAssetAssignmentRepository.save(assignment);
        }

        return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Asset created successfully", new AssetDetailResponse(saved)));
    }

    @PutMapping("/{id}")
    @Transactional
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> updateAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody AssetDto request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(id).orElse(null);
        if (asset == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }

        asset.setAssetCode(request.getAssetCode());
        asset.setAssetName(request.getAssetName());
        asset.setCategory(request.getCategory());
        asset.setBrand(request.getBrand());
        asset.setModel(request.getModel());
        asset.setSerialNumber(request.getSerialNumber());
        if (request.getPurchaseDate() != null)
            asset.setPurchaseDate(request.getPurchaseDate());
        asset.setPurchasePrice(request.getPurchasePrice());
        asset.setCurrentValue(request.getCurrentValue());
        asset.setLocation(request.getLocation());
        asset.setCondition(request.getCondition());
        asset.setWarrantyStatus(request.getWarrantyStatus());
        asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());
        asset.setVendor(request.getVendor());
        if (request.getDepreciationPercentage() != null) {
            asset.setDepreciationPercentage(request.getDepreciationPercentage());
        }

        Employee oldEmp = asset.getAssignedTo();
        Long newEmpId = request.getAssignedToEmployeeId();

        if (newEmpId != null) {
            if (oldEmp == null || !oldEmp.getId().equals(newEmpId)) {
                Employee emp = employeeRepository.findById(newEmpId).orElse(null);
                if (emp != null) {
                    List<MyAssetAssignment> activeAssignments = myAssetAssignmentRepository
                            .findByAssetIdOrderByAssignedDateDesc(id)
                            .stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                            .collect(Collectors.toList());
                    for (MyAssetAssignment active : activeAssignments) {
                        active.setStatus("RETURNED");
                        active.setReturnedDate(LocalDate.now());
                        myAssetAssignmentRepository.save(active);
                    }

                    asset.setAssignedTo(emp);
                    asset.setAssignedDate(LocalDate.now());
                    asset.setStatus("ASSIGNED");

                    MyAssetAssignment assignment = new MyAssetAssignment(asset, emp, LocalDate.now(), null, "ACTIVE",
                            "Updated Assignment");
                    myAssetAssignmentRepository.save(assignment);
                }
            }
        } else {
            if (oldEmp != null) {
                List<MyAssetAssignment> activeAssignments = myAssetAssignmentRepository
                        .findByAssetIdOrderByAssignedDateDesc(id)
                        .stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).collect(Collectors.toList());
                for (MyAssetAssignment active : activeAssignments) {
                    active.setStatus("RETURNED");
                    active.setReturnedDate(LocalDate.now());
                    myAssetAssignmentRepository.save(active);
                }
                asset.setAssignedTo(null);
                asset.setAssignedDate(null);
                asset.setStatus("AVAILABLE");
            }
        }

        asset.setUpdatedAt(LocalDateTime.now());
        MyAsset updated = myAssetRepository.save(asset);
        return ResponseEntity.ok(ApiResponse.success("Asset updated successfully", new AssetDetailResponse(updated)));
    }

    @DeleteMapping("/{id}")
    @Transactional
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> deleteAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        if (myAssetRepository.existsById(id)) {
            // delete documents, assignments, maintenance, activities, return requests, and
            // issues first
            List<MyAssetDocument> docs = myAssetDocumentRepository.findByAssetIdOrderByUploadedAtDesc(id);
            myAssetDocumentRepository.deleteAll(docs);
            List<MyAssetAssignment> assigns = myAssetAssignmentRepository.findByAssetIdOrderByAssignedDateDesc(id);
            myAssetAssignmentRepository.deleteAll(assigns);
            List<MyAssetMaintenance> maint = myAssetMaintenanceRepository.findByAssetIdOrderByStartDateDesc(id);
            myAssetMaintenanceRepository.deleteAll(maint);
            List<MyAssetActivity> activities = myAssetActivityRepository.findByAssetIdOrderByDateDesc(id);
            myAssetActivityRepository.deleteAll(activities);
            myAssetReturnRequestRepository.findByAssetId(id).ifPresent(myAssetReturnRequestRepository::delete);
            List<MyAssetIssue> issues = myAssetIssueRepository.findByAssetId(id);
            myAssetIssueRepository.deleteAll(issues);

            myAssetRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Asset deleted successfully", null));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }
    }

    @GetMapping("/{id}/timeline")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getAssetTimeline(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        try {
            AssetTimelineResponse response = assetService.getAssetTimeline(id, null);
            return ResponseEntity.ok(ApiResponse.success("Asset timeline retrieved successfully", response));
        } catch (IllegalArgumentException e) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error(e.getMessage(), "ASS_001"));
        }
    }

    @GetMapping("/{assetId}/assignments")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getAssignmentHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assetId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        List<MyAssetAssignment> assignments = myAssetAssignmentRepository.findByAssetIdOrderByAssignedDateDesc(assetId);
        List<Map<String, Object>> response = assignments.stream().map(a -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("employeeId", a.getEmployee().getId());
            map.put("employeeName", a.getEmployee().getFullName());
            map.put("assignedDate", a.getAssignedDate());
            map.put("returnedDate", a.getReturnedDate());
            map.put("status", a.getStatus());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Asset assignment history retrieved successfully", response));
    }

    @PostMapping("/{id}/assign")
    @Transactional
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> assignAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(id).orElse(null);
        if (asset == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }

        Object employeeIdObj = payload.get("employeeId");
        if (employeeIdObj == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Employee ID is required", "ASS_002"));
        }
        Long employeeId = Long.valueOf(employeeIdObj.toString());

        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + employeeId, "EMP_002"));
        }

        LocalDate expectedReturnDate = null;
        if (payload.containsKey("expectedReturnDate") && payload.get("expectedReturnDate") != null) {
            expectedReturnDate = LocalDate.parse(payload.get("expectedReturnDate").toString());
        }

        List<MyAssetAssignment> activeAssignments = myAssetAssignmentRepository.findByAssetIdOrderByAssignedDateDesc(id)
                .stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).collect(Collectors.toList());
        for (MyAssetAssignment active : activeAssignments) {
            active.setStatus("RETURNED");
            active.setReturnedDate(LocalDate.now());
            myAssetAssignmentRepository.save(active);
        }

        asset.setAssignedTo(employee);
        asset.setAssignedDate(LocalDate.now());
        asset.setStatus("ASSIGNED");
        asset.setUpdatedAt(LocalDateTime.now());
        myAssetRepository.save(asset);

        MyAssetAssignment assignment = new MyAssetAssignment(asset, employee, LocalDate.now(), expectedReturnDate,
                "ACTIVE", "Assigned via Admin API");
        myAssetAssignmentRepository.save(assignment);

        return ResponseEntity.ok(ApiResponse.success("Asset assigned successfully", new AssetDetailResponse(asset)));
    }

    @PostMapping("/{id}/transfer")
    @Transactional
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> transferAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(id).orElse(null);
        if (asset == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }

        Object toEmpIdObj = payload.get("toEmployeeId");
        if (toEmpIdObj == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Target employee ID is required", "ASS_002"));
        }
        Long toEmployeeId = Long.valueOf(toEmpIdObj.toString());

        Employee toEmployee = employeeRepository.findById(toEmployeeId).orElse(null);
        if (toEmployee == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Target employee not found with ID: " + toEmployeeId, "EMP_002"));
        }

        String remarks = (String) payload.getOrDefault("remarks", "Transferred via Admin API");

        List<MyAssetAssignment> activeAssignments = myAssetAssignmentRepository.findByAssetIdOrderByAssignedDateDesc(id)
                .stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).collect(Collectors.toList());
        for (MyAssetAssignment active : activeAssignments) {
            active.setStatus("TRANSFERRED");
            active.setReturnedDate(LocalDate.now());
            active.setRemarks("Transferred: " + remarks);
            myAssetAssignmentRepository.save(active);
        }

        asset.setAssignedTo(toEmployee);
        asset.setAssignedDate(LocalDate.now());
        asset.setStatus("ASSIGNED");
        asset.setUpdatedAt(LocalDateTime.now());
        myAssetRepository.save(asset);

        MyAssetAssignment assignment = new MyAssetAssignment(asset, toEmployee, LocalDate.now(), null, "ACTIVE",
                remarks);
        myAssetAssignmentRepository.save(assignment);

        return ResponseEntity.ok(ApiResponse.success("Asset transferred successfully", new AssetDetailResponse(asset)));
    }

    @PostMapping("/{id}/return")
    @Transactional
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> returnAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(id).orElse(null);
        if (asset == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }

        if ("DISPOSED".equalsIgnoreCase(asset.getStatus())) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.error("Cannot transition from DISPOSED status.", "ASS_CONFLICT"));
        }

        List<MyAssetAssignment> activeAssignments = myAssetAssignmentRepository.findByAssetIdOrderByAssignedDateDesc(id)
                .stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).collect(Collectors.toList());
        for (MyAssetAssignment active : activeAssignments) {
            active.setStatus("RETURNED");
            active.setReturnedDate(LocalDate.now());
            active.setRemarks("Returned via Return API");
            myAssetAssignmentRepository.save(active);
        }

        asset.setAssignedTo(null);
        asset.setAssignedDate(null);
        asset.setStatus("AVAILABLE");
        asset.setUpdatedAt(LocalDateTime.now());
        myAssetRepository.save(asset);

        return ResponseEntity.ok(ApiResponse.success("Asset returned successfully", new AssetDetailResponse(asset)));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> updateAssetStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody @Valid AssetStatusRequest request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(id).orElse(null);
        if (asset == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }

        if (request.status() == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("status is required", "VAL_001"));
        }
        String targetStatus = request.status().toUpperCase();
        if (!targetStatus.equals("AVAILABLE") && !targetStatus.equals("RETIRED") && !targetStatus.equals("DISPOSED")) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Invalid status. Supported statuses are AVAILABLE, RETIRED, DISPOSED.",
                            "VAL_002"));
        }

        if ("DISPOSED".equalsIgnoreCase(asset.getStatus())) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ErrorResponse.error("Cannot transition from DISPOSED status.", "ASS_CONFLICT"));
        }

        String remarks = request.remarks() != null ? request.remarks()
                : "Status updated to " + targetStatus + " via status API";

        List<MyAssetAssignment> activeAssignments = myAssetAssignmentRepository.findByAssetIdOrderByAssignedDateDesc(id)
                .stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).collect(Collectors.toList());
        for (MyAssetAssignment active : activeAssignments) {
            active.setStatus("RETURNED");
            active.setReturnedDate(LocalDate.now());
            active.setRemarks(targetStatus + ": " + remarks);
            myAssetAssignmentRepository.save(active);
        }

        asset.setAssignedTo(null);
        asset.setAssignedDate(null);
        asset.setStatus(targetStatus);
        if ("DISPOSED".equals(targetStatus)) {
            asset.setCondition("DISPOSED");
        }
        asset.setUpdatedAt(LocalDateTime.now());
        myAssetRepository.save(asset);

        return ResponseEntity
                .ok(ApiResponse.success("Asset status updated successfully", new AssetDetailResponse(asset)));
    }

    @GetMapping("/{assetId}/maintenance")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getMaintenanceRecords(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assetId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        List<MyAssetMaintenance> records = myAssetMaintenanceRepository.findByAssetIdOrderByStartDateDesc(assetId);
        return ResponseEntity.ok(ApiResponse.success("Maintenance records retrieved successfully", records));
    }

    @PostMapping("/{assetId}/maintenance")
    @Transactional
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> createMaintenanceRequest(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assetId,
            @RequestBody Map<String, Object> payload) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(assetId).orElse(null);
        if (asset == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + assetId, "ASS_001"));
        }

        String issue = (String) payload.get("issue");
        String vendor = (String) payload.get("vendor");
        Object costObj = payload.get("estimatedCost");
        if (issue == null || vendor == null || costObj == null) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("issue, vendor, and estimatedCost are required", "ASS_002"));
        }
        BigDecimal estimatedCost = new BigDecimal(costObj.toString());

        MyAssetMaintenance maintenance = new MyAssetMaintenance(asset, issue, vendor, estimatedCost);
        myAssetMaintenanceRepository.save(maintenance);

        asset.setStatus("UNDER_MAINTENANCE");
        asset.setUpdatedAt(LocalDateTime.now());
        myAssetRepository.save(asset);

        return ResponseEntity.ok(ApiResponse.success("Maintenance request created successfully", maintenance));
    }

    @PatchMapping("/maintenance/{maintenanceId}/complete")
    @Transactional
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> completeMaintenance(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long maintenanceId,
            @RequestBody(required = false) Map<String, Object> payload) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAssetMaintenance maintenance = myAssetMaintenanceRepository.findById(maintenanceId).orElse(null);
        if (maintenance == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Maintenance record not found with ID: " + maintenanceId, "ASS_003"));
        }

        BigDecimal actualCost = maintenance.getEstimatedCost();
        if (payload != null && payload.containsKey("actualCost") && payload.get("actualCost") != null) {
            actualCost = new BigDecimal(payload.get("actualCost").toString());
        }

        maintenance.setStatus("COMPLETED");
        maintenance.setActualCost(actualCost);
        maintenance.setCompletedDate(LocalDate.now());
        maintenance.setUpdatedAt(LocalDateTime.now());
        myAssetMaintenanceRepository.save(maintenance);

        MyAsset asset = maintenance.getAsset();
        asset.setStatus("AVAILABLE");
        asset.setUpdatedAt(LocalDateTime.now());
        myAssetRepository.save(asset);

        return ResponseEntity.ok(ApiResponse.success("Maintenance completed successfully", maintenance));
    }

    @PostMapping(value = "/{assetId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> uploadDocument(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assetId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(assetId).orElse(null);
        if (asset == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + assetId, "ASS_001"));
        }

        try {
            MyAssetDocument doc = new MyAssetDocument(
                    asset,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes(),
                    documentType);
            myAssetDocumentRepository.save(doc);

            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("id", doc.getId());
            metadata.put("fileName", doc.getFileName());
            metadata.put("fileType", doc.getFileType());
            metadata.put("documentType", doc.getDocumentType());
            metadata.put("uploadedAt", doc.getUploadedAt());

            return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", metadata));
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest()
                    .body(ErrorResponse.error("Failed to read file contents: " + e.getMessage(), "DOC_002"));
        }
    }

    @GetMapping("/{assetId}/documents")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getDocuments(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long assetId) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        List<MyAssetDocument> docs = myAssetDocumentRepository.findByAssetIdOrderByUploadedAtDesc(assetId);
        List<Map<String, Object>> response = docs.stream().map(d -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", d.getId());
            map.put("fileName", d.getFileName());
            map.put("fileType", d.getFileType());
            map.put("documentType", d.getDocumentType());
            map.put("uploadedAt", d.getUploadedAt());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Asset documents retrieved successfully", response));
    }

    @GetMapping("/reports/utilization")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getUtilizationReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        List<MyAsset> assets = myAssetRepository.findAll().stream()
                .filter(a -> !"DISPOSED".equalsIgnoreCase(a.getStatus())).collect(Collectors.toList());
        long total = assets.size();
        long allocated = assets.stream().filter(
                a -> "ASSIGNED".equalsIgnoreCase(a.getStatus()) || "RETURN_REQUESTED".equalsIgnoreCase(a.getStatus()))
                .count();
        double rate = total > 0 ? ((double) allocated / total) * 100 : 0.0;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalAssets", total);
        response.put("allocatedAssets", allocated);
        response.put("utilizationRate", rate);

        return ResponseEntity.ok(ApiResponse.success("Asset utilization report retrieved successfully", response));
    }

    @GetMapping("/reports/depreciation")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getDepreciationReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        List<MyAsset> assets = myAssetRepository.findAll().stream()
                .filter(a -> !"DISPOSED".equalsIgnoreCase(a.getStatus())).collect(Collectors.toList());
        BigDecimal totalPurchase = BigDecimal.ZERO;
        BigDecimal totalCurrent = BigDecimal.ZERO;

        for (MyAsset asset : assets) {
            if (asset.getPurchasePrice() != null)
                totalPurchase = totalPurchase.add(asset.getPurchasePrice());
            if (asset.getCurrentValue() != null)
                totalCurrent = totalCurrent.add(asset.getCurrentValue());
        }
        BigDecimal totalDepreciation = totalPurchase.subtract(totalCurrent);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalPurchaseValue", totalPurchase);
        response.put("totalCurrentValue", totalCurrent);
        response.put("totalDepreciation", totalDepreciation);

        return ResponseEntity.ok(ApiResponse.success("Asset depreciation report retrieved successfully", response));
    }

    @GetMapping("/reports/maintenance")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getMaintenanceReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        List<MyAssetMaintenance> records = myAssetMaintenanceRepository.findAll();
        long total = records.size();
        long active = records.stream().filter(r -> "UNDER_MAINTENANCE".equalsIgnoreCase(r.getStatus())).count();
        long completed = total - active;
        BigDecimal totalCost = BigDecimal.ZERO;
        for (MyAssetMaintenance r : records) {
            BigDecimal cost = r.getActualCost() != null ? r.getActualCost() : r.getEstimatedCost();
            if (cost != null)
                totalCost = totalCost.add(cost);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalRequests", total);
        response.put("activeRequests", active);
        response.put("completedRequests", completed);
        response.put("totalCost", totalCost);

        return ResponseEntity.ok(ApiResponse.success("Asset maintenance report retrieved successfully", response));
    }

    @GetMapping("/reports/inventory")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public ResponseEntity<ApiResponse<Object>> getInventoryReport(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null)
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        List<MyAsset> assets = myAssetRepository.findAll().stream()
                .filter(a -> !"DISPOSED".equalsIgnoreCase(a.getStatus())).collect(Collectors.toList());
        long total = assets.size();

        Map<String, Long> byCategory = assets.stream()
                .collect(Collectors.groupingBy(MyAsset::getCategory, Collectors.counting()));
        Map<String, Long> byStatus = assets.stream().collect(Collectors.groupingBy(a -> {
            String internalStatus = a.getStatus();
            if ("UNASSIGNED".equalsIgnoreCase(internalStatus) || "RETURNED".equalsIgnoreCase(internalStatus)
                    || "AVAILABLE".equalsIgnoreCase(internalStatus)) {
                return "AVAILABLE";
            }
            return internalStatus;
        }, Collectors.counting()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalAssets", total);
        response.put("byCategory", byCategory);
        response.put("byStatus", byStatus);

        return ResponseEntity.ok(ApiResponse.success("Asset inventory report retrieved successfully", response));
    }

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
}
