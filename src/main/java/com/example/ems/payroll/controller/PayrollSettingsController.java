package com.example.ems.payroll.controller;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.RoleService;
import com.example.ems.common.dto.ApiResponse;
import com.example.ems.common.dto.ErrorResponse;
import com.example.ems.payroll.entity.PayrollSetting;
import com.example.ems.payroll.entity.SalaryComponent;
import com.example.ems.payroll.entity.TaxSlab;
import com.example.ems.payroll.repository.PayrollSettingRepository;
import com.example.ems.payroll.repository.SalaryComponentRepository;
import com.example.ems.payroll.repository.TaxSlabRepository;
import com.example.ems.security.service.JwtService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/payroll-settings")
@CrossOrigin("*")
@Tag(name = "Finance Setup")
public class PayrollSettingsController {

    @Autowired
    private PayrollSettingRepository payrollSettingRepository;

    @Autowired
    private SalaryComponentRepository salaryComponentRepository;

    @Autowired
    private TaxSlabRepository taxSlabRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JwtService jwtService;

    // --- PARAMETERS ---
    @Operation(summary = "Get Payroll Settings Parameters", description = "Retrieves general system configuration parameters for payroll processing (e.g., pay cycle start day, tax calculation enabled).")
    @GetMapping("/parameters")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getParameters(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Payroll parameters retrieved", payrollSettingRepository.findAll()));
    }

    @Operation(summary = "Save Payroll Settings Parameter", description = "Creates or updates a general system configuration parameter for payroll processing.")
    @PostMapping("/parameters")
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> saveParameter(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody PayrollSetting request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        PayrollSetting existing = payrollSettingRepository.findBySettingKey(request.getSettingKey()).orElse(null);
        if (existing != null) {
            existing.setSettingValue(request.getSettingValue());
            existing.setDescription(request.getDescription());
            return ResponseEntity.ok(ApiResponse.success("Payroll parameter updated", payrollSettingRepository.save(existing)));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Payroll parameter created", payrollSettingRepository.save(request)));
        }
    }

    // --- COMPONENTS ---
    @Operation(summary = "Get Salary Components", description = "Retrieves all configured salary earnings and deductions components (e.g., Basic Salary, HRA, Provident Fund).")
    @GetMapping("/components")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getComponents(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Salary components retrieved", salaryComponentRepository.findAll()));
    }

    @Operation(summary = "Create Salary Component", description = "Creates a new salary component structure for basic calculations.")
    @PostMapping("/components")
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createComponent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SalaryComponent component){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Salary component created", salaryComponentRepository.save(component)));
    }

    @Operation(summary = "Update Salary Component", description = "Updates configurations on an existing salary component entry.")
    @PutMapping("/components/{id}")
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateComponent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody SalaryComponent request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        SalaryComponent component = salaryComponentRepository.findById(id).orElse(null);
        if (component == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Salary component not found with ID: " + id, "COM_001"));
        }

        component.setName(request.getName());
        component.setType(request.getType());
        component.setPercentageOfBasic(request.getPercentageOfBasic());
        component.setFixedAmount(request.getFixedAmount());
        component.setTaxable(request.getTaxable());

        return ResponseEntity.ok(ApiResponse.success("Salary component updated", salaryComponentRepository.save(component)));
    }

    @Operation(summary = "Delete Salary Component", description = "Removes a salary component entry from the system setup.")
    @DeleteMapping("/components/{id}")
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteComponent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        if (salaryComponentRepository.existsById(id)) {
            salaryComponentRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Salary component deleted", null));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Salary component not found with ID: " + id, "COM_001"));
        }
    }

    // --- TAX SLABS ---
    @Operation(summary = "Get Tax Slabs", description = "Retrieves all configured income tax slabs under active tax regimes.")
    @GetMapping("/tax-slabs")
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> getTaxSlabs(
            @RequestHeader(value = "Authorization", required = false) String authHeader){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Tax slabs retrieved", taxSlabRepository.findAll()));
    }

    @Operation(summary = "Create Tax Slab", description = "Creates a new income tax slab specifying regime, income boundaries, and rate percentage.")
    @PostMapping("/tax-slabs")
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> createTaxSlab(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody TaxSlab slab){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        return (ResponseEntity) ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tax slab created", taxSlabRepository.save(slab)));
    }

    @Operation(summary = "Update Tax Slab", description = "Updates an existing income tax slab configuration.")
    @PutMapping("/tax-slabs/{id}")
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> updateTaxSlab(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody TaxSlab request){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        TaxSlab slab = taxSlabRepository.findById(id).orElse(null);
        if (slab == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Tax slab not found with ID: " + id, "SLB_001"));
        }

        slab.setRegime(request.getRegime());
        slab.setMinIncome(request.getMinIncome());
        slab.setMaxIncome(request.getMaxIncome());
        slab.setTaxRate(request.getTaxRate());

        return ResponseEntity.ok(ApiResponse.success("Tax slab updated", taxSlabRepository.save(slab)));
    }

    @Operation(summary = "Delete Tax Slab", description = "Deletes an income tax slab entry from configuration.")
    @DeleteMapping("/tax-slabs/{id}")
    @Transactional
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ResponseEntity<ApiResponse<Object>> deleteTaxSlab(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id){
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        if (taxSlabRepository.existsById(id)) {
            taxSlabRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Tax slab deleted", null));
        } else {
            return (ResponseEntity) ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Tax slab not found with ID: " + id, "SLB_001"));
        }
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
