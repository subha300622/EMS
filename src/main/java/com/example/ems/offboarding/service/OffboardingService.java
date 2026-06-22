package com.example.ems.offboarding.service;

import com.example.ems.asset.dto.AssetReturnRequest;
import com.example.ems.auth.entity.User;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.employee.entity.Employee;
import com.example.ems.employee.repository.EmployeeRepository;
import com.example.ems.offboarding.dto.ExitInterviewFeedbackRequest;
import com.example.ems.offboarding.dto.ExitInterviewRequest;
import com.example.ems.offboarding.dto.HandoverRequest;
import com.example.ems.offboarding.dto.OffboardingDashboardResponse;
import com.example.ems.offboarding.dto.OffboardingRequest;
import com.example.ems.offboarding.dto.OffboardingResponse;
import com.example.ems.offboarding.dto.OffboardingTaskResponse;
import com.example.ems.offboarding.dto.SettlementRequest;
import com.example.ems.offboarding.entity.ExitInterview;
import com.example.ems.offboarding.entity.Offboarding;
import com.example.ems.offboarding.entity.OffboardingAssetReturn;
import com.example.ems.offboarding.entity.OffboardingHandover;
import com.example.ems.offboarding.entity.OffboardingSettlement;
import com.example.ems.offboarding.entity.OffboardingTask;
import com.example.ems.offboarding.repository.ExitInterviewRepository;
import com.example.ems.offboarding.repository.OffboardingAssetReturnRepository;
import com.example.ems.offboarding.repository.OffboardingHandoverRepository;
import com.example.ems.offboarding.repository.OffboardingRepository;
import com.example.ems.offboarding.repository.OffboardingSettlementRepository;
import com.example.ems.offboarding.repository.OffboardingTaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OffboardingService {

    @Autowired
    private OffboardingRepository offboardingRepository;

    @Autowired
    private OffboardingTaskRepository offboardingTaskRepository;

    @Autowired
    private OffboardingAssetReturnRepository offboardingAssetReturnRepository;

    @Autowired
    private OffboardingSettlementRepository offboardingSettlementRepository;

    @Autowired
    private OffboardingHandoverRepository offboardingHandoverRepository;

    @Autowired
    private ExitInterviewRepository exitInterviewRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    private OffboardingResponse buildResponse(Offboarding offboarding) {
        List<OffboardingTask> tasks = offboardingTaskRepository.findByOffboardingId(offboarding.getId());
        List<OffboardingAssetReturn> assetReturns = offboardingAssetReturnRepository
                .findByOffboardingId(offboarding.getId());
        List<OffboardingSettlement> settlements = offboardingSettlementRepository
                .findByOffboardingId(offboarding.getId());
        List<OffboardingHandover> handovers = offboardingHandoverRepository.findByOffboardingId(offboarding.getId());
        List<ExitInterview> exitInterviews = exitInterviewRepository.findByOffboardingId(offboarding.getId());
        return new OffboardingResponse(offboarding, tasks, assetReturns, settlements, handovers, exitInterviews);
    }

    // ── 1. DASHBOARD STATS ──────────────────────────────────────────────────
    @Cacheable(value = "offboardingDashboard", key = "'stats'")
    public OffboardingDashboardResponse getDashboardStats() {
        OffboardingDashboardResponse stats = new OffboardingDashboardResponse();

        long total = offboardingRepository.count();
        long pending = offboardingRepository.findByStatus("PENDING").size();
        long inProgress = offboardingRepository.findByStatus("IN_PROGRESS").size();
        long completed = offboardingRepository.findByStatus("COMPLETED").size();
        long approved = offboardingRepository.findByStatus("APPROVED").size();
        long rejected = offboardingRepository.findByStatus("REJECTED").size();

        long totalTasks = offboardingTaskRepository.count();
        long completedTasks = offboardingTaskRepository.findAll().stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()))
                .count();

        double taskRate = totalTasks > 0 ? ((double) completedTasks / totalTasks) * 100.0 : 0.0;

        stats.setTotalOnboardings(total);
        stats.setPendingOffboardings(pending);
        stats.setInProgressOffboardings(inProgress);
        stats.setCompletedOffboardings(completed);
        stats.setApprovedOffboardings(approved);
        stats.setRejectedOffboardings(rejected);
        stats.setTotalTasksAssigned(totalTasks);
        stats.setCompletedTasksCount(completedTasks);
        stats.setTaskCompletionRate(Math.round(taskRate * 100.0) / 100.0);

        return stats;
    }

    // ── 2. CREATE OFFBOARDING ────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "offboardingDashboard", allEntries = true)
    public OffboardingResponse createOffboarding(OffboardingRequest request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Employee not found with ID: " + request.getEmployeeId()));

        Optional<Offboarding> existing = offboardingRepository.findByEmployeeId(request.getEmployeeId());
        if (existing.isPresent()) {
            throw new IllegalStateException(
                    "Offboarding process already active for employee: " + employee.getFullName());
        }

        Offboarding offboarding = new Offboarding();
        offboarding.setEmployee(employee);
        offboarding.setStatus("PENDING");
        offboarding.setReason(request.getReason());
        offboarding.setExitDate(request.getExitDate() != null ? request.getExitDate() : LocalDate.now().plusDays(30));

        Offboarding saved = offboardingRepository.save(offboarding);

        // Populate Default Tasks
        createDefaultTasks(saved);

        return buildResponse(saved);
    }

    private void createDefaultTasks(Offboarding offboarding) {
        String[] titles = {
                "Return Corporate Laptop & Badges",
                "Assign Knowledge Handover",
                "Schedule Exit Interview meeting",
                "Generate Final Settlement Statement"
        };
        String[] descriptions = {
                "Hardware return check: Company laptop, charger, screen, and ID cards.",
                "Schedule a session with your supervisor to handover critical tasks and documentation.",
                "A standard conversation with HR before leaving regarding feedback.",
                "Finance check for pending payments, gratuity, and severance calculations."
        };

        for (int i = 0; i < titles.length; i++) {
            OffboardingTask task = new OffboardingTask();
            task.setOffboarding(offboarding);
            task.setTitle(titles[i]);
            task.setDescription(descriptions[i]);
            task.setStatus("PENDING");
            task.setDueDate(offboarding.getExitDate());
            offboardingTaskRepository.save(task);
        }
    }

    public List<OffboardingResponse> getOffboardings() {
        return offboardingRepository.findAll().stream()
                .map(this::buildResponse)
                .collect(Collectors.toList());
    }

    public Optional<OffboardingResponse> getOffboardingById(Long id) {
        return offboardingRepository.findById(id).map(this::buildResponse);
    }

    public Optional<OffboardingResponse> getOffboardingByEmployeeEmail(String email) {
        return offboardingRepository.findByEmployeeEmail(email).map(this::buildResponse);
    }

    // ── 3. STATE UPDATES ─────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "offboardingDashboard", allEntries = true)
    public Optional<OffboardingResponse> approveOffboarding(Long id) {
        return offboardingRepository.findById(id).map(offboarding -> {
            offboarding.setStatus("APPROVED");
            offboarding.setUpdatedAt(LocalDateTime.now());
            return buildResponse(offboardingRepository.save(offboarding));
        });
    }

    @Transactional
    @CacheEvict(value = "offboardingDashboard", allEntries = true)
    public Optional<OffboardingResponse> rejectOffboarding(Long id) {
        return offboardingRepository.findById(id).map(offboarding -> {
            offboarding.setStatus("REJECTED");
            offboarding.setUpdatedAt(LocalDateTime.now());
            return buildResponse(offboardingRepository.save(offboarding));
        });
    }

    @Transactional
    @CacheEvict(value = "offboardingDashboard", allEntries = true)
    public Optional<OffboardingResponse> completeOffboarding(Long id) {
        return offboardingRepository.findById(id).map(offboarding -> {
            offboarding.setStatus("COMPLETED");
            offboarding.setUpdatedAt(LocalDateTime.now());
            return buildResponse(offboardingRepository.save(offboarding));
        });
    }

    // ── 4. TASKS ────────────────────────────────────────────────────────────
    public List<OffboardingTaskResponse> getTasks(Long offboardingId) {
        return offboardingTaskRepository.findByOffboardingId(offboardingId).stream()
                .map(OffboardingTaskResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "offboardingDashboard", allEntries = true)
    public Optional<OffboardingTaskResponse> updateTaskStatus(Long taskId, String status) {
        return offboardingTaskRepository.findById(taskId).map(task -> {
            task.setStatus(status.toUpperCase());
            if ("COMPLETED".equalsIgnoreCase(status)) {
                task.setCompletedAt(LocalDateTime.now());
            } else {
                task.setCompletedAt(null);
            }

            // Auto transition Offboarding state if modified
            Offboarding ob = task.getOffboarding();
            if ("PENDING".equalsIgnoreCase(ob.getStatus())) {
                ob.setStatus("IN_PROGRESS");
                offboardingRepository.save(ob);
            }

            return new OffboardingTaskResponse(offboardingTaskRepository.save(task));
        });
    }

    // ── 5. ASSET RETURN ──────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "offboardingDashboard", allEntries = true)
    public OffboardingResponse recordAssetReturn(AssetReturnRequest request) {
        Offboarding offboarding = offboardingRepository.findById(request.getOffboardingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Offboarding record not found with ID: " + request.getOffboardingId()));

        OffboardingAssetReturn ret = new OffboardingAssetReturn();
        ret.setOffboarding(offboarding);
        ret.setAssetName(request.getAssetName());
        ret.setSerialNumber(request.getSerialNumber());
        ret.setReturnStatus(request.getReturnStatus().toUpperCase());
        ret.setReturnedAt(LocalDateTime.now());

        offboardingAssetReturnRepository.save(ret);

        offboarding.setStatus("IN_PROGRESS");
        offboarding.setUpdatedAt(LocalDateTime.now());
        offboardingRepository.save(offboarding);

        return buildResponse(offboarding);
    }

    // ── 6. SETTLEMENT ────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "offboardingDashboard", allEntries = true)
    public OffboardingResponse processSettlement(SettlementRequest request) {
        Offboarding offboarding = offboardingRepository.findById(request.getOffboardingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Offboarding record not found with ID: " + request.getOffboardingId()));

        BigDecimal total = request.getGratuity()
                .add(request.getSeverance())
                .add(request.getPendingSalary())
                .subtract(request.getDeductions());

        OffboardingSettlement s = new OffboardingSettlement();
        s.setOffboarding(offboarding);
        s.setGratuity(request.getGratuity());
        s.setSeverance(request.getSeverance());
        s.setPendingSalary(request.getPendingSalary());
        s.setDeductions(request.getDeductions());
        s.setTotalSettlementAmount(total);
        s.setPaymentStatus("PAID");
        s.setProcessedAt(LocalDateTime.now());

        offboardingSettlementRepository.save(s);

        offboarding.setStatus("IN_PROGRESS");
        offboarding.setUpdatedAt(LocalDateTime.now());
        offboardingRepository.save(offboarding);

        return buildResponse(offboarding);
    }

    // ── 7. HANDOVER ──────────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "offboardingDashboard", allEntries = true)
    public OffboardingResponse recordHandover(HandoverRequest request) {
        Offboarding offboarding = offboardingRepository.findById(request.getOffboardingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Offboarding record not found with ID: " + request.getOffboardingId()));

        Employee recipient = employeeRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Recipient Employee not found with ID: " + request.getRecipientId()));

        OffboardingHandover h = new OffboardingHandover();
        h.setOffboarding(offboarding);
        h.setTaskName(request.getTaskName());
        h.setRecipientEmployee(recipient);
        h.setStatus("PENDING");

        offboardingHandoverRepository.save(h);

        offboarding.setStatus("IN_PROGRESS");
        offboarding.setUpdatedAt(LocalDateTime.now());
        offboardingRepository.save(offboarding);

        return buildResponse(offboarding);
    }

    // ── 8. EXIT INTERVIEW ────────────────────────────────────────────────────
    @Transactional
    @CacheEvict(value = "offboardingDashboard", allEntries = true)
    public OffboardingResponse scheduleExitInterview(ExitInterviewRequest request) {
        Offboarding offboarding = offboardingRepository.findById(request.getOffboardingId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Offboarding record not found with ID: " + request.getOffboardingId()));

        ExitInterview exit = new ExitInterview();
        exit.setOffboarding(offboarding);
        exit.setInterviewDate(request.getInterviewDate());
        exit.setInterviewerName(request.getInterviewerName());
        exit.setStatus("SCHEDULED");

        exitInterviewRepository.save(exit);

        offboarding.setStatus("IN_PROGRESS");
        offboarding.setUpdatedAt(LocalDateTime.now());
        offboardingRepository.save(offboarding);

        return buildResponse(offboarding);
    }

    @Transactional
    @CacheEvict(value = "offboardingDashboard", allEntries = true)
    public Optional<OffboardingResponse> addExitFeedback(Long interviewId, ExitInterviewFeedbackRequest request) {
        return exitInterviewRepository.findById(interviewId).map(exit -> {
            exit.setFeedback(request.getFeedback());
            exit.setReasonsForLeaving(request.getReasonsForLeaving());
            exit.setRating(request.getRating());
            exit.setStatus("COMPLETED");

            exitInterviewRepository.save(exit);
            return buildResponse(exit.getOffboarding());
        });
    }

    // ── 9. REVOKE ACCESS & DEACTIVATE PROFILE ────────────────────────────────
    @Transactional
    @CacheEvict(value = "offboardingDashboard", allEntries = true)
    public Map<String, Object> revokeAccess(Long id) {
        Offboarding offboarding = offboardingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Offboarding record not found with ID: " + id));

        // Revoke Employee Profile Status
        Employee emp = offboarding.getEmployee();
        emp.setStatus("INACTIVE");
        employeeRepository.save(emp);

        // Revoke User Account Status
        Optional<User> optUser = userRepository.findByWorkEmail(emp.getEmail());
        if (optUser.isPresent()) {
            User user = optUser.get();
            user.setStatus("INACTIVE");
            userRepository.save(user);
        }

        offboarding.setStatus("IN_PROGRESS");
        offboarding.setUpdatedAt(LocalDateTime.now());
        offboardingRepository.save(offboarding);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("offboardingId", id);
        result.put("employeeName", emp.getFullName());
        result.put("revocationDate", LocalDateTime.now());
        result.put("slackStatus", "DEACTIVATED");
        result.put("githubAccess", "REVOKED");
        result.put("corporateEmail", "BLOCKED");
        result.put("systemAccess", "DISABLED");
        result.put("employeeProfileStatus", "INACTIVE");
        result.put("status", "REVOKED");

        return result;
    }

    // ── 10. REPORTS & ANALYTICS ──────────────────────────────────────────────
    public Map<String, Object> getReportData(String reportType) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reportType", reportType);
        data.put("generatedAt", LocalDateTime.now());

        long total = offboardingRepository.count();
        long approved = offboardingRepository.findByStatus("APPROVED").size();
        long completed = offboardingRepository.findByStatus("COMPLETED").size();

        data.put("totalOffboardingsInitiated", total);
        data.put("completedExits", completed);
        data.put("approvedExits", approved);
        data.put("pendingClearance", offboardingRepository.findByStatus("PENDING").size());

        return data;
    }

    public Map<String, Object> getAnalyticsData() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("compiledAt", LocalDateTime.now());

        List<ExitInterview> interviews = exitInterviewRepository.findAll().stream()
                .filter(e -> "COMPLETED".equalsIgnoreCase(e.getStatus()))
                .collect(Collectors.toList());

        double avgRating = interviews.stream()
                .mapToInt(ExitInterview::getRating)
                .average()
                .orElse(0.0);

        List<String> commonReasons = interviews.stream()
                .map(ExitInterview::getReasonsForLeaving)
                .filter(r -> r != null && !r.isBlank())
                .collect(Collectors.toList());

        data.put("exitSatisfactoryIndex", Math.round(avgRating * 10.0) / 10.0);
        data.put("totalCompletedExitInterviews", interviews.size());
        data.put("exitReasonsCollected", commonReasons);

        return data;
    }

    public Map<String, Object> getClearanceStatus(Long offboardingId) {
        Offboarding offboarding = offboardingRepository.findById(offboardingId)
                .orElseThrow(() -> new IllegalArgumentException("Offboarding record not found with ID: " + offboardingId));

        List<OffboardingTask> tasks = offboardingTaskRepository.findByOffboardingId(offboardingId);

        Map<String, Map<String, Object>> deptStats = new LinkedHashMap<>();
        
        // Group tasks by assignedTo
        Map<String, List<OffboardingTask>> tasksByDept = tasks.stream()
                .collect(Collectors.groupingBy(t -> t.getAssignedTo() != null ? t.getAssignedTo() : "EMPLOYEE"));

        for (Map.Entry<String, List<OffboardingTask>> entry : tasksByDept.entrySet()) {
            String dept = entry.getKey();
            List<OffboardingTask> deptTasks = entry.getValue();
            long total = deptTasks.size();
            long completed = deptTasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
            double ratio = total > 0 ? (double) completed / total : 0.0;

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("totalTasks", total);
            stats.put("completedTasks", completed);
            stats.put("completionRatio", Math.round(ratio * 100.0) / 100.0);
            stats.put("status", (completed == total && total > 0) ? "CLEARED" : "PENDING");

            deptStats.put(dept, stats);
        }

        // Add overall stats
        long overallTotal = tasks.size();
        long overallCompleted = tasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        double overallRatio = overallTotal > 0 ? (double) overallCompleted / overallTotal : 0.0;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("offboardingId", offboardingId);
        response.put("employeeName", offboarding.getEmployee().getFullName());
        response.put("departmentClearance", deptStats);
        response.put("overallCompletionRatio", Math.round(overallRatio * 100.0) / 100.0);
        response.put("isFullyCleared", overallCompleted == overallTotal && overallTotal > 0);

        return response;
    }
}
