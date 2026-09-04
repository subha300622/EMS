package com.example.ems.leave.service;

import com.example.ems.approval.entity.ApprovalWorkflowInstance;
import com.example.ems.approval.entity.WorkflowType;
import com.example.ems.approval.service.ApprovalWorkflowEngineService;
import com.example.ems.employee.entity.Employee;
import com.example.ems.leave.dto.*;
import com.example.ems.leave.entity.*;
import com.example.ems.leave.repository.*;
import com.example.ems.leave.event.LeaveApprovedEvent;
import com.example.ems.leave.event.LeaveCancelledEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for orchestrating Leave Management lifecycle operations.
 * Handles leave types, policies, requests, balances, and approvals.
 */
@Service
public class LeaveService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LeaveService.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ApprovalWorkflowEngineService approvalWorkflowEngineService;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveTypeRepository leaveTypeRepository;

    @Autowired
    private LeavePolicyRepository leavePolicyRepository;

    @Autowired
    private LeaveRuleRepository leaveRuleRepository;

    @Autowired
    private LeaveAccrualRuleRepository accrualRuleRepository;

    @Autowired
    private LeaveRequestHistoryRepository historyRepository;

    @Autowired
    private LeaveRuleValidationService leaveRuleValidationService;

    @Autowired
    private LeaveBalanceService balanceService;

    // == 1. LEAVE TYPES ============================================================
    @Transactional
    public LeaveType createLeaveType(Employee admin, LeaveTypeRequest request) {
        LeaveType leaveType = new LeaveType();
        leaveType.setName(request.getName());
        leaveType.setDescription(request.getDescription());
        leaveType.setDefaultDays(request.getDefaultDays() != null ? request.getDefaultDays() : 0);
        leaveType.setActive(true);
        if (admin != null) leaveType.setOrganization(admin.getOrganization());
        return leaveTypeRepository.save(leaveType);
    }

    public List<LeaveType> getAllLeaveTypes(Long orgId) {
        return leaveTypeRepository.findAll().stream()
                .filter(lt -> lt.getOrganization() == null || orgId == null || orgId.equals(lt.getOrganization().getId()))
                .collect(Collectors.toList());
    }

    public LeaveType getLeaveTypeById(Long id) {
        return leaveTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave type not found: " + id));
    }

    @Transactional
    public LeaveType updateLeaveType(Long id, LeaveTypeRequest request) {
        LeaveType lt = getLeaveTypeById(id);
        lt.setName(request.getName());
        lt.setDescription(request.getDescription());
        if (request.getDefaultDays() != null) lt.setDefaultDays(request.getDefaultDays());
        return leaveTypeRepository.save(lt);
    }

    @Transactional
    public void deleteLeaveType(Long id) {
        LeaveType lt = getLeaveTypeById(id);
        lt.setActive(false);
        leaveTypeRepository.save(lt);
    }

    @Transactional
    public LeaveType toggleLeaveTypeStatus(Long id, boolean active) {
        LeaveType lt = getLeaveTypeById(id);
        lt.setActive(active);
        return leaveTypeRepository.save(lt);
    }

    // == 2. LEAVE POLICIES =========================================================
    @Transactional
    public LeavePolicy createLeavePolicy(Employee admin, LeavePolicyRequest request) {
        LeaveType lt = request.getLeaveTypeId() != null ? getLeaveTypeById(request.getLeaveTypeId()) : null;
        LeavePolicy policy = new LeavePolicy();
        policy.setName(request.getName());
        policy.setLeaveType(lt);
        policy.setCarryingLimit(request.getCarryingLimit());
        policy.setAccrualType(request.getAccrualType());
        policy.setStatus("ACTIVE");
        if (admin != null) policy.setOrganization(admin.getOrganization());
        return leavePolicyRepository.save(policy);
    }

    public List<LeavePolicy> getAllLeavePolicies(Long orgId) {
        return leavePolicyRepository.findAll().stream()
                .filter(p -> p.getOrganization() == null || orgId == null || orgId.equals(p.getOrganization().getId()))
                .collect(Collectors.toList());
    }

    public LeavePolicy getLeavePolicyById(Long id) {
        return leavePolicyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave policy not found: " + id));
    }

    @Transactional
    public LeavePolicy updateLeavePolicy(Long id, LeavePolicyRequest request) {
        LeavePolicy p = getLeavePolicyById(id);
        p.setName(request.getName());
        if (request.getLeaveTypeId() != null) p.setLeaveType(getLeaveTypeById(request.getLeaveTypeId()));
        if (request.getCarryingLimit() != null) p.setCarryingLimit(request.getCarryingLimit());
        if (request.getAccrualType() != null) p.setAccrualType(request.getAccrualType());
        return leavePolicyRepository.save(p);
    }

    @Transactional
    public void deleteLeavePolicy(Long id) {
        LeavePolicy p = getLeavePolicyById(id);
        p.setStatus("INACTIVE");
        leavePolicyRepository.save(p);
    }

    @Transactional
    public LeavePolicy toggleLeavePolicyStatus(Long id, String status) {
        LeavePolicy p = getLeavePolicyById(id);
        p.setStatus(status != null ? status : "ACTIVE");
        return leavePolicyRepository.save(p);
    }

    // == 3. LEAVE RULES ============================================================
    @Transactional
    public LeaveRule createLeaveRule(Employee admin, CreateLeaveRuleRequest request) {
        LeaveType lt = request.getLeaveTypeId() != null ? getLeaveTypeById(request.getLeaveTypeId()) : null;
        LeaveRule rule = new LeaveRule();
        rule.setLeaveType(lt);
        if (admin != null) rule.setOrganization(admin.getOrganization());
        rule.setMinServiceDays(request.getMinServiceDays());
        rule.setMaxConsecutiveDays(request.getMaxConsecutiveDays());
        rule.setIncludeWeekends(request.isIncludeWeekends());
        rule.setIncludeHolidays(request.isIncludeHolidays());
        rule.setAllowHalfDay(request.isAllowHalfDay());
        rule.setNoticePeriodDays(request.getNoticePeriodDays());
        rule.setAllowNegativeBalance(request.isAllowNegativeBalance());
        rule.setMaxCarryForwardDays(request.getMaxCarryForwardDays());
        rule.setActive(true);
        return leaveRuleRepository.save(rule);
    }

    public List<LeaveRule> getAllLeaveRules(Long orgId) {
        return leaveRuleRepository.findAll().stream()
                .filter(r -> r.getOrganization() == null || orgId == null || orgId.equals(r.getOrganization().getId()))
                .collect(Collectors.toList());
    }

    public LeaveRule getLeaveRuleById(Long id) {
        return leaveRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave rule not found: " + id));
    }

    @Transactional
    public LeaveRule updateLeaveRule(Long id, CreateLeaveRuleRequest request) {
        LeaveRule r = getLeaveRuleById(id);
        if (request.getMinServiceDays() != null) r.setMinServiceDays(request.getMinServiceDays());
        if (request.getMaxConsecutiveDays() != null) r.setMaxConsecutiveDays(request.getMaxConsecutiveDays());
        r.setIncludeWeekends(request.isIncludeWeekends());
        r.setIncludeHolidays(request.isIncludeHolidays());
        r.setAllowHalfDay(request.isAllowHalfDay());
        if (request.getNoticePeriodDays() != null) r.setNoticePeriodDays(request.getNoticePeriodDays());
        r.setAllowNegativeBalance(request.isAllowNegativeBalance());
        if (request.getMaxCarryForwardDays() != null) r.setMaxCarryForwardDays(request.getMaxCarryForwardDays());
        return leaveRuleRepository.save(r);
    }

    @Transactional
    public void deleteLeaveRule(Long id) {
        LeaveRule r = getLeaveRuleById(id);
        r.setActive(false);
        leaveRuleRepository.save(r);
    }

    //  4. ACCRUAL RULES  ============================================================
    @Transactional
    public LeaveAccrualRule createAccrualRule(Employee admin, CreateAccrualRuleRequest request) {
        LeaveType lt = request.getLeaveTypeId() != null ? getLeaveTypeById(request.getLeaveTypeId()) : null;
        LeaveAccrualRule rule = new LeaveAccrualRule();
        rule.setLeaveType(lt);
        if (admin != null) rule.setOrganization(admin.getOrganization());
        rule.setAnnualQuota(request.getAnnualQuota());
        rule.setAccrualFrequency(request.getAccrualFrequency());
        rule.setCreditAmount(request.getCreditAmount());
        rule.setActive(true);
        return accrualRuleRepository.save(rule);
    }

    public List<LeaveAccrualRule> getAllAccrualRules(Long orgId) {
        return accrualRuleRepository.findAll().stream()
                .filter(r -> r.getOrganization() == null || orgId == null || orgId.equals(r.getOrganization().getId()))
                .collect(Collectors.toList());
    }

    public LeaveAccrualRule getAccrualRuleById(Long id) {
        return accrualRuleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Accrual rule not found: " + id));
    }

    @Transactional
    public LeaveAccrualRule updateAccrualRule(Long id, CreateAccrualRuleRequest request) {
        LeaveAccrualRule r = getAccrualRuleById(id);
        if (request.getAnnualQuota() != null) r.setAnnualQuota(request.getAnnualQuota());
        if (request.getAccrualFrequency() != null) r.setAccrualFrequency(request.getAccrualFrequency());
        if (request.getCreditAmount() != null) r.setCreditAmount(request.getCreditAmount());
        return accrualRuleRepository.save(r);
    }

    @Transactional
    public void deleteAccrualRule(Long id) {
        LeaveAccrualRule r = getAccrualRuleById(id);
        r.setActive(false);
        accrualRuleRepository.save(r);
    }

    // == 5. LEAVE REQUEST LIFECYCLE ================================================
    @Transactional
    public Leave applyLeave(Employee employee, LeaveRequest request) {
        LeaveType leaveType = getLeaveTypeById(request.getLeaveTypeId());
        if (!leaveType.isActive()) {
            throw new IllegalArgumentException("Leave type is inactive: " + leaveType.getName());
        }

        // Validate Leave Rules
        leaveRuleValidationService.validateLeaveRequest(employee, leaveType, request);

        // Fetch Leave Rule to compute exact duration
        Long orgId = employee.getOrganization() != null ? employee.getOrganization().getId() : 1L;
        LeaveRule rule = leaveRuleRepository.findByLeaveTypeIdAndOrganizationId(leaveType.getId(), orgId)
                .or(() -> leaveRuleRepository.findByLeaveTypeId(leaveType.getId()))
                .orElse(null);

        Double durationDays = leaveRuleValidationService.calculateLeaveDays(rule, request.getStartDate(), request.getEndDate(), request.getDurationType(), orgId);

        // Reserve Pending Balance
        int year = request.getStartDate().getYear();
        balanceService.reserveBalance(employee, leaveType, year, durationDays);

        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setLeaveType(leaveType);
        leave.setOrganization(employee.getOrganization());
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setDurationType(request.getDurationType());
        leave.setDurationDays(durationDays);
        leave.setReason(request.getReason());
        leave.setStatus("PENDING");
        leave.setApprover(employee.getManager());
        leave.setAppliedAt(LocalDateTime.now());
        leave.setUpdatedAt(LocalDateTime.now());

        Leave savedLeave = leaveRepository.save(leave);

        // Start Generic Approval Engine Instance
        Map<String, Object> context = new HashMap<>();
        context.put("requesterId", employee.getId());
        context.put("leaveId", savedLeave.getId());
        context.put("durationDays", durationDays);

        try {
            ApprovalWorkflowInstance instance = approvalWorkflowEngineService.startWorkflow(
                    WorkflowType.LEAVE_APPROVAL,
                    "LEAVE_REQUEST",
                    savedLeave.getId().toString(),
                    employee,
                    context
            );
            savedLeave.setApprovalWorkflowInstanceId(instance.getWorkflowInstanceId());
            savedLeave = leaveRepository.save(savedLeave);
        } catch (Exception e) {
            log.warn("Approval workflow start failed/skipped: {}", e.getMessage());
        }

        // Audit History
        historyRepository.save(new LeaveRequestHistory(
                savedLeave, "APPLIED", employee, null, "PENDING", "Leave request submitted"
        ));

        return savedLeave;
    }

    public List<Leave> getLeaves(Long orgId, Long employeeId, Long leaveTypeId, String status, LocalDate fromDate, LocalDate toDate, Long departmentId) {
        String deptStr = departmentId != null ? String.valueOf(departmentId) : null;
        return leaveRepository.findFilteredLeaves(orgId, employeeId, leaveTypeId, status, fromDate, toDate, deptStr);
    }

    public Optional<Leave> getLeaveById(Long id) {
        return leaveRepository.findById(id);
    }

    @Transactional
    public Leave updateLeave(Long leaveId, Employee employee, LeaveRequest request) {
        Leave leave = getLeaveById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + leaveId));

        if (!"PENDING".equalsIgnoreCase(leave.getStatus())) {
            throw new IllegalStateException("Only leave requests in PENDING status can be edited");
        }

        // Release old pending balance
        int oldYear = leave.getStartDate().getYear();
        balanceService.releasePendingBalance(leave.getEmployee(), leave.getLeaveType(), oldYear, leave.getDurationDays());

        // Validate new dates/rules
        LeaveType newLt = getLeaveTypeById(request.getLeaveTypeId());
        leaveRuleValidationService.validateLeaveRequest(employee, newLt, request);

        Long orgId = employee.getOrganization() != null ? employee.getOrganization().getId() : 1L;
        LeaveRule rule = leaveRuleRepository.findByLeaveTypeIdAndOrganizationId(newLt.getId(), orgId).orElse(null);
        Double newDuration = leaveRuleValidationService.calculateLeaveDays(rule, request.getStartDate(), request.getEndDate(), request.getDurationType(), orgId);

        // Reserve new pending balance
        balanceService.reserveBalance(employee, newLt, request.getStartDate().getYear(), newDuration);

        String oldStatus = leave.getStatus();
        leave.setLeaveType(newLt);
        leave.setStartDate(request.getStartDate());
        leave.setEndDate(request.getEndDate());
        leave.setDurationType(request.getDurationType());
        leave.setDurationDays(newDuration);
        leave.setReason(request.getReason());
        leave.setUpdatedAt(LocalDateTime.now());

        Leave updatedLeave = leaveRepository.save(leave);

        historyRepository.save(new LeaveRequestHistory(
                updatedLeave, "EDITED", employee, oldStatus, "PENDING", "Leave request details modified"
        ));

        return updatedLeave;
    }

    @Transactional
    public Leave cancelLeave(Long leaveId, Employee actor) {
        Leave leave = getLeaveById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + leaveId));

        String oldStatus = leave.getStatus();
        int year = leave.getStartDate().getYear();

        if ("PENDING".equalsIgnoreCase(oldStatus)) {
            balanceService.releasePendingBalance(leave.getEmployee(), leave.getLeaveType(), year, leave.getDurationDays());
        } else if ("APPROVED".equalsIgnoreCase(oldStatus)) {
            balanceService.refundApprovedBalance(leave.getEmployee(), leave.getLeaveType(), year, leave.getDurationDays());
        }

        leave.setStatus("CANCELLED");
        leave.setUpdatedAt(LocalDateTime.now());
        Leave saved = leaveRepository.save(leave);

        historyRepository.save(new LeaveRequestHistory(
                saved, "CANCELLED", actor, oldStatus, "CANCELLED", "Leave request cancelled"
        ));

        if ("APPROVED".equalsIgnoreCase(oldStatus)) {
            String empCode = saved.getEmployee() != null ? (saved.getEmployee().getEmployeeId() != null ? saved.getEmployee().getEmployeeId() : saved.getEmployee().getId().toString()) : "UNKNOWN";
            eventPublisher.publishEvent(new LeaveCancelledEvent(saved.getId(), empCode, saved.getStartDate(), saved.getEndDate()));
        }

        return saved;
    }

    public List<LeaveRequestHistory> getLeaveHistory(Long leaveId) {
        return historyRepository.findByLeaveIdOrderByPerformedAtAsc(leaveId);
    }

    // == 6. LEAVE CALENDAR APIS ====================================================

    public List<LeaveCalendarEventDto> getLeaveCalendarEvents(
            Long orgId,
            Long employeeId,
            Long teamId,
            String department,
            Long leaveTypeId,
            String status,
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        List<String> targetStatuses;
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            targetStatuses = List.of(status.trim().toUpperCase());
        } else {
            targetStatuses = List.of("APPROVED", "PENDING");
        }

        String targetDept = (department != null && !department.trim().isEmpty()) ? department.trim().toLowerCase() : null;

        List<Leave> leaves = leaveRepository.findCalendarEvents(
                orgId, employeeId, teamId, targetDept, leaveTypeId, targetStatuses, startDate, endDate
        );

        return leaves.stream()
                .map(this::mapToCalendarEventDto)
                .collect(Collectors.toList());
    }

    public List<LeaveCalendarEventDto> getEmployeeCalendarEvents(
            Long orgId,
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            String status) {
        return getLeaveCalendarEvents(orgId, employeeId, null, null, null, status, startDate, endDate);
    }

    public List<LeaveCalendarEventDto> getTeamCalendarEvents(
            Long orgId,
            Long teamId,
            LocalDate startDate,
            LocalDate endDate,
            String status) {
        return getLeaveCalendarEvents(orgId, null, teamId, null, null, status, startDate, endDate);
    }

    public List<LeaveCalendarEventDto> getDepartmentCalendarEvents(
            Long orgId,
            String department,
            LocalDate startDate,
            LocalDate endDate,
            String status) {
        return getLeaveCalendarEvents(orgId, null, null, department, null, status, startDate, endDate);
    }

    private LeaveCalendarEventDto mapToCalendarEventDto(Leave leave) {
        Employee emp = leave.getEmployee();
        LeaveType lt = leave.getLeaveType();

        Long empId = emp != null ? emp.getId() : null;
        String empCode = emp != null ? emp.getEmployeeId() : null;
        String empName = emp != null ? emp.getFullName() : "Unknown";
        String empEmail = emp != null ? emp.getEmail() : null;
        String dept = emp != null ? emp.getDepartment() : null;

        Long teamId = (emp != null && emp.getTeam() != null) ? emp.getTeam().getId() : null;
        String teamName = (emp != null && emp.getTeam() != null) ? emp.getTeam().getTeamName() : null;

        Long ltId = lt != null ? lt.getId() : null;
        String ltName = lt != null ? lt.getName() : "Leave";
        String color = getLeaveTypeColor(ltName, leave.getStatus());

        return new LeaveCalendarEventDto(
                leave.getId(),
                empId,
                empCode,
                empName,
                empEmail,
                dept,
                teamId,
                teamName,
                ltId,
                ltName,
                color,
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getDurationDays(),
                leave.getDurationType(),
                leave.getStatus(),
                leave.getReason()
        );
    }

    private String getLeaveTypeColor(String leaveTypeName, String status) {
        if ("PENDING".equalsIgnoreCase(status)) return "#F59E0B";
        if ("CANCELLED".equalsIgnoreCase(status)) return "#9CA3AF";
        if ("REJECTED".equalsIgnoreCase(status)) return "#EF4444";

        if (leaveTypeName == null) return "#3B82F6";
        String lower = leaveTypeName.toLowerCase();
        if (lower.contains("annual") || lower.contains("paid")) return "#3B82F6";
        if (lower.contains("sick") || lower.contains("medical")) return "#10B981";
        if (lower.contains("casual")) return "#8B5CF6";
        if (lower.contains("maternity") || lower.contains("paternity")) return "#EC4899";
        return "#3B82F6";
    }

    public List<Leave> getOrganizationCalendar(Long orgId, LocalDate fromDate, LocalDate toDate) {
        return leaveRepository.findFilteredLeaves(orgId, null, null, "APPROVED", fromDate, toDate, null);
    }

    public List<Leave> getEmployeeCalendar(Long employeeId, LocalDate fromDate, LocalDate toDate) {
        return leaveRepository.findFilteredLeaves(null, employeeId, null, null, fromDate, toDate, null);
    }

    // == BACKWARD COMPATIBILITY HELPERS ============================================

    public List<Leave> getAllLeaves() {
        return leaveRepository.findAll();
    }

    public Map<String, Object> getLeaveBalance(Long employeeId) {
        List<LeaveBalance> balances = balanceService.getEmployeeBalances(employeeId, LocalDate.now().getYear());
        Map<String, Object> map = new HashMap<>();
        for (LeaveBalance b : balances) {
            if (b.getLeaveType() != null) {
                map.put(b.getLeaveType().getName(), b.getAvailableBalance());
            }
        }
        return map;
    }

    @Transactional
    public Leave approveLeave(Long leaveId, Employee approver) {
        Leave leave = getLeaveById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + leaveId));
        int year = leave.getStartDate().getYear();
        String oldStatus = leave.getStatus();
        leave.setStatus("APPROVED");
        leave.setApprovedBy(approver);
        leave.setApprovedAt(LocalDateTime.now());
        leave.setUpdatedAt(LocalDateTime.now());
        Leave saved = leaveRepository.save(leave);

        balanceService.commitBalance(saved.getEmployee(), saved.getLeaveType(), year, saved.getDurationDays());

        historyRepository.save(new LeaveRequestHistory(
                saved, "APPROVED", approver, oldStatus, "APPROVED", "Approved directly"
        ));

        String empCode = saved.getEmployee() != null ? (saved.getEmployee().getEmployeeId() != null ? saved.getEmployee().getEmployeeId() : saved.getEmployee().getId().toString()) : "UNKNOWN";
        String leaveTypeName = saved.getLeaveType() != null ? saved.getLeaveType().getName() : "LEAVE";
        eventPublisher.publishEvent(new LeaveApprovedEvent(saved.getId(), empCode, saved.getStartDate(), saved.getEndDate(), leaveTypeName));

        return saved;
    }

    @Transactional
    public Leave rejectLeave(Long leaveId, Employee approver) {
        Leave leave = getLeaveById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + leaveId));
        int year = leave.getStartDate().getYear();
        String oldStatus = leave.getStatus();
        leave.setStatus("REJECTED");
        leave.setApprovedBy(approver);
        leave.setRejectedAt(LocalDateTime.now());
        leave.setUpdatedAt(LocalDateTime.now());
        leaveRepository.save(leave);

        balanceService.releasePendingBalance(leave.getEmployee(), leave.getLeaveType(), year, leave.getDurationDays());

        historyRepository.save(new LeaveRequestHistory(
                leave, "REJECTED", approver, oldStatus, "REJECTED", "Rejected directly"
        ));
        return leave;
    }

    public List<Leave> getLeavesByEmployeeId(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId);
    }

    public LeaveType deactivateLeaveType(Long id) {
        return toggleLeaveTypeStatus(id, false);
    }

    public LeaveType activateLeaveType(Long id) {
        return toggleLeaveTypeStatus(id, true);
    }

    public org.springframework.data.domain.Page<LeaveApprovalResponseDto> getManagerLeaveApprovals(Employee manager, String status, Long employeeId, LocalDate fromDate, LocalDate toDate, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Leave> page = leaveRepository.findManagerLeaveApprovals(manager.getId(), status, employeeId, fromDate, toDate, pageable);
        return page.map(l -> new LeaveApprovalResponseDto(
                l.getId(),
                l.getEmployee() != null ? l.getEmployee().getId() : null,
                l.getEmployee() != null ? l.getEmployee().getEmployeeId() : null,
                l.getEmployee() != null ? l.getEmployee().getFullName() : null,
                l.getEmployee() != null ? l.getEmployee().getDepartment() : null,
                l.getLeaveType() != null ? l.getLeaveType().getName() : null,
                l.getStartDate(),
                l.getEndDate(),
                l.getDurationDays() != null ? l.getDurationDays().longValue() : 1L,
                l.getReason(),
                l.getAppliedAt(),
                l.getStatus()
        ));
    }

    public LeaveApprovalResponseDto getManagerLeaveApprovalDetails(Long leaveId, Employee manager) {
        Leave l = getLeaveById(leaveId).orElse(null);
        if (l == null) return null;
        return new LeaveApprovalResponseDto(
                l.getId(),
                l.getEmployee() != null ? l.getEmployee().getId() : null,
                l.getEmployee() != null ? l.getEmployee().getEmployeeId() : null,
                l.getEmployee() != null ? l.getEmployee().getFullName() : null,
                l.getEmployee() != null ? l.getEmployee().getDepartment() : null,
                l.getLeaveType() != null ? l.getLeaveType().getName() : null,
                l.getStartDate(),
                l.getEndDate(),
                l.getDurationDays() != null ? l.getDurationDays().longValue() : 1L,
                l.getReason(),
                l.getAppliedAt(),
                l.getStatus()
        );
    }

    public LeaveApprovalSummaryDto getLeaveApprovalSummary(Employee manager) {
        long pending = leaveRepository.countPendingForManager(manager.getId());
        long approved = leaveRepository.countApprovedTodayForManager(manager.getId(), LocalDateTime.now().withHour(0).withMinute(0));
        long rejected = leaveRepository.countRejectedTodayForManager(manager.getId(), LocalDateTime.now().withHour(0).withMinute(0));
        return new LeaveApprovalSummaryDto(pending, approved, rejected);
    }

    @Transactional
    public ManagerApprovalActionResponseDto approveLeaveWithComment(Long leaveId, String comment, Employee approver) {
        Leave leave = approveLeave(leaveId, approver);
        leave.setManagerComment(comment);
        leaveRepository.save(leave);
        ManagerApprovalActionResponseDto resp = new ManagerApprovalActionResponseDto();
        resp.setLeaveId(leave.getId());
        resp.setStatus("APPROVED");
        resp.setApprovedBy(approver != null ? approver.getId() : null);
        resp.setApprovedAt(LocalDateTime.now());
        return resp;
    }

    @Transactional
    public ManagerApprovalActionResponseDto rejectLeaveWithComment(Long leaveId, String comment, Employee approver) {
        Leave leave = rejectLeave(leaveId, approver);
        leave.setManagerComment(comment);
        leaveRepository.save(leave);
        ManagerApprovalActionResponseDto resp = new ManagerApprovalActionResponseDto();
        resp.setLeaveId(leave.getId());
        resp.setStatus("REJECTED");
        resp.setRejectedBy(approver != null ? approver.getId() : null);
        resp.setRejectedAt(LocalDateTime.now());
        return resp;
    }

    @Transactional
    public ManagerApprovalActionResponseDto sendBackLeaveWithComment(Long leaveId, String comment, Employee approver) {
        Leave leave = getLeaveById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found: " + leaveId));
        int year = leave.getStartDate().getYear();
        String oldStatus = leave.getStatus();
        leave.setStatus("NEEDS_REVISION");
        leave.setManagerComment(comment);
        leave.setUpdatedAt(LocalDateTime.now());
        leaveRepository.save(leave);

        balanceService.releasePendingBalance(leave.getEmployee(), leave.getLeaveType(), year, leave.getDurationDays());

        historyRepository.save(new LeaveRequestHistory(
                leave, "SENT_BACK", approver, oldStatus, "NEEDS_REVISION", comment != null ? comment : "Sent back for revision"
        ));

        ManagerApprovalActionResponseDto resp = new ManagerApprovalActionResponseDto();
        resp.setLeaveId(leave.getId());
        resp.setStatus("NEEDS_REVISION");
        return resp;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardMetrics(Long orgId) {
        List<Leave> allLeaves = leaveRepository.findFilteredLeaves(orgId, null, null, null, null, null, null);
        long total = allLeaves.size();
        long pending = allLeaves.stream().filter(l -> "PENDING".equalsIgnoreCase(l.getStatus())).count();
        long approved = allLeaves.stream().filter(l -> "APPROVED".equalsIgnoreCase(l.getStatus())).count();
        long rejected = allLeaves.stream().filter(l -> "REJECTED".equalsIgnoreCase(l.getStatus())).count();
        long cancelled = allLeaves.stream().filter(l -> "CANCELLED".equalsIgnoreCase(l.getStatus())).count();

        Map<String, Long> summary = Map.of(
                "total", total,
                "pending", pending,
                "approved", approved,
                "rejected", rejected,
                "cancelled", cancelled
        );

        Map<String, Long> byType = allLeaves.stream()
                .filter(l -> l.getLeaveType() != null && l.getLeaveType().getName() != null)
                .collect(Collectors.groupingBy(l -> l.getLeaveType().getName(), Collectors.counting()));

        Map<String, Long> byDept = allLeaves.stream()
                .filter(l -> l.getEmployee() != null && l.getEmployee().getDepartment() != null)
                .collect(Collectors.groupingBy(l -> l.getEmployee().getDepartment(), Collectors.counting()));

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("summary", summary);
        dashboard.put("leaveTypeDistribution", byType);
        dashboard.put("departmentDistribution", byDept);
        return dashboard;
    }
}


