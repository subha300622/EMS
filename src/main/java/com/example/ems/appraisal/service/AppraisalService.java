package com.example.ems.appraisal.service;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.Appraisal;
import com.example.ems.appraisal.entity.AppraisalCycle;
import com.example.ems.appraisal.entity.Increment;
import com.example.ems.appraisal.entity.IncrementPolicy;
import com.example.ems.appraisal.entity.SalaryRevision;
import com.example.ems.appraisal.repository.AppraisalCycleRepository;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.appraisal.repository.IncrementPolicyRepository;
import com.example.ems.appraisal.repository.IncrementRepository;
import com.example.ems.appraisal.repository.SalaryRevisionRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AppraisalService {

    @Autowired private AppraisalCycleRepository cycleRepository;
    @Autowired private AppraisalRepository appraisalRepository;
    @Autowired private IncrementPolicyRepository policyRepository;
    @Autowired private IncrementRepository incrementRepository;
    @Autowired private SalaryRevisionRepository revisionRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private UserRepository userRepository;

    @PostConstruct
    public void seedInitialData() {
        if (policyRepository.count() == 0) {
            savePolicy(1, 0.0, "Needs improvement - No increment");
            savePolicy(2, 2.0, "Below expectations - 2% baseline increment");
            savePolicy(3, 5.0, "Meets expectations - 5% standard increment");
            savePolicy(4, 10.0, "Exceeds expectations - 10% high performer increment");
            savePolicy(5, 15.0, "Outstanding - 15% top performer increment");
        }

        if (cycleRepository.count() == 0) {
            AppraisalCycle cycle = new AppraisalCycle();
            cycle.setName("Annual Appraisal Cycle 2026");
            cycle.setStartDate(LocalDate.of(2026, 1, 1));
            cycle.setEndDate(LocalDate.of(2026, 12, 31));
            cycle.setStatus("ACTIVE");
            cycleRepository.save(cycle);
        }
    }

    private void savePolicy(int rating, double percentage, String desc) {
        IncrementPolicy policy = new IncrementPolicy();
        policy.setRating(rating);
        policy.setRecommendedPercentage(BigDecimal.valueOf(percentage));
        policy.setDescription(desc);
        policyRepository.save(policy);
    }

    // ── 1. DASHBOARD ────────────────────────────────────────────────────────
    @Cacheable(value = "appraisalDashboard", key = "'stats'")
    public AppraisalDashboardResponse getDashboardStats() {
        AppraisalDashboardResponse stats = new AppraisalDashboardResponse();

        long totalAppraisals = appraisalRepository.count();
        long pendingSelf = appraisalRepository.findByStatus("PENDING").size();
        long pendingManager = appraisalRepository.findByStatus("SELF_REVIEWED").size();
        long finalized = appraisalRepository.findByStatus("FINALIZED").size();

        double avgRating = appraisalRepository.findAll().stream()
                .filter(a -> a.getFinalRating() != null)
                .mapToInt(Appraisal::getFinalRating)
                .average()
                .orElse(0.0);

        long totalInc = incrementRepository.count();
        long pendingInc = incrementRepository.findByStatus("PENDING").size();
        long approvedInc = incrementRepository.findByStatus("APPROVED").size();
        long appliedInc = incrementRepository.findByStatus("APPLIED").size();

        double avgIncPercent = incrementRepository.findAll().stream()
                .map(Increment::getIncrementPercentage)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        stats.setTotalAppraisals(totalAppraisals);
        stats.setPendingSelfReviews(pendingSelf);
        stats.setPendingManagerReviews(pendingManager);
        stats.setFinalizedAppraisals(finalized);
        stats.setAverageRating(Math.round(avgRating * 100.0) / 100.0);

        stats.setTotalIncrements(totalInc);
        stats.setPendingIncrements(pendingInc);
        stats.setApprovedIncrements(approvedInc);
        stats.setAppliedIncrements(appliedInc);
        stats.setAverageIncrementPercentage(Math.round(avgIncPercent * 100.0) / 100.0);

        return stats;
    }

    // ── 2. APPRAISALS ────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public AppraisalResponse createAppraisal(AppraisalRequest request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));
        AppraisalCycle cycle = cycleRepository.findById(request.getCycleId())
                .orElseThrow(() -> new IllegalArgumentException("Appraisal cycle not found with ID: " + request.getCycleId()));

        Appraisal app = new Appraisal();
        app.setEmployee(emp);
        app.setCycle(cycle);
        app.setStatus("PENDING");

        return new AppraisalResponse(appraisalRepository.save(app));
    }

    public List<AppraisalResponse> getAppraisals() {
        return appraisalRepository.findAll().stream()
                .map(AppraisalResponse::new)
                .collect(Collectors.toList());
    }

    public List<AppraisalResponse> getAppraisalsByEmployee(Long employeeId) {
        return appraisalRepository.findByEmployeeId(employeeId).stream()
                .map(AppraisalResponse::new)
                .collect(Collectors.toList());
    }

    public Optional<AppraisalResponse> getAppraisalById(Long id) {
        return appraisalRepository.findById(id).map(AppraisalResponse::new);
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<AppraisalResponse> submitSelfReview(Long id, AppraisalSelfReviewRequest request) {
        return appraisalRepository.findById(id).map(app -> {
            app.setSelfReview(request.getSelfReview());
            app.setSelfRating(request.getSelfRating());
            app.setSelfReviewSubmittedAt(LocalDateTime.now());
            app.setStatus("SELF_REVIEWED");
            app.setUpdatedAt(LocalDateTime.now());
            return new AppraisalResponse(appraisalRepository.save(app));
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<AppraisalResponse> submitManagerReview(Long id, AppraisalManagerReviewRequest request, String reviewerEmail) {
        User user = userRepository.findByWorkEmail(reviewerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + reviewerEmail));

        Employee manager = null;
        if (user.getEmployeeId() != null) {
            manager = employeeRepository.findById(Long.parseLong(user.getEmployeeId())).orElse(null);
        }

        Employee finalManager = manager;
        return appraisalRepository.findById(id).map(app -> {
            app.setManagerReview(request.getManagerReview());
            app.setManagerRating(request.getManagerRating());
            app.setReviewer(finalManager);
            app.setManagerReviewSubmittedAt(LocalDateTime.now());
            app.setStatus("MANAGER_REVIEWED");
            app.setUpdatedAt(LocalDateTime.now());
            return new AppraisalResponse(appraisalRepository.save(app));
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<AppraisalResponse> finalizeAppraisal(Long id, AppraisalFinalizeRequest request) {
        return appraisalRepository.findById(id).map(app -> {
            app.setFinalRating(request.getFinalRating());
            app.setStatus("FINALIZED");
            app.setUpdatedAt(LocalDateTime.now());
            return new AppraisalResponse(appraisalRepository.save(app));
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<AppraisalResponse> approveAppraisal(Long id) {
        return appraisalRepository.findById(id).map(app -> {
            app.setStatus("APPROVED");
            app.setUpdatedAt(LocalDateTime.now());
            return new AppraisalResponse(appraisalRepository.save(app));
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<AppraisalResponse> rejectAppraisal(Long id) {
        return appraisalRepository.findById(id).map(app -> {
            app.setStatus("REJECTED");
            app.setUpdatedAt(LocalDateTime.now());
            return new AppraisalResponse(appraisalRepository.save(app));
        });
    }

    // ── 2.1 NEW SALARY REVISION BUSINESS METHODS ─────────────────────────────
    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Increment createIncrement(NewIncrementRequest request) {
        Employee emp = employeeRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        Appraisal appraisal = null;
        if (request.getAppraisalId() != null) {
            appraisal = appraisalRepository.findById(request.getAppraisalId()).orElse(null);
        }

        BigDecimal currentSalary = emp.getAnnualSalary() != null ? emp.getAnnualSalary() : BigDecimal.ZERO;
        BigDecimal percent = request.getIncrementPercentage();
        BigDecimal amount = currentSalary.multiply(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal newSalary = currentSalary.add(amount);

        Increment inc = new Increment();
        inc.setEmployee(emp);
        inc.setAppraisal(appraisal);
        inc.setCurrentSalary(currentSalary);
        inc.setIncrementPercentage(percent);
        inc.setIncrementAmount(amount);
        inc.setNewSalary(newSalary);
        inc.setEffectiveDate(request.getEffectiveDate());
        inc.setReason(request.getReason());
        inc.setStatus("PENDING");

        return incrementRepository.save(inc);
    }

    public Optional<Increment> getIncrementEntityById(Long id) {
        return incrementRepository.findById(id);
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<Increment> updateIncrement(Long id, BigDecimal incrementPercentage, LocalDate effectiveDate, String reason) {
        return incrementRepository.findById(id).map(inc -> {
            if (incrementPercentage != null) {
                BigDecimal currentSalary = inc.getCurrentSalary();
                BigDecimal amount = currentSalary.multiply(incrementPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal newSalary = currentSalary.add(amount);
                
                inc.setIncrementPercentage(incrementPercentage);
                inc.setIncrementAmount(amount);
                inc.setNewSalary(newSalary);
            }
            if (effectiveDate != null) {
                inc.setEffectiveDate(effectiveDate);
            }
            if (reason != null) {
                inc.setReason(reason);
            }
            inc.setUpdatedAt(LocalDateTime.now());
            return incrementRepository.save(inc);
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<Increment> approveIncrementEntity(Long id, String approvedByEmail) {
        User user = userRepository.findByWorkEmail(approvedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + approvedByEmail));

        Employee manager = null;
        if (user.getEmployeeId() != null) {
            manager = employeeRepository.findByEmployeeId(user.getEmployeeId()).orElse(null);
        }

        Employee finalManager = manager;
        return incrementRepository.findById(id).map(inc -> {
            inc.setStatus("APPROVED");
            inc.setApprovedBy(finalManager);
            inc.setApprovedAt(LocalDateTime.now());
            inc.setUpdatedAt(LocalDateTime.now());
            return incrementRepository.save(inc);
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<Increment> rejectIncrementEntity(Long id, String rejectedByEmail, String reason) {
        User user = userRepository.findByWorkEmail(rejectedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + rejectedByEmail));

        Employee manager = null;
        if (user.getEmployeeId() != null) {
            manager = employeeRepository.findByEmployeeId(user.getEmployeeId()).orElse(null);
        }

        Employee finalManager = manager;
        return incrementRepository.findById(id).map(inc -> {
            inc.setStatus("REJECTED");
            inc.setApprovedBy(finalManager);
            inc.setApprovedAt(LocalDateTime.now());
            inc.setReason(reason);
            inc.setUpdatedAt(LocalDateTime.now());
            return incrementRepository.save(inc);
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<Increment> applyIncrementEntity(Long id) {
        return incrementRepository.findById(id).map(inc -> {
            Employee emp = inc.getEmployee();
            BigDecimal previousSalary = emp.getAnnualSalary() != null ? emp.getAnnualSalary() : BigDecimal.ZERO;
            
            // 1. Update employee salary
            emp.setAnnualSalary(inc.getNewSalary());
            employeeRepository.save(emp);

            // 2. Create Salary Revision Log
            SalaryRevision revision = new SalaryRevision();
            revision.setEmployee(emp);
            revision.setPreviousSalary(previousSalary);
            revision.setNewSalary(inc.getNewSalary());
            revision.setChangePercentage(inc.getIncrementPercentage());
            revision.setEffectiveDate(inc.getEffectiveDate());
            
            String reasonText = inc.getReason();
            if (reasonText == null || reasonText.trim().isEmpty()) {
                String ratingStr = inc.getAppraisal() != null && inc.getAppraisal().getFinalRating() != null
                        ? " (Final Rating: " + inc.getAppraisal().getFinalRating() + ")"
                        : "";
                reasonText = "Annual Appraisal Increment" + ratingStr;
            }
            revision.setReason(reasonText);
            revisionRepository.save(revision);

            // 3. Update Increment Status
            inc.setStatus("APPLIED");
            inc.setAppliedAt(LocalDateTime.now());
            inc.setUpdatedAt(LocalDateTime.now());

            return incrementRepository.save(inc);
        });
    }

    // ── 3. INCREMENTS ────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public IncrementResponse createIncrement(IncrementRequest request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        Appraisal appraisal = null;
        if (request.getAppraisalId() != null) {
            appraisal = appraisalRepository.findById(request.getAppraisalId()).orElse(null);
        }

        BigDecimal currentSalary = emp.getAnnualSalary() != null ? emp.getAnnualSalary() : BigDecimal.ZERO;
        BigDecimal percent = request.getIncrementPercentage();
        BigDecimal amount = currentSalary.multiply(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal newSalary = currentSalary.add(amount);

        Increment inc = new Increment();
        inc.setEmployee(emp);
        inc.setAppraisal(appraisal);
        inc.setCurrentSalary(currentSalary);
        inc.setIncrementPercentage(percent);
        inc.setIncrementAmount(amount);
        inc.setNewSalary(newSalary);
        inc.setEffectiveDate(request.getEffectiveDate());
        inc.setStatus("PENDING");

        return new IncrementResponse(incrementRepository.save(inc));
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<IncrementResponse> approveIncrement(Long id, String approvedByEmail) {
        User user = userRepository.findByWorkEmail(approvedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + approvedByEmail));

        Employee manager = null;
        if (user.getEmployeeId() != null) {
            manager = employeeRepository.findById(Long.parseLong(user.getEmployeeId())).orElse(null);
        }

        Employee finalManager = manager;
        return incrementRepository.findById(id).map(inc -> {
            inc.setStatus("APPROVED");
            inc.setApprovedBy(finalManager);
            inc.setApprovedAt(LocalDateTime.now());
            inc.setUpdatedAt(LocalDateTime.now());
            return new IncrementResponse(incrementRepository.save(inc));
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<IncrementResponse> applyIncrement(Long id) {
        return incrementRepository.findById(id).map(inc -> {
            Employee emp = inc.getEmployee();
            BigDecimal previousSalary = emp.getAnnualSalary() != null ? emp.getAnnualSalary() : BigDecimal.ZERO;
            
            // 1. Update employee salary
            emp.setAnnualSalary(inc.getNewSalary());
            employeeRepository.save(emp);

            // 2. Create Salary Revision Log
            SalaryRevision revision = new SalaryRevision();
            revision.setEmployee(emp);
            revision.setPreviousSalary(previousSalary);
            revision.setNewSalary(inc.getNewSalary());
            revision.setChangePercentage(inc.getIncrementPercentage());
            revision.setEffectiveDate(inc.getEffectiveDate());
            
            String ratingStr = inc.getAppraisal() != null && inc.getAppraisal().getFinalRating() != null
                    ? " (Final Rating: " + inc.getAppraisal().getFinalRating() + ")"
                    : "";
            revision.setReason("Annual Appraisal Increment" + ratingStr);
            revisionRepository.save(revision);

            // 3. Update Increment Status
            inc.setStatus("APPLIED");
            inc.setAppliedAt(LocalDateTime.now());
            inc.setUpdatedAt(LocalDateTime.now());

            return new IncrementResponse(incrementRepository.save(inc));
        });
    }

    public org.springframework.data.domain.Page<IncrementResponse> getSalaryRevisions(String status, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Increment> page;
        if (status != null && !status.trim().isEmpty()) {
            page = incrementRepository.findByStatus(status.trim().toUpperCase(), pageable);
        } else {
            page = incrementRepository.findAll(pageable);
        }
        return page.map(IncrementResponse::new);
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<IncrementResponse> updateIncrement(Long id, IncrementRequest request) {
        return incrementRepository.findById(id).map(inc -> {
            if (request.getIncrementPercentage() != null) {
                BigDecimal percent = request.getIncrementPercentage();
                BigDecimal currentSalary = inc.getCurrentSalary();
                BigDecimal amount = currentSalary.multiply(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal newSalary = currentSalary.add(amount);
                
                inc.setIncrementPercentage(percent);
                inc.setIncrementAmount(amount);
                inc.setNewSalary(newSalary);
            }
            if (request.getEffectiveDate() != null) {
                inc.setEffectiveDate(request.getEffectiveDate());
            }
            inc.setUpdatedAt(LocalDateTime.now());
            return new IncrementResponse(incrementRepository.save(inc));
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<IncrementResponse> rejectIncrement(Long id, String rejectedByEmail) {
        User user = userRepository.findByWorkEmail(rejectedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + rejectedByEmail));

        Employee manager = null;
        if (user.getEmployeeId() != null) {
            manager = employeeRepository.findById(Long.parseLong(user.getEmployeeId())).orElse(null);
        }

        Employee finalManager = manager;
        return incrementRepository.findById(id).map(inc -> {
            inc.setStatus("REJECTED");
            inc.setApprovedBy(finalManager);
            inc.setApprovedAt(LocalDateTime.now());
            inc.setUpdatedAt(LocalDateTime.now());
            return new IncrementResponse(incrementRepository.save(inc));
        });
    }

    public Optional<IncrementResponse> getIncrementById(Long id) {
        return incrementRepository.findById(id).map(IncrementResponse::new);
    }

    public List<SalaryRevisionResponse> getSalaryRevisions(Long employeeId) {
        return revisionRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId).stream()
                .map(SalaryRevisionResponse::new)
                .collect(Collectors.toList());
    }

    public List<SalaryRevision> getSalaryRevisionEntities(Long employeeId) {
        return revisionRepository.findByEmployeeIdOrderByEffectiveDateDesc(employeeId);
    }


    public List<AppraisalCycleResponse> getAppraisalCycles() {
        return cycleRepository.findAll().stream()
                .map(AppraisalCycleResponse::new)
                .collect(Collectors.toList());
    }

    public List<IncrementPolicyResponse> getIncrementPolicies() {
        return policyRepository.findAll().stream()
                .map(IncrementPolicyResponse::new)
                .collect(Collectors.toList());
    }

    // ── 4. REPORTS ───────────────────────────────────────────────────────────
    public Map<String, Object> getAppraisalsReport(String reportType) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reportType", reportType);
        data.put("module", "Appraisals");
        data.put("generatedAt", LocalDateTime.now());

        long total = appraisalRepository.count();
        long finalized = appraisalRepository.findByStatus("FINALIZED").size();
        double avgRating = appraisalRepository.findAll().stream()
                .filter(a -> a.getFinalRating() != null)
                .mapToInt(Appraisal::getFinalRating)
                .average()
                .orElse(0.0);

        data.put("totalAppraisalsCount", total);
        data.put("finalizedAppraisalsCount", finalized);
        data.put("averageFinalRating", Math.round(avgRating * 100.0) / 100.0);

        return data;
    }

    public Map<String, Object> getIncrementsReport(String reportType) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reportType", reportType);
        data.put("module", "Increments");
        data.put("generatedAt", LocalDateTime.now());

        List<Increment> applied = incrementRepository.findByStatus("APPLIED");
        BigDecimal totalRevisionBudget = applied.stream()
                .map(Increment::getIncrementAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double avgPercentage = incrementRepository.findAll().stream()
                .map(Increment::getIncrementPercentage)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(0.0);

        data.put("totalAppliedIncrementsCount", applied.size());
        data.put("totalIncrementBudgetAmount", totalRevisionBudget);
        data.put("averageIncrementPercentage", Math.round(avgPercentage * 100.0) / 100.0);

        return data;
    }

    // ── 5. LETTER ────────────────────────────────────────────────────────────
    public IncrementLetterResponse getIncrementLetter(Long incrementId) {
        Increment inc = incrementRepository.findById(incrementId)
                .orElseThrow(() -> new IllegalArgumentException("Increment record not found with ID: " + incrementId));

        Employee emp = inc.getEmployee();
        IncrementLetterResponse letter = new IncrementLetterResponse();
        letter.setEmployeeName(emp.getFullName());
        letter.setEmployeeId(emp.getEmployeeId());
        letter.setDepartment(emp.getDepartment());
        letter.setDesignation(emp.getDesignation());
        letter.setCurrentSalary(inc.getCurrentSalary());
        letter.setNewSalary(inc.getNewSalary());
        letter.setIncrementPercentage(inc.getIncrementPercentage());
        letter.setIncrementAmount(inc.getIncrementAmount());
        letter.setEffectiveDate(inc.getEffectiveDate());

        String statusStr = inc.getStatus();
        String noticeHeader = "SUBJECT: SALARY REVISION CONFIRMATION";
        if ("PENDING".equals(statusStr)) {
            noticeHeader = "SUBJECT: PROPOSED SALARY REVISION NOTICE (PENDING APPROVAL)";
        }

        String body = String.format(
                "Dear %s,\n\n" +
                "We are writing to officially confirm the revision of your salary details.\n\n" +
                "Based on the recent performance appraisal cycle, your annual compensation package has been updated as follows:\n" +
                "- Previous Salary: $%s\n" +
                "- Increment Percentage: %s%%\n" +
                "- Increment Amount: $%s\n" +
                "- New Revised Salary: $%s\n" +
                "- Effective Date: %s\n\n" +
                "Your revised compensation will reflect in the payroll cycle following the effective date. " +
                "Thank you for your continuous dedication and valuable contributions to the company.\n\n" +
                "Sincerely,\n" +
                "Human Resources Department\n" +
                "Employee Management System (EMS)",
                emp.getFullName(),
                inc.getCurrentSalary(),
                inc.getIncrementPercentage(),
                inc.getIncrementAmount(),
                inc.getNewSalary(),
                inc.getEffectiveDate()
        );

        letter.setLetterBody(noticeHeader + "\n\n" + body);
        return letter;
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<AppraisalResponse> updateAppraisal(Long id, AppraisalRequest request) {
        return appraisalRepository.findById(id).map(app -> {
            Employee emp = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));
            AppraisalCycle cycle = cycleRepository.findById(request.getCycleId())
                    .orElseThrow(() -> new IllegalArgumentException("Appraisal cycle not found with ID: " + request.getCycleId()));

            app.setEmployee(emp);
            app.setCycle(cycle);
            app.setUpdatedAt(LocalDateTime.now());
            return new AppraisalResponse(appraisalRepository.save(app));
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public boolean deleteAppraisal(Long id) {
        if (appraisalRepository.existsById(id)) {
            // Find all increments pointing to this appraisal and set reference to null
            incrementRepository.findAll().stream()
                    .filter(inc -> inc.getAppraisal() != null && inc.getAppraisal().getId().equals(id))
                    .forEach(inc -> {
                        inc.setAppraisal(null);
                        incrementRepository.save(inc);
                    });
            
            // Finally delete appraisal
            appraisalRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
