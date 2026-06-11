package com.example.ems.payroll.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.payroll.dto.PayrollUpdateRequest;
import com.example.ems.payroll.entity.Payroll;
import com.example.ems.payroll.repository.PayrollRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private com.example.ems.payroll.repository.PayslipRepository payslipRepository;

    // ── BATCH GENERATION ─────────────────────────────────────────────────────

    public List<Payroll> generatePayroll(Integer month, Integer year) {
        List<Employee> activeEmployees = employeeRepository.findAll().stream()
                .filter(e -> "ACTIVE".equalsIgnoreCase(e.getStatus()))
                .collect(Collectors.toList());

        List<Payroll> generatedList = new ArrayList<>();

        for (Employee employee : activeEmployees) {
            Optional<Payroll> existing = payrollRepository.findByEmployeeIdAndMonthAndYear(employee.getId(), month, year);
            if (existing.isEmpty()) {
                BigDecimal basic = BigDecimal.ZERO;
                if (employee.getAnnualSalary() != null) {
                    // basic monthly = annual / 12
                    basic = employee.getAnnualSalary().divide(BigDecimal.valueOf(12), 2, java.math.RoundingMode.HALF_UP);
                }

                Payroll p = new Payroll();
                p.setEmployee(employee);
                p.setMonth(month);
                p.setYear(year);
                p.setBasicSalary(basic);
                p.setAllowances(BigDecimal.ZERO);
                p.setDeductions(BigDecimal.ZERO);
                p.setNetPay(basic);
                p.setStatus("GENERATED");
                p.setGeneratedAt(LocalDateTime.now());

                generatedList.add(payrollRepository.save(p));
            }
        }
        return generatedList;
    }

    // ── GETTERS ──────────────────────────────────────────────────────────────

    public List<Payroll> getAllPayroll() {
        return payrollRepository.findAll();
    }

    public Optional<Payroll> getPayrollById(Long id) {
        return payrollRepository.findById(id);
    }

    public List<Payroll> getPayrollByEmployeeId(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId);
    }

    // ── UPDATES & WORKFLOW ───────────────────────────────────────────────────

    public Payroll updatePayroll(Long id, PayrollUpdateRequest request) {
        Payroll p = payrollRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payroll record not found"));

        if ("PAID".equalsIgnoreCase(p.getStatus())) {
            throw new IllegalArgumentException("Cannot update payroll record that has already been paid");
        }

        p.setBasicSalary(request.getBasicSalary());
        p.setAllowances(request.getAllowances());
        p.setDeductions(request.getDeductions());
        
        // Recalculate netPay
        BigDecimal net = request.getBasicSalary().add(request.getAllowances()).subtract(request.getDeductions());
        p.setNetPay(net.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : net);
        
        return payrollRepository.save(p);
    }

    public Payroll reviewPayroll(Long id) {
        return transitionStatus(id, "GENERATED", "REVIEWED");
    }

    public Payroll approvePayroll(Long id) {
        return transitionStatus(id, "REVIEWED", "APPROVED");
    }

    public Payroll processPayroll(Long id) {
        return transitionStatus(id, "APPROVED", "PROCESSED");
    }

    public Payroll payPayroll(Long id) {
        Payroll p = payrollRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payroll record not found"));

        if (!"PROCESSED".equalsIgnoreCase(p.getStatus())) {
            throw new IllegalArgumentException("Payroll can only be paid after it is PROCESSED. Current status: " + p.getStatus());
        }

        p.setStatus("PAID");
        p.setProcessedAt(LocalDateTime.now());
        return payrollRepository.save(p);
    }

    private Payroll transitionStatus(Long id, String expectedStatus, String newStatus) {
        Payroll p = payrollRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payroll record not found"));

        if (!expectedStatus.equalsIgnoreCase(p.getStatus())) {
            throw new IllegalArgumentException("Cannot transition status to " + newStatus + ". Current status is " + p.getStatus() + ", expected: " + expectedStatus);
        }

        p.setStatus(newStatus);
        return payrollRepository.save(p);
    }

    // ── DASHBOARD STATS ──────────────────────────────────────────────────────

    public Map<String, Object> getPayrollStats() {
        List<Payroll> all = payrollRepository.findAll();
        
        BigDecimal totalBasic = BigDecimal.ZERO;
        BigDecimal totalAllowances = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalNetPay = BigDecimal.ZERO;

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        statusCounts.put("GENERATED", 0L);
        statusCounts.put("REVIEWED", 0L);
        statusCounts.put("APPROVED", 0L);
        statusCounts.put("PROCESSED", 0L);
        statusCounts.put("PAID", 0L);

        for (Payroll p : all) {
            totalBasic = totalBasic.add(p.getBasicSalary());
            totalAllowances = totalAllowances.add(p.getAllowances());
            totalDeductions = totalDeductions.add(p.getDeductions());
            totalNetPay = totalNetPay.add(p.getNetPay());

            String status = p.getStatus().toUpperCase();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalRecords", all.size());
        stats.put("totalBasicSalary", totalBasic);
        stats.put("totalAllowances", totalAllowances);
        stats.put("totalDeductions", totalDeductions);
        stats.put("totalNetPay", totalNetPay);
        stats.put("statusDistribution", statusCounts);

        return stats;
    }

    @org.springframework.transaction.annotation.Transactional
    public boolean deletePayroll(Long id) {
        if (payrollRepository.existsById(id)) {
            payslipRepository.findByPayrollId(id).ifPresent(p -> payslipRepository.delete(p));
            payrollRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
