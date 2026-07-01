package com.example.ems.appraisal.service;

import com.example.ems.appraisal.dto.*;
import com.example.ems.appraisal.entity.*;
import com.example.ems.appraisal.repository.AppraisalCycleRepository;
import com.example.ems.appraisal.repository.AppraisalRepository;
import com.example.ems.appraisal.repository.IncrementPolicyRepository;
import com.example.ems.appraisal.repository.IncrementRepository;
import com.example.ems.appraisal.repository.SalaryRevisionRepository;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.common.exception.BadRequestException;

import com.example.ems.appraisal.entity.AppraisalStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;

import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;

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

    @Autowired
    private AppraisalCycleRepository cycleRepository;
    @Autowired
    private AppraisalRepository appraisalRepository;
    @Autowired
    private IncrementPolicyRepository policyRepository;
    @Autowired
    private IncrementRepository incrementRepository;
    @Autowired
    private SalaryRevisionRepository revisionRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private com.example.ems.attendance.service.AttendanceService attendanceService;
    @Autowired
    private com.example.ems.leave.repository.LeaveRepository leaveRepository;
    @Autowired
    private com.example.ems.performance.repository.GoalRepository goalRepository;

    @Autowired
    private com.example.ems.appraisal.repository.AppraisalCommentRepository commentRepository;
    @Autowired
    private com.example.ems.appraisal.repository.AppraisalTimelineEventRepository timelineEventRepository;
    @Autowired
    private com.example.ems.appraisal.repository.AppraisalHistoryRepository historyRepository;
    @Autowired
    private com.example.ems.appraisal.repository.BulkAppraisalActionLogRepository bulkActionLogRepository;

    @Transactional
    public void seedCoreAppraisalData() {
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
        long pendingSelf = appraisalRepository.findByStatus(AppraisalStatus.ELIGIBLE).size();
        long pendingManager = appraisalRepository.findByStatus(AppraisalStatus.DRAFT).size();
        long finalized = appraisalRepository.findByStatus(AppraisalStatus.PENDING_FINANCE).size();

        double avgRating = appraisalRepository.findAll().stream()
                .filter(a -> a.getFinalRating() != null)
                .mapToDouble(Appraisal::getFinalRating)
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
                .orElseThrow(
                        () -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));
        AppraisalCycle cycle = cycleRepository.findById(request.getCycleId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Appraisal cycle not found with ID: " + request.getCycleId()));

        Appraisal app = new Appraisal();
        app.setEmployee(emp);
        app.setCycle(cycle);
        app.setStatus(AppraisalStatus.ELIGIBLE);

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
            app.setStatus(AppraisalStatus.DRAFT);
            app.setUpdatedAt(LocalDateTime.now());
            return new AppraisalResponse(appraisalRepository.save(app));
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<AppraisalResponse> submitManagerReview(Long id, AppraisalManagerReviewRequest request,
            String reviewerEmail) {
        userRepository.findByWorkEmail(reviewerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + reviewerEmail));

        Employee manager = employeeRepository.findByEmail(reviewerEmail).orElse(null);

        Employee finalManager = manager;
        return appraisalRepository.findById(id).map(app -> {
            StateTransitionValidator.validate(app.getStatus(), AppraisalStatus.MANAGER_APPROVED);
            app.setManagerReview(request.getManagerReview());
            app.setManagerRating(request.getManagerRating());
            app.setReviewer(finalManager);
            app.setManagerReviewSubmittedAt(LocalDateTime.now());
            app.setStatus(AppraisalStatus.MANAGER_APPROVED);
            app.setUpdatedAt(LocalDateTime.now());
            return new AppraisalResponse(appraisalRepository.save(app));
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<AppraisalResponse> finalizeAppraisal(Long id, AppraisalFinalizeRequest request) {
        return appraisalRepository.findById(id).map(app -> {
            app.setFinalRating(request.getFinalRating());
            app.setStatus(AppraisalStatus.PENDING_FINANCE);
            app.setUpdatedAt(LocalDateTime.now());
            return new AppraisalResponse(appraisalRepository.save(app));
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<AppraisalResponse> approveAppraisal(Long id) {
        return appraisalRepository.findById(id).map(app -> {
            app.setStatus(AppraisalStatus.FINANCE_APPROVED);
            app.setUpdatedAt(LocalDateTime.now());
            return new AppraisalResponse(appraisalRepository.save(app));
        });
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<AppraisalResponse> rejectAppraisal(Long id) {
        return appraisalRepository.findById(id).map(app -> {
            app.setStatus(AppraisalStatus.FINANCE_REJECTED);
            app.setUpdatedAt(LocalDateTime.now());
            return new AppraisalResponse(appraisalRepository.save(app));
        });
    }

    // ── 2.1 NEW SALARY REVISION BUSINESS METHODS ─────────────────────────────
    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Increment createIncrement(NewIncrementRequest request) {
        Employee emp = employeeRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

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
    public Optional<Increment> updateIncrement(Long id, BigDecimal incrementPercentage, LocalDate effectiveDate,
            String reason) {
        return incrementRepository.findById(id).map(inc -> {
            if (incrementPercentage != null) {
                BigDecimal currentSalary = inc.getCurrentSalary();
                BigDecimal amount = currentSalary.multiply(incrementPercentage).divide(BigDecimal.valueOf(100), 2,
                        RoundingMode.HALF_UP);
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

            if (inc.getAppraisal() != null) {
                Appraisal appraisal = inc.getAppraisal();
                appraisal.setStatus(AppraisalStatus.PROCESSED);
                appraisal.setUpdatedAt(LocalDateTime.now());
                appraisalRepository.save(appraisal);
            }

            return incrementRepository.save(inc);
        });
    }

    // ── 3. INCREMENTS ────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public IncrementResponse createIncrement(IncrementRequest request) {
        Employee emp = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

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
        userRepository.findByWorkEmail(approvedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + approvedByEmail));

        Employee manager = employeeRepository.findByEmail(approvedByEmail).orElse(null);

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

            if (inc.getAppraisal() != null) {
                Appraisal appraisal = inc.getAppraisal();
                appraisal.setStatus(AppraisalStatus.PROCESSED);
                appraisal.setUpdatedAt(LocalDateTime.now());
                appraisalRepository.save(appraisal);
            }

            return new IncrementResponse(incrementRepository.save(inc));
        });
    }

    public org.springframework.data.domain.Page<IncrementResponse> getSalaryRevisions(String status,
            org.springframework.data.domain.Pageable pageable) {
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
                BigDecimal amount = currentSalary.multiply(percent).divide(BigDecimal.valueOf(100), 2,
                        RoundingMode.HALF_UP);
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
        userRepository.findByWorkEmail(rejectedByEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + rejectedByEmail));

        Employee manager = employeeRepository.findByEmail(rejectedByEmail).orElse(null);

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
        long finalized = appraisalRepository.findByStatus(AppraisalStatus.PENDING_FINANCE).size();
        double avgRating = appraisalRepository.findAll().stream()
                .filter(a -> a.getFinalRating() != null)
                .mapToDouble(Appraisal::getFinalRating)
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
                        "Based on the recent performance appraisal cycle, your annual compensation package has been updated as follows:\n"
                        +
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
                inc.getEffectiveDate());

        letter.setLetterBody(noticeHeader + "\n\n" + body);
        return letter;
    }

    @Transactional
    @CacheEvict(value = "appraisalDashboard", allEntries = true)
    public Optional<AppraisalResponse> updateAppraisal(Long id, AppraisalRequest request) {
        return appraisalRepository.findById(id).map(app -> {
            Employee emp = employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Employee not found with ID: " + request.getEmployeeId()));
            AppraisalCycle cycle = cycleRepository.findById(request.getCycleId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Appraisal cycle not found with ID: " + request.getCycleId()));

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

    // ── 6. TEAM APPRAISAL NEW METHODS ──────────────────────────────────────────

    public BigDecimal calculateRecommendedPercentage(Double rating) {
        if (rating == null)
            return BigDecimal.ZERO;
        if (rating >= 4.5)
            return BigDecimal.valueOf(15.0);
        if (rating >= 4.0)
            return BigDecimal.valueOf(10.0);
        if (rating >= 3.0)
            return BigDecimal.valueOf(5.0);
        if (rating >= 2.0)
            return BigDecimal.valueOf(2.0);
        return BigDecimal.ZERO;
    }

    public TeamAppraisalSummaryDto getTeamSummary(String managerEmail, Long cycleId) {
        User managerUser = userRepository.findByWorkEmail(managerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + managerEmail));
        Employee managerEmp = employeeRepository.findByEmail(managerEmail).orElse(null);

        String roleName = managerUser.getRole() != null ? managerUser.getRole().getName().toUpperCase() : "";
        boolean isAdminOrHr = "ADMIN".equals(roleName) || "HR".equals(roleName) || "SUPER_ADMIN".equals(roleName);

        List<Appraisal> appraisals = appraisalRepository.findAll().stream()
                .filter(a -> {
                    if (cycleId != null && !cycleId.equals(a.getCycle().getId())) {
                        return false;
                    }
                    if (!isAdminOrHr) {
                        if (managerEmp == null || a.getEmployee().getManager() == null) {
                            return false;
                        }
                        return managerEmp.getId().equals(a.getEmployee().getManager().getId());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        long totalCount = appraisals.size();
        long eligibleCount = appraisals.stream().filter(a -> a.getStatus() == AppraisalStatus.ELIGIBLE).count();
        long pendingFinanceCount = appraisals.stream().filter(a -> a.getStatus() == AppraisalStatus.PENDING_FINANCE)
                .count();
        long approvedFinanceCount = appraisals.stream().filter(a -> a.getStatus() == AppraisalStatus.FINANCE_APPROVED)
                .count();

        double avgAttendance = appraisals.stream()
                .mapToDouble(
                        a -> attendanceService.getAttendanceStats(a.getEmployee().getId()).getAttendancePercentage())
                .average()
                .orElse(96.4);

        double avgPerfScore = appraisals.stream()
                .mapToDouble(a -> a.getManagerRating() != null ? a.getManagerRating()
                        : (a.getSelfRating() != null ? a.getSelfRating() : 0.0))
                .filter(score -> score > 0.0)
                .average()
                .orElse(0.0);

        BigDecimal payrollImpact = BigDecimal.ZERO;
        for (Appraisal a : appraisals) {
            Increment inc = incrementRepository.findAll().stream()
                    .filter(i -> i.getAppraisal() != null && i.getAppraisal().getId().equals(a.getId()))
                    .findFirst().orElse(null);
            if (inc != null) {
                payrollImpact = payrollImpact.add(inc.getIncrementAmount());
            } else {
                BigDecimal currentSalary = a.getEmployee().getAnnualSalary() != null ? a.getEmployee().getAnnualSalary()
                        : BigDecimal.ZERO;
                BigDecimal percentage = calculateRecommendedPercentage(a.getManagerRating());
                BigDecimal amount = currentSalary.multiply(percentage).divide(BigDecimal.valueOf(100), 2,
                        RoundingMode.HALF_UP);
                payrollImpact = payrollImpact.add(amount);
            }
        }

        return new TeamAppraisalSummaryDto(
                eligibleCount,
                totalCount,
                pendingFinanceCount,
                approvedFinanceCount,
                Math.round(avgAttendance * 10.0) / 10.0,
                Math.round(avgPerfScore * 100.0) / 100.0,
                payrollImpact.setScale(2, RoundingMode.HALF_UP));
    }

    public PageResponse<TeamAppraisalListItemDto> getTeamAppraisals(
            String managerEmail, Long cycleId, String search, AppraisalStatus status, Pageable pageable) {

        User managerUser = userRepository.findByWorkEmail(managerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + managerEmail));
        Employee managerEmp = employeeRepository.findByEmail(managerEmail).orElse(null);

        String roleName = managerUser.getRole() != null ? managerUser.getRole().getName().toUpperCase() : "";
        boolean isAdminOrHrOrFinance = "ADMIN".equals(roleName) || "HR".equals(roleName) || "SUPER_ADMIN".equals(roleName) || "FINANCE".equals(roleName);

        Long queryManagerId = isAdminOrHrOrFinance ? null : (managerEmp != null ? managerEmp.getId() : -1L);

        List<Appraisal> appraisals = appraisalRepository.findTeamAppraisals(queryManagerId, cycleId, status);

        // 1. Filter by search
        List<Appraisal> filtered = appraisals.stream()
                .filter(a -> {
                    if (search != null && !search.trim().isEmpty()) {
                        String s = search.toLowerCase().trim();
                        return a.getEmployee().getFullName().toLowerCase().contains(s);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 2. Sort
        if (pageable.getSort().isSorted()) {
            Sort.Order order = pageable.getSort().iterator().next();
            boolean asc = order.isAscending();
            String prop = order.getProperty();
            filtered.sort((a, b) -> {
                int comp = 0;
                if ("employeeName".equals(prop) || "employee.fullName".equals(prop)) {
                    comp = a.getEmployee().getFullName().compareToIgnoreCase(b.getEmployee().getFullName());
                } else if ("status".equals(prop)) {
                    comp = a.getStatus().name().compareTo(b.getStatus().name());
                } else if ("attendance".equals(prop)) {
                    double pctA = attendanceService.getAttendanceStats(a.getEmployee().getId())
                            .getAttendancePercentage();
                    double pctB = attendanceService.getAttendanceStats(b.getEmployee().getId())
                            .getAttendancePercentage();
                    comp = Double.compare(pctA, pctB);
                }
                return asc ? comp : -comp;
            });
        }

        // 3. Paginate
        int totalElements = filtered.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), totalElements);
        List<Appraisal> subList = (start < totalElements) ? filtered.subList(start, end) : Collections.emptyList();

        List<TeamAppraisalListItemDto> list = subList.stream()
                .map(a -> {
                    TeamAppraisalListItemDto item = new TeamAppraisalListItemDto();
                    item.setAppraisalId(a.getId());
                    item.setEmployeeId(a.getEmployee().getId());
                    item.setEmployeeRefId(a.getEmployee().getEmployeeId());
                    item.setEmployeeName(a.getEmployee().getFullName());
                    item.setEmployeeDesignation(a.getEmployee().getDesignation());
                    item.setEmployeeProfileImage(a.getEmployee().getProfileImage());
                    item.setDepartment(a.getEmployee().getDepartment());

                    com.example.ems.attendance.dto.AttendanceStatsResponse attStats = attendanceService
                            .getAttendanceStats(a.getEmployee().getId());
                    item.setAttendance(attStats.getAttendancePercentage());
                    item.setLeaves(leaveRepository.findByEmployeeId(a.getEmployee().getId()).size());
                    item.setLate(attStats.getLateMarkCount());

                    // Dummy scores for visual aesthetics
                    item.setPerfScore(a.getManagerRating() != null ? a.getManagerRating()
                            : (a.getSelfRating() != null ? a.getSelfRating() : 0.0));
                    item.setKpiScore(85.0); // default mock kpi score
                    item.setManagerRating(a.getManagerRating());

                    item.setStatus(a.getStatus());
                    item.setCycleId(a.getCycle().getId());
                    item.setCycleName(a.getCycle().getName());
                    item.setAppraisalYear(a.getCycle().getStartDate().getYear());

                    // Manual review checks
                    boolean manualReviewRequired = attStats.getAttendancePercentage() < 85.0
                            && !a.isAttendanceJustified();
                    item.setManualReviewRequired(manualReviewRequired);
                    if (manualReviewRequired) {
                        item.setReviewReason(
                                "Attendance is below 85% (Actual: " + attStats.getAttendancePercentage() + "%)");
                    }

                    // Increment details
                    Increment inc = incrementRepository.findAll().stream()
                            .filter(i -> i.getAppraisal() != null && i.getAppraisal().getId().equals(a.getId()))
                            .findFirst().orElse(null);
                    BigDecimal currentSalary = a.getEmployee().getAnnualSalary() != null
                            ? a.getEmployee().getAnnualSalary()
                            : BigDecimal.ZERO;
                    item.setCurrentSalary(currentSalary);

                    if (inc != null) {
                        item.setIncrementId(inc.getId());
                        item.setIncrementPercentage(inc.getIncrementPercentage());
                        item.setRevisedSalary(inc.getNewSalary());
                    } else {
                        BigDecimal recommendedPct = calculateRecommendedPercentage(a.getManagerRating());
                        BigDecimal incrementAmt = currentSalary.multiply(recommendedPct).divide(BigDecimal.valueOf(100),
                                2, RoundingMode.HALF_UP);
                        item.setIncrementPercentage(recommendedPct);
                        item.setRevisedSalary(currentSalary.add(incrementAmt));
                    }

                    return item;
                })
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) totalElements / pageable.getPageSize());
        if (totalPages == 0)
            totalPages = 1;

        return new PageResponse<>(
                list,
                totalElements,
                totalPages,
                pageable.getPageNumber(),
                pageable.getPageSize());
    }

    public TeamAppraisalDetailDto getAppraisalDetail(String managerEmail, Long appraisalId) {
        Appraisal a = appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found with ID: " + appraisalId));

        User managerUser = userRepository.findByWorkEmail(managerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + managerEmail));
        Employee managerEmp = employeeRepository.findByEmail(managerEmail).orElse(null);

        String roleName = managerUser.getRole() != null ? managerUser.getRole().getName().toUpperCase() : "";
        boolean isAdminOrHrOrFinance = "ADMIN".equals(roleName) || "HR".equals(roleName) || "SUPER_ADMIN".equals(roleName) || "FINANCE".equals(roleName);

        if (!isAdminOrHrOrFinance) {
            if (managerEmp == null || a.getEmployee().getManager() == null
                    || !managerEmp.getId().equals(a.getEmployee().getManager().getId())) {
                throw new IllegalArgumentException("Access Denied: This employee does not report to you.");
            }
        }

        Employee emp = a.getEmployee();
        TeamAppraisalDetailDto detail = new TeamAppraisalDetailDto();
        detail.setAppraisalId(a.getId());
        detail.setEmployee(new TeamAppraisalDetailDto.EmployeeInfo(
                emp.getId(),
                emp.getEmployeeId(),
                emp.getFullName(),
                emp.getDesignation(),
                emp.getDepartment(),
                emp.getProfileImage()));

        com.example.ems.attendance.dto.AttendanceStatsResponse attStats = attendanceService
                .getAttendanceStats(emp.getId());
        detail.setAttendance(attStats.getAttendancePercentage());

        double perfScore = a.getManagerRating() != null ? a.getManagerRating()
                : (a.getSelfRating() != null ? a.getSelfRating() : 0.0);
        detail.setPerfScore(perfScore);
        detail.setManagerRating(a.getManagerRating());

        // Increment info
        Increment inc = incrementRepository.findAll().stream()
                .filter(i -> i.getAppraisal() != null && i.getAppraisal().getId().equals(a.getId()))
                .findFirst().orElse(null);
        BigDecimal currentSalary = emp.getAnnualSalary() != null ? emp.getAnnualSalary() : BigDecimal.ZERO;
        detail.setCurrentSalary(currentSalary);

        BigDecimal incrementPct;
        BigDecimal revisedSalary;
        BigDecimal annualImpact;

        if (inc != null) {
            incrementPct = inc.getIncrementPercentage();
            revisedSalary = inc.getNewSalary();
            annualImpact = inc.getIncrementAmount();
        } else {
            incrementPct = calculateRecommendedPercentage(a.getManagerRating());
            BigDecimal incrementAmt = currentSalary.multiply(incrementPct).divide(BigDecimal.valueOf(100), 2,
                    RoundingMode.HALF_UP);
            revisedSalary = currentSalary.add(incrementAmt);
            annualImpact = incrementAmt;
        }

        detail.setIncrementPercentage(incrementPct);
        detail.setRevisedSalary(revisedSalary);
        detail.setAnnualIncreaseImpact(annualImpact);

        // Goals count
        long totalGoals = goalRepository.countByEmployeeId(emp.getId());
        long completedGoals = goalRepository.countByEmployeeIdAndStatus(emp.getId(), "COMPLETED");
        if (totalGoals == 0) {
            totalGoals = 5;
            completedGoals = 4;
        }
        detail.setTotalGoals((int) totalGoals);
        detail.setGoalsCompleted((int) completedGoals);

        detail.setKpiScore(88.0);
        detail.setPeerRating(4.2);

        detail.setCycleId(a.getCycle().getId());
        detail.setCycleName(a.getCycle().getName());
        detail.setAppraisalYear(a.getCycle().getStartDate().getYear());
        detail.setStatus(a.getStatus());

        // Journey Steps
        List<TeamAppraisalDetailDto.JourneyStep> journey = new ArrayList<>();

        // 1. Self Review
        boolean selfDone = a.getSelfReviewSubmittedAt() != null;
        journey.add(new TeamAppraisalDetailDto.JourneyStep(
                "Self Review",
                selfDone ? "COMPLETED" : "PENDING",
                selfDone ? "Self-review submitted on " + a.getSelfReviewSubmittedAt() : "Self-review pending"));

        // 2. Manager Review
        boolean managerDone = a.getManagerReviewSubmittedAt() != null;
        journey.add(new TeamAppraisalDetailDto.JourneyStep(
                "Manager Review",
                managerDone ? "COMPLETED" : "PENDING",
                managerDone ? "Manager review submitted on " + a.getManagerReviewSubmittedAt()
                        : "Manager review pending"));

        // 3. Attendance Justification
        boolean attendanceOk = attStats.getAttendancePercentage() >= 85.0;
        String justificationStatus = "NOT_REQUIRED";
        String justificationDesc = "Attendance meets standard (>= 85%)";
        if (!attendanceOk) {
            if (a.isAttendanceJustified()) {
                justificationStatus = "COMPLETED";
                justificationDesc = "Attendance justified on " + a.getAttendanceJustifiedAt() + " with comments: "
                        + a.getAttendanceJustification();
            } else {
                justificationStatus = "PENDING";
                justificationDesc = "Attendance below 85% requires manager justification";
            }
        }
        journey.add(new TeamAppraisalDetailDto.JourneyStep(
                "Attendance Justification",
                justificationStatus,
                justificationDesc));

        // 4. Finance Approval
        String finStatus = "PENDING";
        String finDesc = "Pending Finance approval";
        if (a.getStatus() == AppraisalStatus.FINANCE_APPROVED) {
            finStatus = "APPROVED";
            finDesc = "Finance approved increment: " + (inc != null ? inc.getReason() : "");
        } else if (a.getStatus() == AppraisalStatus.FINANCE_REJECTED) {
            finStatus = "REJECTED";
            finDesc = "Finance rejected increment: " + (inc != null ? inc.getReason() : "");
        }
        journey.add(new TeamAppraisalDetailDto.JourneyStep(
                "Finance Approval",
                finStatus,
                finDesc));

        detail.setJourney(journey);
        return detail;
    }

    @Transactional
    public AppraisalResponse saveTeamRating(String managerEmail, Long appraisalId, TeamAppraisalRatingRequest request) {
        Appraisal a = appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found: " + appraisalId));

        User managerUser = userRepository.findByWorkEmail(managerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + managerEmail));
        Employee managerEmp = employeeRepository.findByEmail(managerEmail).orElse(null);

        String roleName = managerUser.getRole() != null ? managerUser.getRole().getName().toUpperCase() : "";
        boolean isAdminOrHr = "ADMIN".equals(roleName) || "HR".equals(roleName) || "SUPER_ADMIN".equals(roleName);

        if (!isAdminOrHr) {
            if (managerEmp == null || a.getEmployee().getManager() == null
                    || !managerEmp.getId().equals(a.getEmployee().getManager().getId())) {
                throw new IllegalArgumentException("Access Denied: This employee does not report to you.");
            }
        }

        a.setManagerRating(request.managerRating());
        a.setManagerReview(request.managerComments());
        a.setReviewer(managerEmp);
        a.setManagerReviewSubmittedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());

        // Check if attendance is < 85% to mark status MANUAL_REVIEW_REQUIRED
        com.example.ems.attendance.dto.AttendanceStatsResponse attStats = attendanceService
                .getAttendanceStats(a.getEmployee().getId());
        if (attStats.getAttendancePercentage() < 85.0 && !a.isAttendanceJustified()) {
            a.setStatus(AppraisalStatus.MANUAL_REVIEW_REQUIRED);
        } else {
            a.setStatus(AppraisalStatus.DRAFT);
        }

        return new AppraisalResponse(appraisalRepository.save(a));
    }

    @Transactional
    public AppraisalResponse justifyAttendance(String managerEmail, Long appraisalId,
            AttendanceJustificationRequest request) {
        Appraisal a = appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found: " + appraisalId));

        User managerUser = userRepository.findByWorkEmail(managerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + managerEmail));
        Employee managerEmp = employeeRepository.findByEmail(managerEmail).orElse(null);

        String roleName = managerUser.getRole() != null ? managerUser.getRole().getName().toUpperCase() : "";
        boolean isAdminOrHr = "ADMIN".equals(roleName) || "HR".equals(roleName) || "SUPER_ADMIN".equals(roleName);

        if (!isAdminOrHr) {
            if (managerEmp == null || a.getEmployee().getManager() == null
                    || !managerEmp.getId().equals(a.getEmployee().getManager().getId())) {
                throw new IllegalArgumentException("Access Denied: This employee does not report to you.");
            }
        }

        a.setAttendanceJustified(true);
        a.setAttendanceJustification(request.reason());
        a.setAttendanceJustifiedAt(LocalDateTime.now());
        a.setAttendanceJustifiedBy(managerEmp != null ? managerEmp.getId() : null);
        a.setStatus(AppraisalStatus.PENDING_FINANCE);
        a.setUpdatedAt(LocalDateTime.now());

        Appraisal saved = appraisalRepository.save(a);

        // Auto-create Increment record since we transitioned to PENDING_FINANCE
        createOrUpdateIncrementForAppraisal(saved);

        return new AppraisalResponse(saved);
    }

    @Transactional
    public AppraisalResponse submitToFinance(String managerEmail, Long appraisalId) {
        Appraisal a = appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found: " + appraisalId));

        User managerUser = userRepository.findByWorkEmail(managerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + managerEmail));
        Employee managerEmp = employeeRepository.findByEmail(managerEmail).orElse(null);

        String roleName = managerUser.getRole() != null ? managerUser.getRole().getName().toUpperCase() : "";
        boolean isAdminOrHr = "ADMIN".equals(roleName) || "HR".equals(roleName) || "SUPER_ADMIN".equals(roleName);

        if (!isAdminOrHr) {
            if (managerEmp == null || a.getEmployee().getManager() == null
                    || !managerEmp.getId().equals(a.getEmployee().getManager().getId())) {
                throw new IllegalArgumentException("Access Denied: This employee does not report to you.");
            }
        }

        com.example.ems.attendance.dto.AttendanceStatsResponse attStats = attendanceService
                .getAttendanceStats(a.getEmployee().getId());
        if (attStats.getAttendancePercentage() < 85.0 && !a.isAttendanceJustified()) {
            throw new IllegalArgumentException(
                    "Cannot submit to finance: Low attendance requires manager justification.");
        }

        a.setStatus(AppraisalStatus.PENDING_FINANCE);
        a.setUpdatedAt(LocalDateTime.now());

        Appraisal saved = appraisalRepository.save(a);

        // Auto-create Increment record
        createOrUpdateIncrementForAppraisal(saved);

        return new AppraisalResponse(saved);
    }

    private void createOrUpdateIncrementForAppraisal(Appraisal a) {
        Increment inc = incrementRepository.findAll().stream()
                .filter(i -> i.getAppraisal() != null && i.getAppraisal().getId().equals(a.getId()))
                .findFirst().orElseGet(Increment::new);

        BigDecimal currentSalary = a.getEmployee().getAnnualSalary() != null ? a.getEmployee().getAnnualSalary()
                : BigDecimal.ZERO;
        BigDecimal percentage = calculateRecommendedPercentage(a.getManagerRating());
        BigDecimal amount = currentSalary.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal newSalary = currentSalary.add(amount);

        inc.setEmployee(a.getEmployee());
        inc.setAppraisal(a);
        inc.setCurrentSalary(currentSalary);
        inc.setIncrementPercentage(percentage);
        inc.setIncrementAmount(amount);
        inc.setNewSalary(newSalary);
        inc.setEffectiveDate(LocalDate.now().plusMonths(1).withDayOfMonth(1)); // First day of next month
        inc.setStatus("PENDING");

        String auditNotes = "Performance Appraisal recommended increment";
        if (a.isAttendanceJustified()) {
            auditNotes += " (Attendance Justification: " + a.getAttendanceJustification() + ")";
        }
        inc.setReason(auditNotes);

        incrementRepository.save(inc);
    }

    @Transactional
    public AppraisalResponse approveFinanceIncrement(Long appraisalId, FinanceDecisionRequest request,
            String financeEmail) {
        Appraisal a = appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found: " + appraisalId));

        userRepository.findByWorkEmail(financeEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + financeEmail));
        Employee financeEmp = employeeRepository.findByEmail(financeEmail).orElse(null);

        a.setStatus(AppraisalStatus.FINANCE_APPROVED);
        a.setFinalRating(a.getManagerRating()); // finalized with manager rating
        a.setUpdatedAt(LocalDateTime.now());

        Appraisal saved = appraisalRepository.save(a);

        // Update increment status
        Increment inc = incrementRepository.findAll().stream()
                .filter(i -> i.getAppraisal() != null && i.getAppraisal().getId().equals(a.getId()))
                .findFirst().orElseGet(() -> {
                    // Create if not exists (fallback)
                    Increment i = new Increment();
                    i.setEmployee(saved.getEmployee());
                    i.setAppraisal(saved);
                    BigDecimal currentSalary = saved.getEmployee().getAnnualSalary() != null
                            ? saved.getEmployee().getAnnualSalary()
                            : BigDecimal.ZERO;
                    BigDecimal percentage = calculateRecommendedPercentage(saved.getManagerRating());
                    BigDecimal amount = currentSalary.multiply(percentage).divide(BigDecimal.valueOf(100), 2,
                            RoundingMode.HALF_UP);
                    i.setCurrentSalary(currentSalary);
                    i.setIncrementPercentage(percentage);
                    i.setIncrementAmount(amount);
                    i.setNewSalary(currentSalary.add(amount));
                    i.setEffectiveDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));
                    return i;
                });

        inc.setStatus("APPROVED");
        inc.setApprovedBy(financeEmp);
        inc.setApprovedAt(LocalDateTime.now());
        inc.setReason(request.comments()); // Set finance comments
        inc.setUpdatedAt(LocalDateTime.now());
        incrementRepository.save(inc);

        return new AppraisalResponse(saved);
    }

    @Transactional
    public AppraisalResponse rejectFinanceIncrement(Long appraisalId, FinanceDecisionRequest request,
            String financeEmail) {
        Appraisal a = appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found: " + appraisalId));

        userRepository.findByWorkEmail(financeEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + financeEmail));
        Employee financeEmp = employeeRepository.findByEmail(financeEmail).orElse(null);

        a.setStatus(AppraisalStatus.FINANCE_REJECTED);
        a.setUpdatedAt(LocalDateTime.now());

        Appraisal saved = appraisalRepository.save(a);

        // Update increment status
        Increment inc = incrementRepository.findAll().stream()
                .filter(i -> i.getAppraisal() != null && i.getAppraisal().getId().equals(a.getId()))
                .findFirst().orElseGet(() -> {
                    Increment i = new Increment();
                    i.setEmployee(saved.getEmployee());
                    i.setAppraisal(saved);
                    BigDecimal currentSalary = saved.getEmployee().getAnnualSalary() != null
                            ? saved.getEmployee().getAnnualSalary()
                            : BigDecimal.ZERO;
                    BigDecimal percentage = calculateRecommendedPercentage(saved.getManagerRating());
                    BigDecimal amount = currentSalary.multiply(percentage).divide(BigDecimal.valueOf(100), 2,
                            RoundingMode.HALF_UP);
                    i.setCurrentSalary(currentSalary);
                    i.setIncrementPercentage(percentage);
                    i.setIncrementAmount(amount);
                    i.setNewSalary(currentSalary.add(amount));
                    i.setEffectiveDate(LocalDate.now().plusMonths(1).withDayOfMonth(1));
                    return i;
                });

        inc.setStatus("REJECTED");
        inc.setApprovedBy(financeEmp);
        inc.setApprovedAt(LocalDateTime.now());
        inc.setReason(request.comments()); // Set finance comments
        inc.setUpdatedAt(LocalDateTime.now());
        incrementRepository.save(inc);

        return new AppraisalResponse(saved);
    }

    // ── E. Hardened Lifecycle, Timeline & Audit Methods ───────────────────────

    private void recordTimelineEvent(Appraisal appraisal, AppraisalStatus state, String description, Employee actionBy) {
        AppraisalTimelineEvent event = new AppraisalTimelineEvent();
        event.setAppraisal(appraisal);
        event.setState(state.name());
        event.setDescription(description);
        event.setActionBy(actionBy);
        event.setTimestamp(LocalDateTime.now());
        timelineEventRepository.save(event);
    }

    private void recordHistory(Appraisal appraisal, String fieldName, String oldValue, String newValue, Employee changedBy) {
        AppraisalHistory history = new AppraisalHistory();
        history.setAppraisal(appraisal);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());
        historyRepository.save(history);
    }

    private void checkCycleFreezeRules(Appraisal a, AppraisalStatus targetStatus) {
        if (a.getCycle() != null && "FROZEN".equalsIgnoreCase(a.getCycle().getStatus())) {
            AppraisalStatus currentStatus = a.getStatus();
            if (currentStatus == AppraisalStatus.DRAFT || currentStatus == AppraisalStatus.ELIGIBLE) {
                throw new BadRequestException("Appraisal Cycle is frozen. Modifications to DRAFT or ELIGIBLE appraisals are blocked.");
            }
            if (currentStatus == AppraisalStatus.SUBMITTED || currentStatus == AppraisalStatus.UNDER_REVIEW) {
                throw new BadRequestException("Appraisal Cycle is frozen. State changes are frozen/paused.");
            }
            if (currentStatus == AppraisalStatus.LOCKED || currentStatus == AppraisalStatus.FINANCE_PENDING) {
                if (a.isFinanceStageStarted()) {
                    throw new BadRequestException("Appraisal Cycle is frozen. Finance review stage has started; forward actions are blocked.");
                }
            }
        }
    }

    @Transactional
    public AppraisalResponse submitAppraisal(Long id, String email) {
        Appraisal a = appraisalRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + id));
        checkCycleFreezeRules(a, AppraisalStatus.SUBMITTED);
        
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        StateTransitionValidator.validate(a.getStatus(), AppraisalStatus.SUBMITTED);
        
        a.setStatus(AppraisalStatus.SUBMITTED);
        a.setSelfReviewSubmittedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        Appraisal saved = appraisalRepository.save(a);
        
        recordTimelineEvent(saved, AppraisalStatus.SUBMITTED, "Appraisal submitted by employee.", employee);
        return new AppraisalResponse(saved);
    }

    @Transactional
    public AppraisalResponse reopenAppraisal(Long id, String email) {
        Appraisal a = appraisalRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + id));
        Employee actionBy = employeeRepository.findByEmail(email).orElse(null);
        
        AppraisalStatus current = a.getStatus();
        if (current == AppraisalStatus.FINANCE_APPROVED || current == AppraisalStatus.PROCESSED || current == AppraisalStatus.CLOSED) {
            throw new BadRequestException("Cannot reopen appraisal in " + current + " state.");
        }
        
        StateTransitionValidator.validate(current, AppraisalStatus.DRAFT);
        a.setStatus(AppraisalStatus.DRAFT);
        a.setUpdatedAt(LocalDateTime.now());
        Appraisal saved = appraisalRepository.save(a);
        
        recordTimelineEvent(saved, AppraisalStatus.DRAFT, "Appraisal reopened.", actionBy);
        return new AppraisalResponse(saved);
    }

    @Transactional
    public AppraisalResponse requestRevision(Long id, String instructions, String email) {
        Appraisal a = appraisalRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + id));
        checkCycleFreezeRules(a, AppraisalStatus.DRAFT);
        Employee actionBy = employeeRepository.findByEmail(email).orElse(null);
        
        StateTransitionValidator.validate(a.getStatus(), AppraisalStatus.DRAFT);
        a.setStatus(AppraisalStatus.DRAFT);
        a.setUpdatedAt(LocalDateTime.now());
        Appraisal saved = appraisalRepository.save(a);
        
        recordTimelineEvent(saved, AppraisalStatus.DRAFT, "Revision requested: " + instructions, actionBy);
        return new AppraisalResponse(saved);
    }

    @Transactional
    public AppraisalResponse saveDraft(Long id, AppraisalSelfReviewRequest request, String email) {
        Appraisal a = appraisalRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + id));
        checkCycleFreezeRules(a, AppraisalStatus.DRAFT);
        Employee employee = employeeRepository.findByEmail(email).orElse(null);
        
        if (request.getSelfRating() != null) {
            recordHistory(a, "selfRating", String.valueOf(a.getSelfRating()), String.valueOf(request.getSelfRating()), employee);
            a.setSelfRating(request.getSelfRating());
        }
        if (request.getSelfReview() != null) {
            recordHistory(a, "selfReview", a.getSelfReview(), request.getSelfReview(), employee);
            a.setSelfReview(request.getSelfReview());
        }
        
        a.setStatus(AppraisalStatus.DRAFT);
        a.setUpdatedAt(LocalDateTime.now());
        Appraisal saved = appraisalRepository.save(a);
        
        recordTimelineEvent(saved, AppraisalStatus.DRAFT, "Appraisal draft updated.", employee);
        return new AppraisalResponse(saved);
    }

    @Transactional
    public AppraisalResponse lockAppraisal(Long id, String email) {
        Appraisal a = appraisalRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + id));
        checkCycleFreezeRules(a, AppraisalStatus.LOCKED);
        Employee actionBy = employeeRepository.findByEmail(email).orElse(null);
        
        StateTransitionValidator.validate(a.getStatus(), AppraisalStatus.LOCKED);
        a.setStatus(AppraisalStatus.LOCKED);
        a.setUpdatedAt(LocalDateTime.now());
        Appraisal saved = appraisalRepository.save(a);
        
        recordTimelineEvent(saved, AppraisalStatus.LOCKED, "Appraisal locked by HR.", actionBy);
        return new AppraisalResponse(saved);
    }

    public Map<String, Object> simulateIncrement(Long id) {
        Appraisal a = appraisalRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + id));
        BigDecimal currentSalary = a.getEmployee().getAnnualSalary() != null ? a.getEmployee().getAnnualSalary() : BigDecimal.ZERO;
        BigDecimal percentage = calculateRecommendedPercentage(a.getManagerRating() != null ? a.getManagerRating() : a.getSelfRating());
        BigDecimal amount = currentSalary.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal newSalary = currentSalary.add(amount);
        
        Map<String, Object> sim = new LinkedHashMap<>();
        sim.put("appraisalId", a.getId());
        sim.put("employeeName", a.getEmployee().getFullName());
        sim.put("currentSalary", currentSalary);
        sim.put("incrementPercentage", percentage);
        sim.put("incrementAmount", amount);
        sim.put("simulatedNewSalary", newSalary);
        sim.put("status", "SIMULATED");
        return sim;
    }

    @Transactional
    public AppraisalCycleResponse freezeCycle(Long cycleId, String email) {
        AppraisalCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new BadRequestException("Appraisal cycle not found: " + cycleId));
        cycle.setStatus("FROZEN");
        AppraisalCycle saved = cycleRepository.save(cycle);
        return new AppraisalCycleResponse(saved);
    }

    @Transactional
    public AppraisalCycleResponse reopenCycle(Long cycleId, String email) {
        AppraisalCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new BadRequestException("Appraisal cycle not found: " + cycleId));
        cycle.setStatus("ACTIVE");
        AppraisalCycle saved = cycleRepository.save(cycle);
        return new AppraisalCycleResponse(saved);
    }

    @Transactional
    public AppraisalCycleResponse closeCycle(Long cycleId, String email) {
        AppraisalCycle cycle = cycleRepository.findById(cycleId)
                .orElseThrow(() -> new BadRequestException("Appraisal cycle not found: " + cycleId));
        cycle.setStatus("CLOSED");
        AppraisalCycle saved = cycleRepository.save(cycle);
        
        List<Appraisal> appraisals = appraisalRepository.findAll().stream()
                .filter(a -> a.getCycle().getId().equals(cycleId))
                .collect(Collectors.toList());
        for (Appraisal a : appraisals) {
            if (a.getStatus() != AppraisalStatus.CLOSED) {
                StateTransitionValidator.validate(a.getStatus(), AppraisalStatus.CLOSED);
                a.setStatus(AppraisalStatus.CLOSED);
                a.setUpdatedAt(LocalDateTime.now());
                appraisalRepository.save(a);
            }
        }
        return new AppraisalCycleResponse(saved);
    }

    @Transactional
    public AppraisalResponse compensationFreeze(Long id, String email) {
        Appraisal a = appraisalRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + id));
        Employee actionBy = employeeRepository.findByEmail(email).orElse(null);
        a.setCompensationFrozen(true);
        Appraisal saved = appraisalRepository.save(a);
        recordTimelineEvent(saved, saved.getStatus(), "Compensation details frozen.", actionBy);
        return new AppraisalResponse(saved);
    }

    @Transactional
    public AppraisalResponse financeApproveHardened(Long appraisalId, FinanceDecisionRequest request, String financeEmail) {
        Appraisal a = appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + appraisalId));
        checkCycleFreezeRules(a, AppraisalStatus.FINANCE_APPROVED);
        Employee financeEmp = employeeRepository.findByEmail(financeEmail).orElse(null);
        
        StateTransitionValidator.validate(a.getStatus(), AppraisalStatus.FINANCE_APPROVED);
        a.setStatus(AppraisalStatus.FINANCE_APPROVED);
        a.setFinalRating(a.getManagerRating());
        a.setUpdatedAt(LocalDateTime.now());
        Appraisal saved = appraisalRepository.save(a);
        
        recordTimelineEvent(saved, AppraisalStatus.FINANCE_APPROVED, "Increment approved by finance.", financeEmp);
        
        createOrUpdateIncrementForAppraisal(saved);
        Increment inc = incrementRepository.findAll().stream()
                .filter(i -> i.getAppraisal() != null && i.getAppraisal().getId().equals(saved.getId()))
                .findFirst().orElseThrow();
        inc.setStatus("APPROVED");
        inc.setApprovedBy(financeEmp);
        inc.setApprovedAt(LocalDateTime.now());
        inc.setReason(request.comments());
        inc.setUpdatedAt(LocalDateTime.now());
        incrementRepository.save(inc);
        
        return new AppraisalResponse(saved);
    }

    @Transactional
    public AppraisalResponse financeRejectHardened(Long appraisalId, FinanceDecisionRequest request, String financeEmail) {
        Appraisal a = appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + appraisalId));
        checkCycleFreezeRules(a, AppraisalStatus.FINANCE_REJECTED);
        Employee financeEmp = employeeRepository.findByEmail(financeEmail).orElse(null);
        
        StateTransitionValidator.validate(a.getStatus(), AppraisalStatus.FINANCE_REJECTED);
        a.setStatus(AppraisalStatus.FINANCE_REJECTED);
        a.setUpdatedAt(LocalDateTime.now());
        Appraisal saved = appraisalRepository.save(a);
        
        recordTimelineEvent(saved, AppraisalStatus.FINANCE_REJECTED, "Increment rejected by finance.", financeEmp);
        
        createOrUpdateIncrementForAppraisal(saved);
        Increment inc = incrementRepository.findAll().stream()
                .filter(i -> i.getAppraisal() != null && i.getAppraisal().getId().equals(saved.getId()))
                .findFirst().orElseThrow();
        inc.setStatus("REJECTED");
        inc.setApprovedBy(financeEmp);
        inc.setApprovedAt(LocalDateTime.now());
        inc.setReason(request.comments());
        inc.setUpdatedAt(LocalDateTime.now());
        incrementRepository.save(inc);
        
        return new AppraisalResponse(saved);
    }

    @Transactional
    public AppraisalResponse financeSendBack(Long appraisalId, String financeEmail) {
        Appraisal a = appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + appraisalId));
        checkCycleFreezeRules(a, AppraisalStatus.UNDER_REVIEW);
        Employee financeEmp = employeeRepository.findByEmail(financeEmail).orElse(null);
        
        StateTransitionValidator.validate(a.getStatus(), AppraisalStatus.UNDER_REVIEW);
        a.setStatus(AppraisalStatus.UNDER_REVIEW);
        a.setUpdatedAt(LocalDateTime.now());
        Appraisal saved = appraisalRepository.save(a);
        
        recordTimelineEvent(saved, AppraisalStatus.UNDER_REVIEW, "Send back requested by finance.", financeEmp);
        return new AppraisalResponse(saved);
    }

    @Transactional
    public Map<String, Object> bulkApprove(List<Long> ids, String email) {
        Employee manager = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Employee not found for email: " + email));
        
        List<Long> successIds = new ArrayList<>();
        List<Map<String, Object>> failedList = new ArrayList<>();
        
        BulkAppraisalActionLog log = new BulkAppraisalActionLog();
        log.setOperationType("APPROVE");
        log.setExecutedBy(manager);
        log.setTimestamp(LocalDateTime.now());
        BulkAppraisalActionLog savedLog = bulkActionLogRepository.save(log);
        
        for (Long id : ids) {
            BulkAppraisalItemResult itemResult = new BulkAppraisalItemResult();
            itemResult.setActionLog(savedLog);
            itemResult.setAppraisalId(id);
            try {
                Appraisal a = appraisalRepository.findById(id)
                        .orElseThrow(() -> new BadRequestException("Appraisal not found."));
                
                checkCycleFreezeRules(a, AppraisalStatus.MANUAL_REVIEW_REQUIRED);
                
                if (a.getEmployee().getManager() == null || !a.getEmployee().getManager().getId().equals(manager.getId())) {
                    throw new BadRequestException("Employee does not report to you.");
                }
                
                a.setStatus(AppraisalStatus.MANUAL_REVIEW_REQUIRED);
                a.setUpdatedAt(LocalDateTime.now());
                Appraisal savedAppraisal = appraisalRepository.save(a);
                
                recordTimelineEvent(savedAppraisal, AppraisalStatus.MANUAL_REVIEW_REQUIRED, "Bulk approved by manager.", manager);
                successIds.add(id);
                itemResult.setStatus("SUCCESS");
                savedLog.getResults().add(itemResult);
            } catch (Exception e) {
                Map<String, Object> failure = new LinkedHashMap<>();
                failure.put("id", id);
                failure.put("reason", e.getMessage());
                failedList.add(failure);
                
                itemResult.setStatus("FAILED");
                itemResult.setReason(e.getMessage());
                savedLog.getResults().add(itemResult);
            }
        }
        bulkActionLogRepository.save(savedLog);
        
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", successIds);
        resp.put("failed", failedList);
        return resp;
    }

    @Transactional
    public Map<String, Object> bulkReject(List<Long> ids, String email) {
        Employee manager = employeeRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Employee not found for email: " + email));
        
        List<Long> successIds = new ArrayList<>();
        List<Map<String, Object>> failedList = new ArrayList<>();
        
        BulkAppraisalActionLog log = new BulkAppraisalActionLog();
        log.setOperationType("REJECT");
        log.setExecutedBy(manager);
        log.setTimestamp(LocalDateTime.now());
        BulkAppraisalActionLog savedLog = bulkActionLogRepository.save(log);
        
        for (Long id : ids) {
            BulkAppraisalItemResult itemResult = new BulkAppraisalItemResult();
            itemResult.setActionLog(savedLog);
            itemResult.setAppraisalId(id);
            try {
                Appraisal a = appraisalRepository.findById(id)
                        .orElseThrow(() -> new BadRequestException("Appraisal not found."));
                
                checkCycleFreezeRules(a, AppraisalStatus.DRAFT);
                
                if (a.getEmployee().getManager() == null || !a.getEmployee().getManager().getId().equals(manager.getId())) {
                    throw new BadRequestException("Employee does not report to you.");
                }
                
                a.setStatus(AppraisalStatus.DRAFT);
                a.setUpdatedAt(LocalDateTime.now());
                Appraisal savedAppraisal = appraisalRepository.save(a);
                
                recordTimelineEvent(savedAppraisal, AppraisalStatus.DRAFT, "Bulk rejected by manager.", manager);
                successIds.add(id);
                itemResult.setStatus("SUCCESS");
                savedLog.getResults().add(itemResult);
            } catch (Exception e) {
                Map<String, Object> failure = new LinkedHashMap<>();
                failure.put("id", id);
                failure.put("reason", e.getMessage());
                failedList.add(failure);
                
                itemResult.setStatus("FAILED");
                itemResult.setReason(e.getMessage());
                savedLog.getResults().add(itemResult);
            }
        }
        bulkActionLogRepository.save(savedLog);
        
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", successIds);
        resp.put("failed", failedList);
        return resp;
    }

    public List<Map<String, Object>> getTimeline(Long id) {
        if (!appraisalRepository.existsById(id)) {
            throw new BadRequestException("Appraisal not found: " + id);
        }
        List<AppraisalTimelineEvent> events = timelineEventRepository.findByAppraisalIdOrderByTimestampAsc(id);
        return events.stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("state", e.getState());
            m.put("description", e.getDescription());
            m.put("actionBy", e.getActionBy() != null ? e.getActionBy().getFullName() : "System");
            m.put("timestamp", e.getTimestamp().toString());
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getComments(Long id) {
        if (!appraisalRepository.existsById(id)) {
            throw new BadRequestException("Appraisal not found: " + id);
        }
        List<AppraisalComment> comments = commentRepository.findByAppraisalIdOrderByCreatedAtAsc(id);
        return comments.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("comment", c.getComment());
            m.put("authorName", c.getUser() != null ? c.getUser().getWorkEmail() : "Unknown");
            m.put("createdAt", c.getCreatedAt().toString());
            return m;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> addComment(Long id, String commentText, String email) {
        Appraisal a = appraisalRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + id));
        User user = userRepository.findByWorkEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found: " + email));
        
        AppraisalComment comment = new AppraisalComment();
        comment.setAppraisal(a);
        comment.setUser(user);
        comment.setComment(commentText);
        comment.setCreatedAt(LocalDateTime.now());
        AppraisalComment saved = commentRepository.save(comment);
        
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", saved.getId());
        m.put("comment", saved.getComment());
        m.put("authorName", user.getWorkEmail());
        m.put("createdAt", saved.getCreatedAt().toString());
        return m;
    }

    public Map<String, Long> getRatingDistribution() {
        List<Appraisal> appraisals = appraisalRepository.findAll();
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Appraisal a : appraisals) {
            Double rating = a.getFinalRating() != null ? a.getFinalRating() : a.getManagerRating();
            if (rating != null) {
                String key = String.valueOf(rating);
                distribution.put(key, distribution.getOrDefault(key, 0L) + 1);
            }
        }
        return distribution;
    }

    public List<Map<String, Object>> getDepartmentSummary() {
        List<Appraisal> appraisals = appraisalRepository.findAll();
        Map<String, List<Appraisal>> grouped = appraisals.stream()
                .filter(a -> a.getEmployee().getDepartment() != null)
                .collect(Collectors.groupingBy(a -> a.getEmployee().getDepartment()));
        
        List<Map<String, Object>> summary = new ArrayList<>();
        grouped.forEach((dept, list) -> {
            long total = list.size();
            long processed = list.stream().filter(a -> a.getStatus() == AppraisalStatus.PROCESSED).count();
            double avgRating = list.stream()
                    .mapToDouble(a -> a.getFinalRating() != null ? a.getFinalRating() : (a.getManagerRating() != null ? a.getManagerRating() : 0.0))
                    .average().orElse(0.0);
            
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("department", dept);
            map.put("totalAppraisals", total);
            map.put("completedAppraisals", processed);
            map.put("averageRating", Math.round(avgRating * 100.0) / 100.0);
            summary.add(map);
        });
        return summary;
    }

    public Map<String, Object> getIncrementGap() {
        List<Increment> increments = incrementRepository.findAll();
        BigDecimal totalRecommended = BigDecimal.ZERO;
        BigDecimal totalApproved = BigDecimal.ZERO;
        
        for (Increment inc : increments) {
            BigDecimal currentSalary = inc.getCurrentSalary() != null ? inc.getCurrentSalary() : BigDecimal.ZERO;
            BigDecimal hrRecPercentage = calculateRecommendedPercentage(inc.getAppraisal() != null && inc.getAppraisal().getManagerRating() != null ? inc.getAppraisal().getManagerRating() : 3.0);
            BigDecimal hrRecAmount = currentSalary.multiply(hrRecPercentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            totalRecommended = totalRecommended.add(hrRecAmount);
            
            if ("APPROVED".equalsIgnoreCase(inc.getStatus())) {
                totalApproved = totalApproved.add(inc.getIncrementAmount());
            }
        }
        
        Map<String, Object> gap = new LinkedHashMap<>();
        gap.put("totalHRRecommendedAmount", totalRecommended);
        gap.put("totalFinanceApprovedAmount", totalApproved);
        gap.put("gapAmount", totalRecommended.subtract(totalApproved));
        return gap;
    }

    public Map<String, Object> getCycleProgress(Long cycleId) {
        List<Appraisal> list = appraisalRepository.findAll().stream()
                .filter(a -> a.getCycle().getId().equals(cycleId))
                .collect(Collectors.toList());
        long total = list.size();
        long completed = list.stream().filter(a -> a.getStatus() == AppraisalStatus.PROCESSED || a.getStatus() == AppraisalStatus.CLOSED).count();
        long pending = total - completed;
        
        Map<String, Object> progress = new LinkedHashMap<>();
        progress.put("cycleId", cycleId);
        progress.put("totalAppraisals", total);
        progress.put("completedCount", completed);
        progress.put("pendingCount", pending);
        progress.put("completenessPercentage", total > 0 ? (double) completed / total * 100.0 : 100.0);
        return progress;
    }

    public List<Map<String, Object>> getAuditLogs(Long id) {
        return getTimeline(id);
    }

    public List<Map<String, Object>> getHistory(Long id) {
        List<AppraisalHistory> history = historyRepository.findByAppraisalIdOrderByChangedAtAsc(id);
        return history.stream().map(h -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("fieldName", h.getFieldName());
            m.put("oldValue", h.getOldValue());
            m.put("newValue", h.getNewValue());
            m.put("changedBy", h.getChangedBy() != null ? h.getChangedBy().getFullName() : "System");
            m.put("changedAt", h.getChangedAt().toString());
            return m;
        }).collect(Collectors.toList());
    }

    @Transactional
    public SalaryRevision executePayrollDecoupled(Long appraisalId, String email) {
        Appraisal a = appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + appraisalId));
        
        if (a.getStatus() == AppraisalStatus.PROCESSED || a.getStatus() == AppraisalStatus.CLOSED) {
            throw new BadRequestException("Duplicate execution: Appraisal is already processed or closed.");
        }
        
        if (a.getStatus() != AppraisalStatus.FINANCE_APPROVED) {
            throw new BadRequestException("Appraisal status must be FINANCE_APPROVED to execute payroll.");
        }
        
        Employee actionBy = employeeRepository.findByEmail(email).orElse(null);
        StateTransitionValidator.validate(a.getStatus(), AppraisalStatus.PROCESSED);
        
        Increment inc = incrementRepository.findAll().stream()
                .filter(i -> i.getAppraisal() != null && i.getAppraisal().getId().equals(a.getId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Approved increment not found for appraisal: " + appraisalId));
        
        if (!"APPROVED".equalsIgnoreCase(inc.getStatus())) {
            throw new BadRequestException("Increment status must be APPROVED by Finance.");
        }
        
        boolean exists = revisionRepository.findAll().stream()
                .anyMatch(r -> r.getEmployee().getId().equals(a.getEmployee().getId()) 
                        && r.getPreviousSalary().compareTo(inc.getCurrentSalary()) == 0 
                        && r.getNewSalary().compareTo(inc.getNewSalary()) == 0
                        && r.getEffectiveDate().equals(inc.getEffectiveDate()));
        if (exists) {
            throw new BadRequestException("Duplicate SalaryRevision already exists.");
        }
        
        SalaryRevision rev = new SalaryRevision();
        rev.setEmployee(a.getEmployee());
        rev.setPreviousSalary(inc.getCurrentSalary());
        rev.setNewSalary(inc.getNewSalary());
        rev.setChangePercentage(inc.getIncrementPercentage());
        rev.setEffectiveDate(inc.getEffectiveDate());
        rev.setReason(inc.getReason());
        rev.setCreatedAt(LocalDateTime.now());
        SalaryRevision savedRev = revisionRepository.save(rev);
        
        Employee employee = a.getEmployee();
        employee.setAnnualSalary(inc.getNewSalary());
        employeeRepository.save(employee);
        
        a.setStatus(AppraisalStatus.PROCESSED);
        a.setUpdatedAt(LocalDateTime.now());
        appraisalRepository.save(a);
        
        inc.setStatus("APPLIED");
        inc.setAppliedAt(LocalDateTime.now());
        inc.setUpdatedAt(LocalDateTime.now());
        incrementRepository.save(inc);
        
        recordTimelineEvent(a, AppraisalStatus.PROCESSED, "Payroll revision executed. New salary applied.", actionBy);
        return savedRev;
    }

    @Transactional
    public SalaryRevision retryPayroll(Long appraisalId, String email) {
        Appraisal a = appraisalRepository.findById(appraisalId)
                .orElseThrow(() -> new BadRequestException("Appraisal not found: " + appraisalId));
        
        if (a.getStatus() == AppraisalStatus.PROCESSED) {
            return revisionRepository.findAll().stream()
                    .filter(r -> r.getEmployee().getId().equals(a.getEmployee().getId()))
                    .reduce((first, second) -> second)
                    .orElseThrow(() -> new BadRequestException("No SalaryRevision found to retry/retrieve."));
        }
        
        return executePayrollDecoupled(appraisalId, email);
    }
}
