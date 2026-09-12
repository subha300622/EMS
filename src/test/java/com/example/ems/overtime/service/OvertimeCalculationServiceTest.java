package com.example.ems.overtime.service;

import com.example.ems.attendance.entity.Attendance;
import com.example.ems.attendance.repository.AttendanceRepository;
import com.example.ems.common.exception.ConflictException;
import com.example.ems.common.exception.ResourceNotFoundException;
import com.example.ems.employee.entity.Employee;
import com.example.ems.holiday.repository.HolidayRepository;
import com.example.ems.organization.entity.Organization;
import com.example.ems.overtime.dto.OvertimePreviewResponse;
import com.example.ems.overtime.dto.OvertimeRecordResponse;
import com.example.ems.overtime.entity.*;
import com.example.ems.overtime.repository.OvertimeRecordRepository;
import com.example.ems.security.context.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OvertimeCalculationServiceTest {

    @Mock
    private OvertimePolicyService policyService;

    @Mock
    private OvertimeRecordRepository recordRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private HolidayRepository holidayRepository;

    @InjectMocks
    private OvertimeCalculationService calculationService;

    private Organization organization;
    private Employee employee;
    private Attendance attendance;
    private OvertimePolicy policy;
    private final Long orgId = 1L;
    private final Long empId = 10L;
    private final Long attId = 100L;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(orgId);

        organization = new Organization();
        organization.setId(orgId);

        employee = new Employee();
        employee.setId(empId);
        employee.setFullName("John Doe");
        employee.setEmail("john.doe@company.com");
        employee.setAnnualSalary(BigDecimal.valueOf(624000)); // 52,000 monthly gross -> 26,000 basic
        employee.setOrganization(organization);

        attendance = new Attendance();
        attendance.setId(attId);
        attendance.setOrganization(organization);
        attendance.setEmployee(employee);
        // Wednesday - Normal working day
        attendance.setDate(LocalDate.of(2026, 9, 9));
        attendance.setCheckInTime(Instant.parse("2026-09-09T03:30:00Z")); // 09:00 IST
        attendance.setCheckOutTime(Instant.parse("2026-09-09T14:30:00Z")); // 20:00 IST (11 hrs presence)
        attendance.setTotalBreakMinutes(60); // 1 hr break -> 10 hrs worked (600 mins)
        attendance.setTotalWorkingMinutes(600);

        policy = new OvertimePolicy();
        policy.setId(5L);
        policy.setName("Standard OT Policy");
        policy.setStatus(OvertimePolicyStatus.ACTIVE);
        policy.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        policy.setNormalWorkingHours(8); // 480 mins scheduled
        policy.setMinimumOtMinutes(30);
        policy.setMaximumOtMinutes(240); // 4 hrs cap
        policy.setAmountBasis(OvertimeAmountBasis.BASIC_SALARY);
        policy.setWorkingDaysPerMonth(26);
        policy.setWorkingHoursPerDay(8); // 208 hrs/month
        policy.setNormalDayMultiplier(BigDecimal.valueOf(1.50));
        policy.setWeekendMultiplier(BigDecimal.valueOf(2.00));
        policy.setHolidayMultiplier(BigDecimal.valueOf(2.00));
        policy.setRoundingRule(OvertimeRoundingRule.EXACT);
        policy.setApprovalRequired(true);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testBasicSalaryCalculation_NormalDay() {
        when(attendanceRepository.findByIdAndOrganizationId(attId, orgId)).thenReturn(Optional.of(attendance));
        when(policyService.resolveApplicablePolicy(eq(orgId), any(), eq(attendance.getDate()))).thenReturn(policy);

        OvertimePreviewResponse preview = calculationService.previewOvertime(attId);

        assertNotNull(preview);
        assertEquals(480, preview.getScheduledMinutes());
        assertEquals(600, preview.getWorkedMinutes());
        assertEquals(120, preview.getRawOtMinutes());
        assertEquals(120, preview.getCalculatedOtMinutes()); // 2 hrs OT
        assertEquals(OvertimeDayType.NORMAL_DAY, preview.getDayType());
        assertEquals(BigDecimal.valueOf(1.50), preview.getOtMultiplier());

        // Basic = 26,000 -> hourlyRate = 26,000 / 208 = 125.00
        assertEquals(new BigDecimal("125.00"), preview.getHourlyRate());
        // otRate = 125.00 * 1.5 = 187.50
        assertEquals(new BigDecimal("187.50"), preview.getOtRate());
        // calculatedAmount = 2.0 * 187.50 = 375.00
        assertEquals(new BigDecimal("375.00"), preview.getCalculatedAmount());
    }

    @Test
    void testGrossSalaryCalculation_WeekendMultiplier() {
        // Saturday
        attendance.setDate(LocalDate.of(2026, 9, 12));
        policy.setAmountBasis(OvertimeAmountBasis.GROSS_SALARY);

        when(attendanceRepository.findByIdAndOrganizationId(attId, orgId)).thenReturn(Optional.of(attendance));
        when(policyService.resolveApplicablePolicy(eq(orgId), any(), eq(attendance.getDate()))).thenReturn(policy);

        OvertimePreviewResponse preview = calculationService.previewOvertime(attId);

        assertNotNull(preview);
        assertEquals(OvertimeDayType.WEEKEND, preview.getDayType());
        assertEquals(BigDecimal.valueOf(2.00), preview.getOtMultiplier());
        // Monthly Gross = 52,000 -> hourlyRate = 52,000 / 208 = 250.00
        assertEquals(new BigDecimal("250.00"), preview.getHourlyRate());
        // otRate = 250.00 * 2.0 = 500.00
        assertEquals(new BigDecimal("500.00"), preview.getOtRate());
        // calculatedAmount = 2.0 * 500.00 = 1000.00
        assertEquals(new BigDecimal("1000.00"), preview.getCalculatedAmount());
    }

    @Test
    void testFixedHourlyRate_HolidayMultiplier() {
        attendance.setDate(LocalDate.of(2026, 8, 15)); // Independence Day
        policy.setAmountBasis(OvertimeAmountBasis.FIXED_HOURLY_RATE);
        policy.setFixedHourlyRate(BigDecimal.valueOf(200.00));

        when(attendanceRepository.findByIdAndOrganizationId(attId, orgId)).thenReturn(Optional.of(attendance));
        when(policyService.resolveApplicablePolicy(eq(orgId), any(), eq(attendance.getDate()))).thenReturn(policy);
        when(holidayRepository.existsByOrganizationIdAndHolidayDate(orgId, attendance.getDate())).thenReturn(true);

        OvertimePreviewResponse preview = calculationService.previewOvertime(attId);

        assertNotNull(preview);
        assertEquals(OvertimeDayType.HOLIDAY, preview.getDayType());
        assertEquals(new BigDecimal("200.00"), preview.getHourlyRate());
        assertEquals(BigDecimal.valueOf(2.00), preview.getOtMultiplier());
        assertEquals(new BigDecimal("400.00"), preview.getOtRate());
        // 2 hrs * 400 = 800.00
        assertEquals(new BigDecimal("800.00"), preview.getCalculatedAmount());
    }

    @Test
    void testMinimumThreshold_WhenBelowMin_PayableIsZero() {
        attendance.setTotalWorkingMinutes(500); // 20 mins OT (scheduled 480)
        policy.setMinimumOtMinutes(30);

        when(attendanceRepository.findByIdAndOrganizationId(attId, orgId)).thenReturn(Optional.of(attendance));
        when(policyService.resolveApplicablePolicy(eq(orgId), any(), eq(attendance.getDate()))).thenReturn(policy);

        OvertimePreviewResponse preview = calculationService.previewOvertime(attId);

        assertEquals(20, preview.getRawOtMinutes());
        assertEquals(0, preview.getCalculatedOtMinutes());
        assertEquals(new BigDecimal("0.00"), preview.getCalculatedAmount());
    }

    @Test
    void testMaximumCap_WhenAboveMax_CappedAtPolicyLimit() {
        attendance.setTotalWorkingMinutes(800); // 320 mins raw OT (scheduled 480)
        policy.setMaximumOtMinutes(240); // 4 hrs cap

        when(attendanceRepository.findByIdAndOrganizationId(attId, orgId)).thenReturn(Optional.of(attendance));
        when(policyService.resolveApplicablePolicy(eq(orgId), any(), eq(attendance.getDate()))).thenReturn(policy);

        OvertimePreviewResponse preview = calculationService.previewOvertime(attId);

        assertEquals(320, preview.getRawOtMinutes());
        assertEquals(240, preview.getCalculatedOtMinutes()); // Capped at 240
    }

    @Test
    void testRoundingRules() {
        assertEquals(37, calculationService.applyRounding(37, OvertimeRoundingRule.EXACT));
        assertEquals(35, calculationService.applyRounding(37, OvertimeRoundingRule.FIVE_MINUTES));
        assertEquals(40, calculationService.applyRounding(38, OvertimeRoundingRule.FIVE_MINUTES));
        assertEquals(30, calculationService.applyRounding(37, OvertimeRoundingRule.FIFTEEN_MINUTES));
        assertEquals(45, calculationService.applyRounding(38, OvertimeRoundingRule.FIFTEEN_MINUTES));
        assertEquals(30, calculationService.applyRounding(40, OvertimeRoundingRule.THIRTY_MINUTES));
        assertEquals(60, calculationService.applyRounding(46, OvertimeRoundingRule.THIRTY_MINUTES));
    }

    @Test
    void testCalculateOvertime_PersistsRecord() {
        when(attendanceRepository.findByIdAndOrganizationId(attId, orgId)).thenReturn(Optional.of(attendance));
        when(policyService.resolveApplicablePolicy(eq(orgId), any(), eq(attendance.getDate()))).thenReturn(policy);
        when(recordRepository.existsByOrganizationIdAndEmployeeIdAndAttendanceId(orgId, empId, attId)).thenReturn(false);
        when(recordRepository.save(any(OvertimeRecord.class))).thenAnswer(inv -> {
            OvertimeRecord rec = inv.getArgument(0);
            rec.setId(101L);
            return rec;
        });

        OvertimeRecordResponse response = calculationService.calculateOvertime(attId);

        assertNotNull(response);
        assertEquals(101L, response.getId());
        assertEquals(OvertimeStatus.CALCULATED, response.getStatus());
        assertEquals(OvertimePayrollStatus.PENDING, response.getPayrollStatus());
        assertEquals(120, response.getCalculatedOtMinutes());
        verify(recordRepository, times(1)).save(any(OvertimeRecord.class));
    }

    @Test
    void testCalculateOvertime_DuplicateRejected() {
        when(attendanceRepository.findByIdAndOrganizationId(attId, orgId)).thenReturn(Optional.of(attendance));
        when(policyService.resolveApplicablePolicy(eq(orgId), any(), eq(attendance.getDate()))).thenReturn(policy);
        when(recordRepository.existsByOrganizationIdAndEmployeeIdAndAttendanceId(orgId, empId, attId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> calculationService.calculateOvertime(attId));
        verify(recordRepository, never()).save(any());
    }

    @Test
    void testAttendanceNotFound_ThrowsResourceNotFound() {
        when(attendanceRepository.findByIdAndOrganizationId(attId, orgId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> calculationService.previewOvertime(attId));
    }
}
