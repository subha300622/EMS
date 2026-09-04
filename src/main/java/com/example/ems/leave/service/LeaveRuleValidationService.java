package com.example.ems.leave.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.holiday.repository.HolidayRepository;
import com.example.ems.leave.dto.LeaveRequest;
import com.example.ems.leave.entity.Leave;
import com.example.ems.leave.entity.LeaveBalance;
import com.example.ems.leave.entity.LeaveRule;
import com.example.ems.leave.entity.LeaveType;
import com.example.ems.leave.repository.LeaveBalanceRepository;
import com.example.ems.leave.repository.LeaveRepository;
import com.example.ems.leave.repository.LeaveRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class LeaveRuleValidationService {

    @Autowired
    private LeaveRuleRepository leaveRuleRepository;

    @Autowired
    private LeaveRepository leaveRepository;

    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    public Double calculateLeaveDays(LeaveRule rule, LocalDate startDate, LocalDate endDate, String durationType, Long orgId) {
        if ("FIRST_HALF".equalsIgnoreCase(durationType) || "SECOND_HALF".equalsIgnoreCase(durationType)) {
            return 0.5;
        }

        double totalDays = 0.0;
        boolean incWeekends = rule != null && rule.isIncludeWeekends();
        boolean incHolidays = rule != null && rule.isIncludeHolidays();

        Long targetOrgId = (rule != null && rule.getOrganization() != null) ? rule.getOrganization().getId() : (orgId != null ? orgId : 1L);

        LocalDate curr = startDate;
        while (!curr.isAfter(endDate)) {
            boolean isWeekend = (curr.getDayOfWeek() == DayOfWeek.SATURDAY || curr.getDayOfWeek() == DayOfWeek.SUNDAY);
            boolean isHoliday = false;

            if (!incHolidays) {
                if (holidayRepository.existsByOrganizationIdAndHolidayDate(targetOrgId, curr)) {
                    isHoliday = true;
                }
            }

            if ((!isWeekend || incWeekends) && (!isHoliday || incHolidays)) {
                totalDays += 1.0;
            }
            curr = curr.plusDays(1);
        }
        return totalDays;
    }

    public Double calculateLeaveDays(LeaveRule rule, LocalDate startDate, LocalDate endDate, String durationType) {
        return calculateLeaveDays(rule, startDate, endDate, durationType, null);
    }

    public void validateLeaveRequest(Employee employee, LeaveType leaveType, LeaveRequest request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        // Fetch Rule for Leave Type & Org
        Long orgId = employee.getOrganization() != null ? employee.getOrganization().getId() : 1L;
        LeaveRule rule = leaveRuleRepository.findByLeaveTypeIdAndOrganizationId(leaveType.getId(), orgId)
                .or(() -> leaveRuleRepository.findByLeaveTypeId(leaveType.getId()))
                .orElse(null);

        // Calculate Net Working Days (excluding weekends and holidays unless rule allows)
        Double durationDays = calculateLeaveDays(rule, startDate, endDate, request.getDurationType(), orgId);
        if (durationDays <= 0.0) {
            throw new IllegalArgumentException("NO_WORKING_DAYS_IN_LEAVE_RANGE: Requested leave range contains no working days (all dates are holidays or weekends)");
        }

        // 1. Min Service Days
        if (rule != null && rule.getMinServiceDays() != null && rule.getMinServiceDays() > 0) {
            if (employee.getJoiningDate() != null) {
                long serviceDays = ChronoUnit.DAYS.between(employee.getJoiningDate(), LocalDate.now());
                if (serviceDays < rule.getMinServiceDays()) {
                    throw new IllegalArgumentException("Minimum service period of " + rule.getMinServiceDays() + " days is required before applying for " + leaveType.getName());
                }
            }
        }

        // 2. Half-Day Availability
        if ("FIRST_HALF".equalsIgnoreCase(request.getDurationType()) || "SECOND_HALF".equalsIgnoreCase(request.getDurationType())) {
            if (rule != null && !rule.isAllowHalfDay()) {
                throw new IllegalArgumentException("Half-day leaves are not allowed for leave type: " + leaveType.getName());
            }
        }

        // 3. Max Consecutive Days
        if (rule != null && rule.getMaxConsecutiveDays() != null && rule.getMaxConsecutiveDays() > 0) {
            if (durationDays > rule.getMaxConsecutiveDays()) {
                throw new IllegalArgumentException("Requested duration (" + durationDays + " days) exceeds maximum consecutive limit of " + rule.getMaxConsecutiveDays() + " days");
            }
        }

        // 4. Notice Period
        if (rule != null && rule.getNoticePeriodDays() != null && rule.getNoticePeriodDays() > 0) {
            long daysInAdvance = ChronoUnit.DAYS.between(LocalDate.now(), startDate);
            if (daysInAdvance < rule.getNoticePeriodDays()) {
                throw new IllegalArgumentException("Leave request must be applied at least " + rule.getNoticePeriodDays() + " days in advance");
            }
        }

        // 5. Overlapping Leaves Check
        List<Leave> overlaps = leaveRepository.findOverlappingLeaves(employee.getId(), startDate, endDate);
        if (!overlaps.isEmpty()) {
            throw new IllegalArgumentException("Leave request overlaps with an existing pending or approved leave request");
        }

        // 6. Balance Verification
        Optional<LeaveBalance> balanceOpt = leaveBalanceRepository.findByEmployeeIdAndLeaveTypeIdAndYear(
                employee.getId(), leaveType.getId(), startDate.getYear());

        double available = balanceOpt.map(LeaveBalance::getAvailableBalance).orElse((double) leaveType.getDefaultDays());
        boolean allowNeg = rule != null && rule.isAllowNegativeBalance();

        if (!allowNeg && durationDays > available) {
            throw new IllegalArgumentException("Insufficient leave balance. Available: " + available + " days, Requested: " + durationDays + " days");
        }
    }
}
