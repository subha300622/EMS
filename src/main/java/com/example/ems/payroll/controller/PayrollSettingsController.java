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
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/payroll-settings")
@CrossOrigin("*")
@Tag(name = "Payroll Settings")
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

    @PostConstruct
    public void seedDefaults() {
        if (payrollSettingRepository.count() == 0) {
            payrollSettingRepository.save(new PayrollSetting("paycycle_start_day", "1", "Start day of pay cycle"));
            payrollSettingRepository.save(new PayrollSetting("tax_enabled", "true", "Whether income tax calculation is enabled"));
        }
        if (salaryComponentRepository.count() == 0) {
            salaryComponentRepository.save(new SalaryComponent("Basic Salary", "EARNING", BigDecimal.valueOf(50.0), null, true));
            salaryComponentRepository.save(new SalaryComponent("HRA", "EARNING", BigDecimal.valueOf(20.0), null, true));
            salaryComponentRepository.save(new SalaryComponent("Provident Fund", "DEDUCTION", BigDecimal.valueOf(12.0), null, false));
        }
        if (taxSlabRepository.count() == 0) {
            taxSlabRepository.save(new TaxSlab("NEW", BigDecimal.valueOf(0.0), BigDecimal.valueOf(300000.0), BigDecimal.ZERO));
            taxSlabRepository.save(new TaxSlab("NEW", BigDecimal.valueOf(300000.0), BigDecimal.valueOf(600000.0), BigDecimal.valueOf(5.0)));
            taxSlabRepository.save(new TaxSlab("NEW", BigDecimal.valueOf(600000.0), null, BigDecimal.valueOf(10.0)));
        }
    }

    // --- PARAMETERS ---
    @GetMapping("/parameters")
    public ResponseEntity<?> getParameters(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Payroll parameters retrieved", payrollSettingRepository.findAll()));
    }

    @PostMapping("/parameters")
    @Transactional
    public ResponseEntity<?> saveParameter(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody PayrollSetting request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        PayrollSetting existing = payrollSettingRepository.findBySettingKey(request.getSettingKey()).orElse(null);
        if (existing != null) {
            existing.setSettingValue(request.getSettingValue());
            existing.setDescription(request.getDescription());
            return ResponseEntity.ok(ApiResponse.success("Payroll parameter updated", payrollSettingRepository.save(existing)));
        } else {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Payroll parameter created", payrollSettingRepository.save(request)));
        }
    }

    // --- COMPONENTS ---
    @GetMapping("/components")
    public ResponseEntity<?> getComponents(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Salary components retrieved", salaryComponentRepository.findAll()));
    }

    @PostMapping("/components")
    @Transactional
    public ResponseEntity<?> createComponent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody SalaryComponent component) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Salary component created", salaryComponentRepository.save(component)));
    }

    @PutMapping("/components/{id}")
    @Transactional
    public ResponseEntity<?> updateComponent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody SalaryComponent request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        SalaryComponent component = salaryComponentRepository.findById(id).orElse(null);
        if (component == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Salary component not found with ID: " + id, "COM_001"));
        }

        component.setName(request.getName());
        component.setType(request.getType());
        component.setPercentageOfBasic(request.getPercentageOfBasic());
        component.setFixedAmount(request.getFixedAmount());
        component.setTaxable(request.getTaxable());

        return ResponseEntity.ok(ApiResponse.success("Salary component updated", salaryComponentRepository.save(component)));
    }

    @DeleteMapping("/components/{id}")
    @Transactional
    public ResponseEntity<?> deleteComponent(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        if (salaryComponentRepository.existsById(id)) {
            salaryComponentRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Salary component deleted", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Salary component not found with ID: " + id, "COM_001"));
        }
    }

    // --- TAX SLABS ---
    @GetMapping("/tax-slabs")
    public ResponseEntity<?> getTaxSlabs(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.ok(ApiResponse.success("Tax slabs retrieved", taxSlabRepository.findAll()));
    }

    @PostMapping("/tax-slabs")
    @Transactional
    public ResponseEntity<?> createTaxSlab(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody TaxSlab slab) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tax slab created", taxSlabRepository.save(slab)));
    }

    @PutMapping("/tax-slabs/{id}")
    @Transactional
    public ResponseEntity<?> updateTaxSlab(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody TaxSlab request) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        TaxSlab slab = taxSlabRepository.findById(id).orElse(null);
        if (slab == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.error("Tax slab not found with ID: " + id, "SLB_001"));
        }

        slab.setRegime(request.getRegime());
        slab.setMinIncome(request.getMinIncome());
        slab.setMaxIncome(request.getMaxIncome());
        slab.setTaxRate(request.getTaxRate());

        return ResponseEntity.ok(ApiResponse.success("Tax slab updated", taxSlabRepository.save(slab)));
    }

    @DeleteMapping("/tax-slabs/{id}")
    @Transactional
    public ResponseEntity<?> deleteTaxSlab(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id) {
        User currentUser = resolveUser(authHeader);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.error("Unauthorized", "AUTH_014"));
        }

        if (!roleService.hasPermission(currentUser.getWorkEmail(), "payroll-settings.manage")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.error("Access Denied: Requires 'payroll-settings.manage' permission.", "AUTH_002"));
        }

        if (taxSlabRepository.existsById(id)) {
            taxSlabRepository.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("Tax slab deleted", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
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
