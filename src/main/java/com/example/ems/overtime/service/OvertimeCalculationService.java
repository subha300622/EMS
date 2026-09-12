package com.example.ems.overtime.service;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.common.exception.BadRequestException;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.holiday.repository.HolidayRepository;
import com.example.ems.overtime.dto.OvertimePreviewResponse;
import com.example.ems.overtime.dto.OvertimeRecordResponse;
import com.example.ems.overtime.entity.*;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
import com.example.ems.payroll.dto.SalaryCalculatedComponentResponse;
import com.example.ems.payroll.dto.SalaryCalculationResponse;
import com.example.ems.payroll.service.SalaryCalculationService;
import com.example.ems.schedule.entity.MyShift;
import com.example.ems.schedule.repository.MyShiftRepository;
import com.example.ems.security.context.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
@Transactional
public class OvertimeCalculationService {

    private static final Logger log = LoggerFactory.getLogger(OvertimeCalculationService.class);

    private final OvertimePolicyService policyService;
    private final OvertimeRecordRepository recordRepository;
    private final AttendanceRepository attendanceRepository;
    private final HolidayRepository holidayRepository;

    @Autowired(required = false)
    private MyShiftRepository shiftRepository;

    @Autowired(required = false)
    private SalaryCalculationService salaryCalculationService;

    @Autowired(required = false)
    private com.example.ems.organization.service.OrganizationCompensationConfigService compensationConfigService;

    @Autowired
    public OvertimeCalculationService(OvertimePolicyService policyService,
                                      OvertimeRecordRepository recordRepository,
                                      AttendanceRepository attendanceRepository,
                                      HolidayRepository holidayRepository,
                                      @Autowired(required = false) com.example.ems.organization.service.OrganizationCompensationConfigService compensationConfigService) {
        this.policyService = policyService;
        this.recordRepository = recordRepository;
        this.attendanceRepository = attendanceRepository;
        this.holidayRepository = holidayRepository;
        this.compensationConfigService = compensationConfigService;
    }

    public OvertimeCalculationService(OvertimePolicyService policyService,
                                      OvertimeRecordRepository recordRepository,
                                      AttendanceRepository attendanceRepository,
                                      HolidayRepository holidayRepository) {
        this(policyService, recordRepository, attendanceRepository, holidayRepository, null);
    }

    /**
     * Stateless Preview API for Overtime Calculation.
     * Computes exact OT duration, day multiplier, hourly rate, and amounts without persisting records.
     */
    @Transactional(readOnly = true)
    public OvertimePreviewResponse previewOvertime(Long attendanceId) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireOvertimeEnabled(orgId);
        }
        CalculationContext ctx = buildCalculationContext(attendanceId, orgId);
        return ctx.toPreviewResponse();
    }

    /**
     * Persists a calculated OvertimeRecord in CALCULATED status.
     * Enforces uniqueness per (organization, employee, attendance).
     */
    public OvertimeRecordResponse calculateOvertime(Long attendanceId) {
        Long orgId = TenantContext.requireOrganizationId();
        if (compensationConfigService != null) {
            compensationConfigService.requireOvertimeEnabled(orgId);
        }
        CalculationContext ctx = buildCalculationContext(attendanceId, orgId);

        if (recordRepository.existsByOrganizationIdAndEmployeeIdAndAttendanceId(orgId, ctx.employee.getId(), attendanceId)) {
            throw new ConflictException("An overtime record already exists for employee " +
                    ctx.employee.getFullName() + " on attendance record ID " + attendanceId);
        }

        OvertimeRecord record = new OvertimeRecord();
        record.setOrganization(ctx.attendance.getOrganization());
        record.setEmployee(ctx.employee);
        record.setAttendance(ctx.attendance);
        record.setPolicy(ctx.policy);
        record.setPolicyVersion(ctx.policy.getVersion() != null ? ctx.policy.getVersion() : 0L);
        record.setWorkDate(ctx.workDate);

        record.setScheduledMinutes(ctx.scheduledMinutes);
        record.setWorkedMinutes(ctx.workedMinutes);
        record.setRawOtMinutes(ctx.rawOtMinutes);
        record.setCalculatedOtMinutes(ctx.calculatedOtMinutes);

        record.setAmountBasis(ctx.policy.getAmountBasis());
        record.setHourlyRate(ctx.hourlyRate);
        record.setOtMultiplier(ctx.multiplier);
        record.setOtRate(ctx.otRate);
        record.setCalculatedAmount(ctx.calculatedAmount);

        record.setDayType(ctx.dayType);
        record.setStatus(OvertimeStatus.CALCULATED);
        record.setPayrollStatus(OvertimePayrollStatus.PENDING);

        record = recordRepository.save(record);
        log.info("Persisted OvertimeRecord ID={} for Emp ID={} on Date={}, CalculatedAmount={}",
                record.getId(), ctx.employee.getId(), ctx.workDate, record.getCalculatedAmount());

        return OvertimeRecordResponse.fromEntity(record);
    }

    private CalculationContext buildCalculationContext(Long attendanceId, Long orgId) {
        if (attendanceId == null) {
            throw new BadRequestException("attendanceId is mandatory for overtime calculation.");
        }

        Attendance attendance = attendanceRepository.findByIdAndOrganizationId(attendanceId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found with ID: " + attendanceId + " for tenant"));

        Employee employee = attendance.getEmployee();
        if (employee == null) {
            throw new ResourceNotFoundException("No employee associated with attendance record ID: " + attendanceId);
        }

        LocalDate workDate = attendance.getDate() != null ? attendance.getDate() : LocalDate.now();

        // 1. Resolve applicable active policy
        OvertimePolicy policy = policyService.resolveApplicablePolicy(orgId, employee, workDate);

        // 2. Resolve scheduled minutes
        int scheduledMinutes = resolveScheduledMinutes(employee, workDate, policy);

        // 3. Resolve actual worked minutes
        int workedMinutes = resolveWorkedMinutes(attendance);

        // 4. Calculate raw OT
        int rawOtMinutes = Math.max(0, workedMinutes - scheduledMinutes);

        // 5. Minimum OT threshold check
        int payableOtMinutes = rawOtMinutes;
        int minOt = policy.getMinimumOtMinutes() != null ? policy.getMinimumOtMinutes() : 0;
        if (rawOtMinutes < minOt) {
            payableOtMinutes = 0;
        }

        // 6. Maximum OT cap
        int maxOt = policy.getMaximumOtMinutes() != null ? policy.getMaximumOtMinutes() : 240;
        payableOtMinutes = Math.min(payableOtMinutes, maxOt);

        // 7. Apply rounding rule
        payableOtMinutes = applyRounding(payableOtMinutes, policy.getRoundingRule());
        payableOtMinutes = Math.min(payableOtMinutes, maxOt);

        // 8. Day Type Determination
        OvertimeDayType dayType = determineDayType(orgId, workDate);

        // 9. Multiplier Selection
        BigDecimal multiplier = selectMultiplier(policy, dayType);

        // 10. Hourly Rate Calculation
        BigDecimal hourlyRate = calculateHourlyRate(employee, policy, workDate);

        // 11. OT Rate & Amount Calculation
        BigDecimal otRate = hourlyRate.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);

        BigDecimal payableHours = BigDecimal.valueOf(payableOtMinutes)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);

        BigDecimal calculatedAmount = payableHours.multiply(otRate).setScale(2, RoundingMode.HALF_UP);

        return new CalculationContext(
                attendance, employee, policy, workDate, scheduledMinutes, workedMinutes,
                rawOtMinutes, payableOtMinutes, dayType, multiplier, hourlyRate, otRate, calculatedAmount
        );
    }

    private int resolveScheduledMinutes(Employee employee, LocalDate workDate, OvertimePolicy policy) {
        if (shiftRepository != null && employee.getEmail() != null) {
            try {
                Optional<MyShift> shiftOpt = shiftRepository.findByEmployeeEmailAndDate(employee.getEmail(), workDate);
                if (shiftOpt.isPresent() && shiftOpt.get().getTemplate() != null) {
                    var template = shiftOpt.get().getTemplate();
                    if (template.getStartTime() != null && template.getEndTime() != null) {
                        LocalTime start = LocalTime.parse(template.getStartTime());
                        LocalTime end = LocalTime.parse(template.getEndTime());
                        long minutes = Duration.between(start, end).toMinutes();
                        if (minutes < 0) minutes += 24 * 60;
                        int breakMins = template.getBreakDurationMinutes() != null ? template.getBreakDurationMinutes() : 0;
                        return (int) Math.max(0, minutes - breakMins);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not determine shift schedule for employee {}: {}", employee.getId(), e.getMessage());
            }
        }
        int normalHours = policy.getNormalWorkingHours() != null ? policy.getNormalWorkingHours() : 8;
        return normalHours * 60;
    }

    private int resolveWorkedMinutes(Attendance attendance) {
        if (attendance.getTotalWorkingMinutes() != null && attendance.getTotalWorkingMinutes() > 0) {
            return attendance.getTotalWorkingMinutes();
        }
        if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            long durationMins = Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes();
            int breakMins = attendance.getTotalBreakMinutes() != null ? attendance.getTotalBreakMinutes() : 0;
            return (int) Math.max(0, durationMins - breakMins);
        }
        if (attendance.getPayableMinutes() != null && attendance.getPayableMinutes() > 0) {
            return attendance.getPayableMinutes();
        }
        return 0;
    }

    private OvertimeDayType determineDayType(Long orgId, LocalDate workDate) {
        if (holidayRepository != null && holidayRepository.existsByOrganizationIdAndHolidayDate(orgId, workDate)) {
            return OvertimeDayType.HOLIDAY;
        }
        DayOfWeek dow = workDate.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            return OvertimeDayType.WEEKEND;
        }
        return OvertimeDayType.NORMAL_DAY;
    }

    private BigDecimal selectMultiplier(OvertimePolicy policy, OvertimeDayType dayType) {
        if (dayType == OvertimeDayType.HOLIDAY) {
            return policy.getHolidayMultiplier() != null ? policy.getHolidayMultiplier() : BigDecimal.valueOf(2.00);
        }
        if (dayType == OvertimeDayType.WEEKEND) {
            return policy.getWeekendMultiplier() != null ? policy.getWeekendMultiplier() : BigDecimal.valueOf(2.00);
        }
        return policy.getNormalDayMultiplier() != null ? policy.getNormalDayMultiplier() : BigDecimal.valueOf(1.50);
    }

    private BigDecimal calculateHourlyRate(Employee employee, OvertimePolicy policy, LocalDate workDate) {
        if (policy.getAmountBasis() == OvertimeAmountBasis.FIXED_HOURLY_RATE) {
            if (policy.getFixedHourlyRate() != null && policy.getFixedHourlyRate().compareTo(BigDecimal.ZERO) > 0) {
                return policy.getFixedHourlyRate().setScale(2, RoundingMode.HALF_UP);
            }
            throw new BadRequestException("Policy configured with FIXED_HOURLY_RATE but fixedHourlyRate is missing or zero.");
        }

        int workingDays = policy.getWorkingDaysPerMonth() != null ? policy.getWorkingDaysPerMonth() : 26;
        int workingHours = policy.getWorkingHoursPerDay() != null ? policy.getWorkingHoursPerDay() : 8;
        BigDecimal totalHoursInMonth = BigDecimal.valueOf((long) workingDays * workingHours);

        BigDecimal salaryBasisAmount = resolveSalaryBasis(employee, policy.getAmountBasis(), workDate);
        return salaryBasisAmount.divide(totalHoursInMonth, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveSalaryBasis(Employee employee, OvertimeAmountBasis basis, LocalDate workDate) {
        if (salaryCalculationService != null) {
            try {
                SalaryCalculationResponse salaryCalc = salaryCalculationService.calculateSalaryForDate(employee.getId(), workDate);
                if (salaryCalc != null) {
                    if (basis == OvertimeAmountBasis.BASIC_SALARY) {
                        if (salaryCalc.getComponents() != null) {
                            for (SalaryCalculatedComponentResponse comp : salaryCalc.getComponents()) {
                                String code = comp.getComponentCode() != null ? comp.getComponentCode().toUpperCase() : "";
                                if ("BASIC".equals(code) || "BASIC_SALARY".equals(code)) {
                                    return comp.getAmount() != null ? comp.getAmount() : BigDecimal.ZERO;
                                }
                            }
                        }
                        if (salaryCalc.getGrossPay() != null && salaryCalc.getGrossPay().compareTo(BigDecimal.ZERO) > 0) {
                            return salaryCalc.getGrossPay().divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                        }
                    } else if (basis == OvertimeAmountBasis.GROSS_SALARY) {
                        if (salaryCalc.getGrossPay() != null && salaryCalc.getGrossPay().compareTo(BigDecimal.ZERO) > 0) {
                            return salaryCalc.getGrossPay();
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Could not calculate dynamic salary for employee {}: {}. Falling back to annual salary.",
                        employee.getId(), e.getMessage());
            }
        }

        // Fallback to Employee annualSalary if available
        if (employee.getAnnualSalary() != null && employee.getAnnualSalary().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal monthlyGross = employee.getAnnualSalary().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            if (basis == OvertimeAmountBasis.BASIC_SALARY) {
                return monthlyGross.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            }
            return monthlyGross;
        }

        return BigDecimal.valueOf(30000); // Default sensible base
    }

    /**
     * Applies standard nearest rounding rules to OT minutes.
     */
    public int applyRounding(int minutes, OvertimeRoundingRule rule) {
        if (rule == null || rule == OvertimeRoundingRule.EXACT || minutes <= 0) {
            return minutes;
        }
        int interval = switch (rule) {
            case FIVE_MINUTES -> 5;
            case FIFTEEN_MINUTES -> 15;
            case THIRTY_MINUTES -> 30;
            default -> 1;
        };
        return (int) (Math.round((double) minutes / interval) * interval);
    }

    private static class CalculationContext {
        final Attendance attendance;
        final Employee employee;
        final OvertimePolicy policy;
        final LocalDate workDate;
        final int scheduledMinutes;
        final int workedMinutes;
        final int rawOtMinutes;
        final int calculatedOtMinutes;
        final OvertimeDayType dayType;
        final BigDecimal multiplier;
        final BigDecimal hourlyRate;
        final BigDecimal otRate;
        final BigDecimal calculatedAmount;

        CalculationContext(Attendance attendance, Employee employee, OvertimePolicy policy, LocalDate workDate,
                           int scheduledMinutes, int workedMinutes, int rawOtMinutes, int calculatedOtMinutes,
                           OvertimeDayType dayType, BigDecimal multiplier, BigDecimal hourlyRate, BigDecimal otRate,
                           BigDecimal calculatedAmount) {
            this.attendance = attendance;
            this.employee = employee;
            this.policy = policy;
            this.workDate = workDate;
            this.scheduledMinutes = scheduledMinutes;
            this.workedMinutes = workedMinutes;
            this.rawOtMinutes = rawOtMinutes;
            this.calculatedOtMinutes = calculatedOtMinutes;
            this.dayType = dayType;
            this.multiplier = multiplier;
            this.hourlyRate = hourlyRate;
            this.otRate = otRate;
            this.calculatedAmount = calculatedAmount;
        }

        OvertimePreviewResponse toPreviewResponse() {
            OvertimePreviewResponse resp = new OvertimePreviewResponse();
            resp.setAttendanceId(attendance.getId());
            resp.setEmployeeId(employee.getId());
            resp.setEmployeeName(employee.getFullName());
            resp.setWorkDate(workDate);
            resp.setScheduledMinutes(scheduledMinutes);
            resp.setWorkedMinutes(workedMinutes);
            resp.setRawOtMinutes(rawOtMinutes);
            resp.setCalculatedOtMinutes(calculatedOtMinutes);
            resp.setDayType(dayType);
            resp.setAmountBasis(policy.getAmountBasis());
            resp.setHourlyRate(hourlyRate);
            resp.setOtMultiplier(multiplier);
            resp.setOtRate(otRate);
            resp.setCalculatedAmount(calculatedAmount);
            resp.setRoundingRule(policy.getRoundingRule());
            resp.setPolicyId(policy.getId());
            resp.setPolicyName(policy.getName());
            resp.setPolicyVersion(policy.getVersion() != null ? policy.getVersion() : 0L);
            resp.setApprovalRequired(policy.getApprovalRequired());
            return resp;
        }
    }
}
