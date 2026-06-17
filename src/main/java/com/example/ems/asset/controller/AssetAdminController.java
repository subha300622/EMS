package com.example.ems.asset.controller;

import com.example.ems.asset.dto.AssetDto;
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

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/assets")
@CrossOrigin("*")
@Tag(name = "Administration")
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

    @GetMapping
    public ResponseEntity<?> getAllAssets(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Assets list retrieved successfully", myAssetRepository.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAssetById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(id).orElse(null);
        if (asset == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }

        return ResponseEntity.ok(ApiResponse.success("Asset details retrieved successfully", asset));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> createAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody AssetDto request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
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
        asset.setStatus("UNASSIGNED");

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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Asset created successfully", saved));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody AssetDto request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(id).orElse(null);
        if (asset == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }

        asset.setAssetCode(request.getAssetCode());
        asset.setAssetName(request.getAssetName());
        asset.setCategory(request.getCategory());
        asset.setBrand(request.getBrand());
        asset.setModel(request.getModel());
        asset.setSerialNumber(request.getSerialNumber());
        if (request.getPurchaseDate() != null) asset.setPurchaseDate(request.getPurchaseDate());
        asset.setPurchasePrice(request.getPurchasePrice());
        asset.setCurrentValue(request.getCurrentValue());
        asset.setLocation(request.getLocation());
        asset.setCondition(request.getCondition());
        asset.setWarrantyStatus(request.getWarrantyStatus());
        asset.setWarrantyExpiryDate(request.getWarrantyExpiryDate());

        if (request.getAssignedToEmployeeId() != null) {
            Employee emp = employeeRepository.findById(request.getAssignedToEmployeeId()).orElse(null);
            if (emp != null) {
                asset.setAssignedTo(emp);
                asset.setAssignedDate(LocalDate.now());
                asset.setStatus("ASSIGNED");
            }
        } else {
            asset.setAssignedTo(null);
            asset.setAssignedDate(null);
            asset.setStatus("UNASSIGNED");
        }

        asset.setUpdatedAt(LocalDateTime.now());
        MyAsset updated = myAssetRepository.save(asset);
        return ResponseEntity.ok(ApiResponse.success("Asset updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        if (myAssetRepository.existsById(id)) {
            myAssetRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Asset deleted successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }
    }

    @PostMapping("/{id}/assign")
    @Transactional
    public ResponseEntity<?> assignAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody Map<String, Long> payload) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(id).orElse(null);
        if (asset == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }

        Long employeeId = payload.get("employeeId");
        if (employeeId == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.error("Employee ID is required", "ASS_002"));
        }

        Employee employee = employeeRepository.findById(employeeId).orElse(null);
        if (employee == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Employee not found with ID: " + employeeId, "EMP_002"));
        }

        asset.setAssignedTo(employee);
        asset.setAssignedDate(LocalDate.now());
        asset.setStatus("ASSIGNED");
        asset.setUpdatedAt(LocalDateTime.now());

        MyAsset updated = myAssetRepository.save(asset);
        return ResponseEntity.ok(ApiResponse.success("Asset assigned successfully", updated));
    }

    @PostMapping("/{id}/return")
    @Transactional
    public ResponseEntity<?> returnAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(id).orElse(null);
        if (asset == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }

        asset.setAssignedTo(null);
        asset.setAssignedDate(null);
        asset.setStatus("RETURNED");
        asset.setUpdatedAt(LocalDateTime.now());

        MyAsset updated = myAssetRepository.save(asset);
        return ResponseEntity.ok(ApiResponse.success("Asset returned successfully", updated));
    }

    @PostMapping("/{id}/dispose")
    @Transactional
    public ResponseEntity<?> disposeAsset(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "asset.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'asset.manage' permission.", "AUTH_002"));
        }

        MyAsset asset = myAssetRepository.findById(id).orElse(null);
        if (asset == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Asset not found with ID: " + id, "ASS_001"));
        }

        asset.setAssignedTo(null);
        asset.setAssignedDate(null);
        asset.setStatus("DISPOSED");
        asset.setCondition("DISPOSED");
        asset.setUpdatedAt(LocalDateTime.now());

        MyAsset updated = myAssetRepository.save(asset);
        return ResponseEntity.ok(ApiResponse.success("Asset disposed successfully", updated));
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
