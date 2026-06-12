package com.example.ems.finance.service;

import com.example.ems.finance.entity.FinanceOnboarding;
import com.example.ems.finance.repository.FinanceOnboardingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FinanceOnboardingService {

    @Autowired
    private FinanceOnboardingRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // ── 1. CREATE ONBOARDING ────────────────────────────────────────────────
    public FinanceOnboarding createOnboarding() {
        // Archive any existing active (DRAFT) onboarding to keep only one active
        repository.findFirstByStatusOrderByCreatedAtDesc("DRAFT")
                .ifPresent(existing -> {
                    existing.setStatus("ARCHIVED");
                    existing.setUpdatedAt(LocalDateTime.now());
                    repository.save(existing);
                });

        FinanceOnboarding onboarding = new FinanceOnboarding();
        onboarding.setStatus("DRAFT");
        onboarding.setStepProgress(0);
        onboarding.setValidated(false);
        onboarding.setCompleted(false);
        onboarding.setCreatedAt(LocalDateTime.now());
        onboarding.setUpdatedAt(LocalDateTime.now());

        return repository.save(onboarding);
    }

    // ── 2. GET CURRENT ONBOARDING ───────────────────────────────────────────
    public Optional<FinanceOnboarding> getCurrentOnboarding() {
        return repository.findFirstByStatusOrderByCreatedAtDesc("DRAFT")
                .or(() -> repository.findFirstByStatusOrderByCreatedAtDesc("VALIDATED"));
    }

    // ── 3. GET BY ID ────────────────────────────────────────────────────────
    public Optional<FinanceOnboarding> getOnboardingById(Long id) {
        return repository.findById(id);
    }

    // ── 4. CALCULATE PROGRESS ───────────────────────────────────────────────
    public int calculateProgress(FinanceOnboarding onboarding) {
        int progress = 0;
        if (onboarding.getCompanyName() != null && !onboarding.getCompanyName().isEmpty()) progress += 17;
        if (onboarding.getBankName() != null && !onboarding.getBankName().isEmpty()) progress += 17;
        if (onboarding.getTaxRegime() != null && !onboarding.getTaxRegime().isEmpty()) progress += 17;
        if (onboarding.getPaymentMethod() != null && !onboarding.getPaymentMethod().isEmpty()) progress += 16;
        if (onboarding.getPayrollCycleStartDay() != null) progress += 16;
        if (onboarding.getBudgetTotal() != null) progress += 17;
        return progress;
    }

    // ── 5. STEP PATCH UPDATES ───────────────────────────────────────────────
    public FinanceOnboarding updateCompany(Long id, Map<String, String> data) {
        FinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding session not found with ID: " + id));

        ob.setCompanyName(data.get("companyName"));
        ob.setCompanyAddress(data.get("companyAddress"));
        ob.setCompanyPhone(data.get("companyPhone"));
        ob.setCompanyRegistrationNumber(data.get("companyRegistrationNumber"));
        ob.setCompanyTaxId(data.get("companyTaxId"));
        ob.setCompanyWebsite(data.get("companyWebsite"));

        ob.setStepProgress(calculateProgress(ob));
        ob.setUpdatedAt(LocalDateTime.now());
        return repository.save(ob);
    }

    public FinanceOnboarding updateBankAccount(Long id, Map<String, String> data) {
        FinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding session not found with ID: " + id));

        ob.setBankName(data.get("bankName"));
        ob.setBankAccountNumber(data.get("bankAccountNumber"));
        ob.setBankRoutingNumber(data.get("bankRoutingNumber"));
        ob.setBankSwiftCode(data.get("bankSwiftCode"));

        ob.setStepProgress(calculateProgress(ob));
        ob.setUpdatedAt(LocalDateTime.now());
        return repository.save(ob);
    }

    public FinanceOnboarding updateTax(Long id, Map<String, Object> data) {
        FinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding session not found with ID: " + id));

        ob.setTaxRegime((String) data.get("taxRegime"));
        if (data.containsKey("taxRate") && data.get("taxRate") != null) {
            ob.setTaxRate(new BigDecimal(data.get("taxRate").toString()));
        }
        ob.setTaxFinancialYear((String) data.get("taxFinancialYear"));

        ob.setStepProgress(calculateProgress(ob));
        ob.setUpdatedAt(LocalDateTime.now());
        return repository.save(ob);
    }

    public FinanceOnboarding updatePaymentMethod(Long id, Map<String, String> data) {
        FinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding session not found with ID: " + id));

        ob.setPaymentMethod(data.get("paymentMethod"));
        ob.setPaymentCurrency(data.get("paymentCurrency"));

        ob.setStepProgress(calculateProgress(ob));
        ob.setUpdatedAt(LocalDateTime.now());
        return repository.save(ob);
    }

    public FinanceOnboarding updatePayroll(Long id, Map<String, Integer> data) {
        FinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding session not found with ID: " + id));

        ob.setPayrollCycleStartDay(data.get("payrollCycleStartDay"));
        ob.setPayrollCycleEndDay(data.get("payrollCycleEndDay"));

        ob.setStepProgress(calculateProgress(ob));
        ob.setUpdatedAt(LocalDateTime.now());
        return repository.save(ob);
    }

    public FinanceOnboarding updateBudget(Long id, Map<String, Object> data) {
        FinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding session not found with ID: " + id));

        if (data.containsKey("budgetTotal") && data.get("budgetTotal") != null) {
            ob.setBudgetTotal(new BigDecimal(data.get("budgetTotal").toString()));
        }
        ob.setBudgetCurrency((String) data.get("budgetCurrency"));
        ob.setBudgetDepartmentBreakdown((String) data.get("budgetDepartmentBreakdown"));

        ob.setStepProgress(calculateProgress(ob));
        ob.setUpdatedAt(LocalDateTime.now());
        return repository.save(ob);
    }

    // ── 6. VALIDATE ONBOARDING ──────────────────────────────────────────────
    public List<String> validateOnboarding(Long id) {
        FinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding session not found with ID: " + id));

        List<String> errors = new ArrayList<>();

        if (ob.getCompanyName() == null || ob.getCompanyName().isEmpty()) {
            errors.add("Company settings: Company name is required");
        }
        if (ob.getCompanyTaxId() == null || ob.getCompanyTaxId().isEmpty()) {
            errors.add("Company settings: Company tax ID is required");
        }
        if (ob.getBankName() == null || ob.getBankName().isEmpty()) {
            errors.add("Bank account settings: Bank name is required");
        }
        if (ob.getBankAccountNumber() == null || ob.getBankAccountNumber().isEmpty()) {
            errors.add("Bank account settings: Account number is required");
        }
        if (ob.getTaxRegime() == null || ob.getTaxRegime().isEmpty()) {
            errors.add("Tax settings: Tax regime is required");
        }
        if (ob.getTaxRate() == null) {
            errors.add("Tax settings: Tax rate is required");
        }
        if (ob.getPaymentMethod() == null || ob.getPaymentMethod().isEmpty()) {
            errors.add("Payment method settings: Payment method is required");
        }
        if (ob.getPayrollCycleStartDay() == null || ob.getPayrollCycleStartDay() < 1 || ob.getPayrollCycleStartDay() > 31) {
            errors.add("Payroll settings: Pay cycle start day must be between 1 and 31");
        }
        if (ob.getPayrollCycleEndDay() == null || ob.getPayrollCycleEndDay() < 1 || ob.getPayrollCycleEndDay() > 31) {
            errors.add("Payroll settings: Pay cycle end day must be between 1 and 31");
        }
        if (ob.getBudgetTotal() == null || ob.getBudgetTotal().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Budget settings: Total budget must be greater than zero");
        }

        if (errors.isEmpty()) {
            ob.setValidated(true);
            ob.setStatus("VALIDATED");
            ob.setStepProgress(100);
            ob.setUpdatedAt(LocalDateTime.now());
            repository.save(ob);
        } else {
            ob.setValidated(false);
            ob.setStatus("DRAFT");
            ob.setUpdatedAt(LocalDateTime.now());
            repository.save(ob);
        }

        return errors;
    }

    // ── 7. COMPLETE ONBOARDING ──────────────────────────────────────────────
    public FinanceOnboarding completeOnboarding(Long id) {
        FinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding session not found with ID: " + id));

        List<String> errors = validateOnboarding(id);
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Cannot complete onboarding. Validation failed: " + String.join(", ", errors));
        }

        // Apply settings system-wide to database settings tables
        try {
            // 1. Sync company_settings
            List<Long> companyIds = jdbcTemplate.queryForList("SELECT id FROM company_settings", Long.class);
            if (companyIds.isEmpty()) {
                jdbcTemplate.update("INSERT INTO company_settings (company_name, address, phone, registration_number, tax_id, website) VALUES (?, ?, ?, ?, ?, ?)",
                        ob.getCompanyName(), ob.getCompanyAddress(), ob.getCompanyPhone(),
                        ob.getCompanyRegistrationNumber(), ob.getCompanyTaxId(), ob.getCompanyWebsite());
            } else {
                jdbcTemplate.update("UPDATE company_settings SET company_name = ?, address = ?, phone = ?, registration_number = ?, tax_id = ?, website = ? WHERE id = ?",
                        ob.getCompanyName(), ob.getCompanyAddress(), ob.getCompanyPhone(),
                        ob.getCompanyRegistrationNumber(), ob.getCompanyTaxId(), ob.getCompanyWebsite(), companyIds.get(0));
            }

            // 2. Sync payroll_settings
            List<Long> payrollIds = jdbcTemplate.queryForList("SELECT id FROM payroll_settings", Long.class);
            if (payrollIds.isEmpty()) {
                jdbcTemplate.update("INSERT INTO payroll_settings (currency, pay_cycle_start_day, pay_cycle_end_day, payment_method, updated_at) VALUES (?, ?, ?, ?, ?)",
                        ob.getPaymentCurrency(), ob.getPayrollCycleStartDay(), ob.getPayrollCycleEndDay(), ob.getPaymentMethod(), LocalDateTime.now());
            } else {
                jdbcTemplate.update("UPDATE payroll_settings SET currency = ?, pay_cycle_start_day = ?, pay_cycle_end_day = ?, payment_method = ?, updated_at = ? WHERE id = ?",
                        ob.getPaymentCurrency(), ob.getPayrollCycleStartDay(), ob.getPayrollCycleEndDay(), ob.getPaymentMethod(), LocalDateTime.now(), payrollIds.get(0));
            }

            // 3. Sync tax_settings
            List<Long> taxIds = jdbcTemplate.queryForList("SELECT id FROM tax_settings", Long.class);
            if (taxIds.isEmpty()) {
                jdbcTemplate.update("INSERT INTO tax_settings (financial_year, min_income, max_income, tax_rate, tax_regime) VALUES (?, ?, ?, ?, ?)",
                        ob.getTaxFinancialYear(), BigDecimal.ZERO, new BigDecimal("999999999.99"), ob.getTaxRate(), ob.getTaxRegime());
            } else {
                jdbcTemplate.update("UPDATE tax_settings SET financial_year = ?, tax_rate = ?, tax_regime = ? WHERE id = ?",
                        ob.getTaxFinancialYear(), ob.getTaxRate(), ob.getTaxRegime(), taxIds.get(0));
            }
        } catch (Exception e) {
            // Log setting sync failure but do not rollback wizard state
            System.err.println("Warning: Settings tables synchronization failed: " + e.getMessage());
        }

        ob.setCompleted(true);
        ob.setStatus("COMPLETED");
        ob.setUpdatedAt(LocalDateTime.now());
        return repository.save(ob);
    }

    // ── 8. ADMIN LIST & ARCHIVE ─────────────────────────────────────────────
    public List<FinanceOnboarding> listAll() {
        return repository.findByStatusNot("ARCHIVED");
    }

    public boolean archiveOnboarding(Long id) {
        return repository.findById(id)
                .map(ob -> {
                    ob.setStatus("ARCHIVED");
                    ob.setUpdatedAt(LocalDateTime.now());
                    repository.save(ob);
                    return true;
                }).orElse(false);
    }
}
