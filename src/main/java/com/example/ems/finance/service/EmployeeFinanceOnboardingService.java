package com.example.ems.finance.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.finance.entity.EmployeeFinanceOnboarding;
import com.example.ems.finance.entity.FinanceOnboardingHistory;
import com.example.ems.finance.repository.EmployeeFinanceOnboardingRepository;
import com.example.ems.finance.repository.FinanceOnboardingHistoryRepository;
import com.example.ems.payroll.entity.SalaryStructure;
import com.example.ems.payroll.repository.SalaryStructureRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeFinanceOnboardingService {

    @Autowired
    private EmployeeFinanceOnboardingRepository repository;

    @Autowired
    private FinanceOnboardingHistoryRepository historyRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryStructureRepository salaryStructureRepository;

    // ── 1. CREATE ONBOARDING ────────────────────────────────────────────────
    @Transactional
    public EmployeeFinanceOnboarding create(Map<String, Object> data, String userEmail) {
        Long employeeId = Long.valueOf(data.get("employeeId").toString());
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + employeeId));

        Optional<EmployeeFinanceOnboarding> existing = repository.findByEmployeeId(employeeId);
        if (existing.isPresent()) {
            throw new IllegalStateException("Finance onboarding already initialized for employee: " + employee.getFullName());
        }

        EmployeeFinanceOnboarding ob = new EmployeeFinanceOnboarding();
        ob.setEmployee(employee);
        ob.setStatus(data.getOrDefault("status", "PENDING").toString());
        
        ob.setBankName((String) data.get("bankName"));
        ob.setBankAccountNumber((String) data.get("bankAccountNumber"));
        ob.setBankIfsc((String) data.get("bankIfsc"));

        ob.setPanNumber((String) data.get("panNumber"));
        ob.setUanNumber((String) data.get("uanNumber"));

        EmployeeFinanceOnboarding saved = repository.save(ob);

        // Audit log
        historyRepository.save(new FinanceOnboardingHistory(saved.getId(), "CREATED", userEmail, "Finance onboarding record initialized"));

        return saved;
    }

    // ── 2. GET LIST ─────────────────────────────────────────────────────────
    public List<EmployeeFinanceOnboarding> list(String status, String search, int page, int size) {
        List<EmployeeFinanceOnboarding> list = repository.findAll();

        if (status != null && !status.isBlank() && !status.equalsIgnoreCase("ALL")) {
            list = list.stream()
                    .filter(ob -> status.equalsIgnoreCase(ob.getStatus()))
                    .collect(Collectors.toList());
        }

        if (search != null && !search.isBlank()) {
            String s = search.toLowerCase().trim();
            list = list.stream()
                    .filter(ob -> {
                        Employee emp = ob.getEmployee();
                        if (emp == null) return false;
                        return (emp.getFullName() != null && emp.getFullName().toLowerCase().contains(s)) ||
                               (emp.getEmployeeId() != null && emp.getEmployeeId().toLowerCase().contains(s)) ||
                               (emp.getEmail() != null && emp.getEmail().toLowerCase().contains(s));
                    })
                    .collect(Collectors.toList());
        }

        int start = page * size;
        if (start >= list.size()) {
            return new ArrayList<>();
        }
        int end = Math.min(start + size, list.size());
        return list.subList(start, end);
    }

    // ── 3. GET BY ID ────────────────────────────────────────────────────────
    public Optional<EmployeeFinanceOnboarding> get(Long id) {
        return repository.findById(id);
    }

    // ── 4. UPDATE ───────────────────────────────────────────────────────────
    @Transactional
    public EmployeeFinanceOnboarding update(Long id, Map<String, Object> data, String userEmail) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));

        if (data.containsKey("bankName")) ob.setBankName((String) data.get("bankName"));
        if (data.containsKey("bankAccountNumber")) ob.setBankAccountNumber((String) data.get("bankAccountNumber"));
        if (data.containsKey("bankIfsc")) ob.setBankIfsc((String) data.get("bankIfsc"));
        if (data.containsKey("panNumber")) ob.setPanNumber((String) data.get("panNumber"));
        if (data.containsKey("uanNumber")) ob.setUanNumber((String) data.get("uanNumber"));
        if (data.containsKey("status")) ob.setStatus((String) data.get("status"));

        EmployeeFinanceOnboarding updated = repository.save(ob);

        // Audit log
        historyRepository.save(new FinanceOnboardingHistory(id, "UPDATED", userEmail, "Details updated"));

        return updated;
    }

    // ── 5. DELETE ───────────────────────────────────────────────────────────
    @Transactional
    public void delete(Long id) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));
        if (!"PENDING".equalsIgnoreCase(ob.getStatus()) && !"DRAFT".equalsIgnoreCase(ob.getStatus())) {
            throw new IllegalStateException("Only pending or draft onboarding records can be deleted.");
        }
        repository.delete(ob);
    }

    // ── 6. VERIFICATION APIS ────────────────────────────────────────────────
    @Transactional
    public EmployeeFinanceOnboarding verifyBank(Long id, String status, String notes, String userEmail) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));
        ob.setBankVerificationStatus(status.toUpperCase());
        ob.setBankVerificationNotes(notes);

        EmployeeFinanceOnboarding saved = repository.save(ob);
        historyRepository.save(new FinanceOnboardingHistory(id, "VERIFIED_BANK", userEmail, "Bank verification status: " + status.toUpperCase() + ". Notes: " + notes));
        return saved;
    }

    @Transactional
    public EmployeeFinanceOnboarding verifyPan(Long id, String status, String notes, String userEmail) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));
        ob.setPanVerificationStatus(status.toUpperCase());
        ob.setPanVerificationNotes(notes);

        EmployeeFinanceOnboarding saved = repository.save(ob);
        historyRepository.save(new FinanceOnboardingHistory(id, "VERIFIED_PAN", userEmail, "PAN verification status: " + status.toUpperCase() + ". Notes: " + notes));
        return saved;
    }

    @Transactional
    public EmployeeFinanceOnboarding verifyUan(Long id, String status, String notes, String userEmail) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));
        ob.setUanVerificationStatus(status.toUpperCase());
        ob.setUanVerificationNotes(notes);

        EmployeeFinanceOnboarding saved = repository.save(ob);
        historyRepository.save(new FinanceOnboardingHistory(id, "VERIFIED_UAN", userEmail, "PF/UAN verification status: " + status.toUpperCase() + ". Notes: " + notes));
        return saved;
    }

    public Map<String, Object> getVerificationStatus(Long id) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("bankStatus", ob.getBankVerificationStatus());
        map.put("bankNotes", ob.getBankVerificationNotes());
        map.put("panStatus", ob.getPanVerificationStatus());
        map.put("panNotes", ob.getPanVerificationNotes());
        map.put("uanStatus", ob.getUanVerificationStatus());
        map.put("uanNotes", ob.getUanVerificationNotes());
        return map;
    }

    // ── 7. APPROVAL APIS ────────────────────────────────────────────────────
    @Transactional
    public EmployeeFinanceOnboarding approve(Long id, String userEmail, String notes) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));
        ob.setStatus("APPROVED");
        
        EmployeeFinanceOnboarding saved = repository.save(ob);
        historyRepository.save(new FinanceOnboardingHistory(id, "APPROVED", userEmail, notes != null ? notes : "Approved by finance manager"));
        return saved;
    }

    @Transactional
    public EmployeeFinanceOnboarding reject(Long id, String userEmail, String notes) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));
        ob.setStatus("REJECTED");

        EmployeeFinanceOnboarding saved = repository.save(ob);
        historyRepository.save(new FinanceOnboardingHistory(id, "REJECTED", userEmail, notes != null ? notes : "Rejected by finance manager"));
        return saved;
    }

    @Transactional
    public EmployeeFinanceOnboarding sendBack(Long id, String userEmail, String notes) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));
        ob.setStatus("SENT_BACK");

        EmployeeFinanceOnboarding saved = repository.save(ob);
        historyRepository.save(new FinanceOnboardingHistory(id, "SENT_BACK", userEmail, notes != null ? notes : "Sent back for correction"));
        return saved;
    }

    public List<FinanceOnboardingHistory> getHistory(Long id) {
        return historyRepository.findByOnboardingIdOrderByTimestampDesc(id);
    }

    // ── 8. SALARY SETUP APIS ────────────────────────────────────────────────
    @Transactional
    public EmployeeFinanceOnboarding assignSalaryStructure(Long id, BigDecimal basic, BigDecimal hra, BigDecimal allowances, String userEmail) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));

        ob.setBasicSalary(basic != null ? basic : BigDecimal.ZERO);
        ob.setHra(hra != null ? hra : BigDecimal.ZERO);
        ob.setAllowances(allowances != null ? allowances : BigDecimal.ZERO);
        ob.setMonthlyCtc(ob.getBasicSalary().add(ob.getHra()).add(ob.getAllowances()));
        ob.setSalaryStructureAssigned(true);

        EmployeeFinanceOnboarding saved = repository.save(ob);

        // Sync with SalaryStructure entity
        Employee emp = ob.getEmployee();
        if (emp != null) {
            SalaryStructure ss = salaryStructureRepository.findByEmployeeId(emp.getId())
                    .orElse(new SalaryStructure());
            ss.setEmployeeId(emp.getId());
            ss.setBasicSalary(ob.getBasicSalary());
            ss.setHra(ob.getHra());
            ss.setAllowances(ob.getAllowances());
            salaryStructureRepository.save(ss);
        }

        historyRepository.save(new FinanceOnboardingHistory(id, "SALARY_ASSIGNED", userEmail, "Salary structure assigned: Basic=" + basic + ", HRA=" + hra + ", Allowances=" + allowances));
        return saved;
    }

    public Map<String, Object> getSalaryStructure(Long id) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("basicSalary", ob.getBasicSalary());
        map.put("hra", ob.getHra());
        map.put("allowances", ob.getAllowances());
        map.put("monthlyCtc", ob.getMonthlyCtc());
        map.put("assigned", ob.getSalaryStructureAssigned());
        return map;
    }

    public Map<String, Object> calculateCtcBreakup(BigDecimal monthlyCtc) {
        BigDecimal basic = monthlyCtc.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal hra = basic.multiply(BigDecimal.valueOf(0.4)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal allowances = monthlyCtc.subtract(basic).subtract(hra).setScale(2, RoundingMode.HALF_UP);

        BigDecimal pf = basic.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pt = BigDecimal.valueOf(200.00);
        BigDecimal tax = monthlyCtc.multiply(BigDecimal.valueOf(0.10)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netPay = monthlyCtc.subtract(pf).subtract(pt).subtract(tax).setScale(2, RoundingMode.HALF_UP);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("annualCtc", monthlyCtc.multiply(BigDecimal.valueOf(12)));
        map.put("monthlyCtc", monthlyCtc);
        map.put("basicSalary", basic);
        map.put("hra", hra);
        map.put("allowances", allowances);
        map.put("providentFund", pf);
        map.put("professionalTax", pt);
        map.put("incomeTax", tax);
        map.put("netPay", netPay);
        return map;
    }

    public Map<String, Object> getSalaryPreview(Long id) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));

        BigDecimal basic;
        BigDecimal hra;
        BigDecimal allowances;

        if (ob.getSalaryStructureAssigned()) {
            basic = ob.getBasicSalary();
            hra = ob.getHra();
            allowances = ob.getAllowances();
        } else {
            BigDecimal monthlyCtc = BigDecimal.ZERO;
            if (ob.getEmployee() != null && ob.getEmployee().getAnnualSalary() != null) {
                monthlyCtc = ob.getEmployee().getAnnualSalary().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            }
            if (monthlyCtc.compareTo(BigDecimal.ZERO) == 0) {
                monthlyCtc = BigDecimal.valueOf(85000.00);
            }

            basic = monthlyCtc.multiply(BigDecimal.valueOf(50)).divide(BigDecimal.valueOf(85), 2, RoundingMode.HALF_UP);
            hra = monthlyCtc.multiply(BigDecimal.valueOf(25)).divide(BigDecimal.valueOf(85), 2, RoundingMode.HALF_UP);
            allowances = monthlyCtc.subtract(basic).subtract(hra);
        }

        BigDecimal grossSalary = basic.add(hra).add(allowances);
        BigDecimal calculatedPf = basic.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pf = calculatedPf.min(BigDecimal.valueOf(1800.00));
        BigDecimal netSalary = grossSalary.subtract(pf);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("basic", basic);
        map.put("hra", hra);
        map.put("allowances", allowances);
        map.put("grossSalary", grossSalary);
        map.put("pf", pf);
        map.put("netSalary", netSalary);
        return map;
    }

    // ── 9. PAYROLL INTEGRATION APIS ─────────────────────────────────────────
    @Transactional
    public EmployeeFinanceOnboarding activatePayroll(Long id, String userEmail) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));
        ob.setPayrollActivated(true);
        ob.setPayrollActivatedAt(LocalDateTime.now());

        EmployeeFinanceOnboarding saved = repository.save(ob);
        historyRepository.save(new FinanceOnboardingHistory(id, "PAYROLL_ACTIVATED", userEmail, "Payroll activated successfully for employee"));
        return saved;
    }

    public Map<String, Object> getPayrollStatus(Long id) {
        EmployeeFinanceOnboarding ob = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + id));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("activated", ob.getPayrollActivated());
        map.put("activatedAt", ob.getPayrollActivatedAt());
        return map;
    }

    // ── 10. DASHBOARD SUMMARY ────────────────────────────────────────────────
    public Map<String, Object> getDashboardSummary() {
        List<EmployeeFinanceOnboarding> all = repository.findAll();

        long pendingVerification = all.stream().filter(ob -> "PENDING".equalsIgnoreCase(ob.getStatus()) || "DRAFT".equalsIgnoreCase(ob.getStatus()) || "SENT_BACK".equalsIgnoreCase(ob.getStatus())).count();
        long salaryAssignmentPending = all.stream().filter(ob -> !ob.getSalaryStructureAssigned()).count();
        long payrollActivationPending = all.stream().filter(ob -> "APPROVED".equalsIgnoreCase(ob.getStatus()) && !ob.getPayrollActivated()).count();
        long completed = all.stream().filter(ob -> ob.getPayrollActivated()).count();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("pendingVerification", pendingVerification);
        map.put("salaryAssignmentPending", salaryAssignmentPending);
        map.put("payrollActivationPending", payrollActivationPending);
        map.put("completed", completed);
        return map;
    }

    public List<Map<String, Object>> getRecentActivities() {
        List<FinanceOnboardingHistory> histories = historyRepository.findTop10ByOrderByTimestampDesc();
        List<Map<String, Object>> list = new ArrayList<>();
        
        for (FinanceOnboardingHistory h : histories) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", h.getId());
            map.put("onboardingId", h.getOnboardingId());
            map.put("action", h.getAction());
            map.put("performedBy", h.getPerformedBy());
            map.put("notes", h.getNotes());
            map.put("timestamp", h.getTimestamp());

            // Try to find employee name
            repository.findById(h.getOnboardingId()).ifPresent(ob -> {
                if (ob.getEmployee() != null) {
                    map.put("employeeName", ob.getEmployee().getFullName());
                }
            });
            
            list.add(map);
        }
        return list;
    }

    // ── 11. REPORTS ──────────────────────────────────────────────────────────
    public List<Map<String, Object>> getReportData() {
        List<EmployeeFinanceOnboarding> all = repository.findAll();
        List<Map<String, Object>> details = new ArrayList<>();

        for (EmployeeFinanceOnboarding ob : all) {
            Employee emp = ob.getEmployee();
            if (emp == null) continue;

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("onboardingId", ob.getId());
            map.put("employeeId", emp.getEmployeeId());
            map.put("employeeName", emp.getFullName());
            map.put("employeeEmail", emp.getEmail());
            map.put("joiningDate", emp.getJoiningDate());
            map.put("status", ob.getStatus());
            map.put("bankVerified", "VERIFIED".equalsIgnoreCase(ob.getBankVerificationStatus()));
            map.put("panVerified", "VERIFIED".equalsIgnoreCase(ob.getPanVerificationStatus()));
            map.put("uanVerified", "VERIFIED".equalsIgnoreCase(ob.getUanVerificationStatus()));
            map.put("salaryStructureAssigned", ob.getSalaryStructureAssigned());
            map.put("payrollActivated", ob.getPayrollActivated());
            details.add(map);
        }
        return details;
    }

    public String exportReportAsCsv() {
        List<Map<String, Object>> details = getReportData();
        StringBuilder sb = new StringBuilder();
        sb.append("Onboarding ID,Employee ID,Employee Name,Email,Joining Date,Status,Bank Verified,PAN Verified,UAN Verified,Salary Assigned,Payroll Activated\n");
        for (Map<String, Object> row : details) {
            sb.append(row.get("onboardingId")).append(",")
              .append(row.get("employeeId")).append(",")
              .append("\"").append(row.get("employeeName")).append("\",")
              .append(row.get("employeeEmail")).append(",")
              .append(row.get("joiningDate")).append(",")
              .append(row.get("status")).append(",")
              .append(row.get("bankVerified")).append(",")
              .append(row.get("panVerified")).append(",")
              .append(row.get("uanVerified")).append(",")
              .append(row.get("salaryStructureAssigned")).append(",")
              .append(row.get("payrollActivated")).append("\n");
        }
        return sb.toString();
    }

    public EmployeeFinanceOnboarding getByEmployeeId(Long employeeId) {
        return repository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found for employee with ID: " + employeeId));
    }

    public List<EmployeeFinanceOnboarding> getPendingReviews(String department, String status) {
        return repository.findAll().stream().filter(ob -> {
            boolean statusMatches;
            if (status != null && !status.isBlank()) {
                statusMatches = status.equalsIgnoreCase(ob.getStatus());
            } else {
                statusMatches = "PENDING".equalsIgnoreCase(ob.getStatus()) ||
                                "DRAFT".equalsIgnoreCase(ob.getStatus()) ||
                                "SENT_BACK".equalsIgnoreCase(ob.getStatus());
            }

            boolean deptMatches = true;
            if (department != null && !department.isBlank()) {
                deptMatches = ob.getEmployee() != null && department.equalsIgnoreCase(ob.getEmployee().getDepartment());
            }

            return statusMatches && deptMatches;
        }).collect(Collectors.toList());
    }

    @Transactional
    public EmployeeFinanceOnboarding verifyFinancialDetails(Long onboardingId, Boolean bankVerified, Boolean panVerified, Boolean uanVerified, String remarks, String userEmail) {
        EmployeeFinanceOnboarding ob = repository.findById(onboardingId)
                .orElseThrow(() -> new IllegalArgumentException("Onboarding record not found with ID: " + onboardingId));

        ob.setBankVerificationStatus(Boolean.TRUE.equals(bankVerified) ? "VERIFIED" : "REJECTED");
        ob.setBankVerificationNotes(remarks);

        ob.setPanVerificationStatus(Boolean.TRUE.equals(panVerified) ? "VERIFIED" : "REJECTED");
        ob.setPanVerificationNotes(remarks);

        ob.setUanVerificationStatus(Boolean.TRUE.equals(uanVerified) ? "VERIFIED" : "REJECTED");
        ob.setUanVerificationNotes(remarks);

        if (Boolean.TRUE.equals(bankVerified) && Boolean.TRUE.equals(panVerified) && Boolean.TRUE.equals(uanVerified)) {
            ob.setStatus("APPROVED");
        } else {
            ob.setStatus("SENT_BACK");
        }

        EmployeeFinanceOnboarding saved = repository.save(ob);
        historyRepository.save(new FinanceOnboardingHistory(onboardingId, "VERIFIED_DETAILS", userEmail,
                String.format("Batch verification: Bank=%s, PAN=%s, UAN=%s. Remarks: %s", bankVerified, panVerified, uanVerified, remarks)));
        return saved;
    }
}
