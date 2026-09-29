package com.example.ems.offboarding.service;

import com.example.ems.offboarding.dto.FnfCalculationRequest;
import com.example.ems.offboarding.dto.FnfSnapshotDto;
import com.example.ems.offboarding.entity.EmployeeExit;
import com.example.ems.offboarding.entity.FnfSettlement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

@Service
public class FnfSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(FnfSnapshotService.class);
    private final ObjectMapper canonicalMapper;

    public FnfSnapshotService() {
        this.canonicalMapper = new ObjectMapper();
        this.canonicalMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public static class SnapshotResult {
        private final String json;
        private final String hash;
        private final FnfSnapshotDto dto;

        public SnapshotResult(String json, String hash, FnfSnapshotDto dto) {
            this.json = json;
            this.hash = hash;
            this.dto = dto;
        }

        public String getJson() {
            return json;
        }

        public String getHash() {
            return hash;
        }

        public FnfSnapshotDto getDto() {
            return dto;
        }
    }

    public SnapshotResult captureSnapshot(EmployeeExit exit, FnfCalculationRequest request, BigDecimal salaryAmount) {
        FnfSnapshotDto dto = new FnfSnapshotDto();

        // 1. Salary
        FnfSnapshotDto.SalarySnapshot salary = new FnfSnapshotDto.SalarySnapshot();
        if (exit.getEmployee() != null) {
            BigDecimal annualSalary = exit.getEmployee().getAnnualSalary() != null
                    ? exit.getEmployee().getAnnualSalary()
                    : BigDecimal.ZERO;
            salary.setAnnualSalary(annualSalary);
            BigDecimal monthlyBase = annualSalary.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            salary.setMonthlyBase(monthlyBase);

            LocalDate lwd = exit.getLastWorkingDate() != null ? exit.getLastWorkingDate()
                    : (exit.getRequestedLastWorkingDate() != null ? exit.getRequestedLastWorkingDate()
                            : LocalDate.now());
            YearMonth ym = YearMonth.from(lwd);
            int daysInMonth = ym.lengthOfMonth();
            salary.setDaysInMonth(daysInMonth);
            salary.setWorkedDays(request.getSalaryDaysWorked());

            if (daysInMonth > 0) {
                BigDecimal dailyRate = monthlyBase.divide(BigDecimal.valueOf(daysInMonth), 4, RoundingMode.HALF_UP);
                salary.setDailyRate(dailyRate);
            }
            salary.setProRatedSalary(salaryAmount);
        }
        dto.setSalary(salary);

        // 2. Leave
        FnfSnapshotDto.LeaveSnapshot leave = new FnfSnapshotDto.LeaveSnapshot();
        leave.setLeaveEncashmentAmount(request.getLeaveEncashment());
        leave.setDailyRate(salary.getDailyRate());
        if (salary.getDailyRate() != null && salary.getDailyRate().compareTo(BigDecimal.ZERO) > 0
                && request.getLeaveEncashment() != null) {
            leave.setEncashableDays(
                    request.getLeaveEncashment().divide(salary.getDailyRate(), 1, RoundingMode.HALF_UP).doubleValue());
        } else {
            leave.setEncashableDays(0.0);
        }
        dto.setLeave(leave);

        // 3. Assets
        FnfSnapshotDto.AssetSnapshot assets = new FnfSnapshotDto.AssetSnapshot();
        assets.setDeductionAmount(request.getAssetDamage() != null ? request.getAssetDamage() : BigDecimal.ZERO);
        assets.setClearanceReference("EXIT-CLR-" + exit.getId());
        dto.setAssets(assets);

        // 4. Notice
        FnfSnapshotDto.NoticeSnapshot notice = new FnfSnapshotDto.NoticeSnapshot();
        notice.setRequiredNoticeDays(exit.getNoticePeriodDays() != null ? exit.getNoticePeriodDays() : 30);
        notice.setServedNoticeDays(exit.getNoticeServedDays() != null ? exit.getNoticeServedDays() : 30);
        int shortfall = Math.max(0, notice.getRequiredNoticeDays() - notice.getServedNoticeDays());
        notice.setShortfallDays(shortfall);
        notice.setNoticeRecoveryAmount(
                request.getNoticePeriodRecovery() != null ? request.getNoticePeriodRecovery() : BigDecimal.ZERO);
        dto.setNotice(notice);

        // 5. Variable Pay
        FnfSnapshotDto.VariablePaySnapshot variablePay = new FnfSnapshotDto.VariablePaySnapshot();
        variablePay.setBonus(request.getBonus() != null ? request.getBonus() : BigDecimal.ZERO);
        variablePay.setIncentives(request.getIncentives() != null ? request.getIncentives() : BigDecimal.ZERO);
        variablePay.setOvertime(request.getOvertime() != null ? request.getOvertime() : BigDecimal.ZERO);
        dto.setVariablePay(variablePay);

        // 6. Expenses
        FnfSnapshotDto.ExpenseSnapshot expenses = new FnfSnapshotDto.ExpenseSnapshot();
        expenses.setPendingReimbursements(
                request.getReimbursements() != null ? request.getReimbursements() : BigDecimal.ZERO);
        dto.setExpenses(expenses);

        // 7. Deductions
        FnfSnapshotDto.DeductionSnapshot deductions = new FnfSnapshotDto.DeductionSnapshot();
        deductions.setLoanRecovery(BigDecimal.ZERO);
        deductions.setTaxDeduction(request.getTaxDeduction() != null ? request.getTaxDeduction() : BigDecimal.ZERO);
        deductions.setOtherDeductions(
                request.getOtherDeductions() != null ? request.getOtherDeductions() : BigDecimal.ZERO);
        dto.setDeductions(deductions);

        // 8. Traceable References
        Map<String, Object> refs = new HashMap<>();
        refs.put("exitId", exit.getId());
        if (exit.getEmployee() != null) {
            refs.put("employeeId", exit.getEmployee().getId());
            refs.put("employeeCode", exit.getEmployee().getEmployeeId());
        }
        refs.put("capturedAt", LocalDateTime.now().toString());
        dto.setSourceReferences(refs);

        // Serialize & Hash
        try {
            String json = canonicalMapper.writeValueAsString(dto);
            String hash = calculateSha256(json);
            return new SnapshotResult(json, hash, dto);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize F&F canonical snapshot: {}", e.getMessage());
            throw new IllegalStateException("Failed to capture F&F calculation snapshot", e);
        }
    }

    public FnfSnapshotDto captureSnapshot(EmployeeExit exit, FnfSettlement settlement) {
        FnfCalculationRequest req = new FnfCalculationRequest();
        req.setSalaryDaysWorked(settlement.getSalaryDaysWorked() != null ? settlement.getSalaryDaysWorked() : 0);
        req.setUnpaidSalary(settlement.getUnpaidSalary() != null ? settlement.getUnpaidSalary() : BigDecimal.ZERO);
        req.setLeaveEncashment(
                settlement.getLeaveEncashment() != null ? settlement.getLeaveEncashment() : BigDecimal.ZERO);
        req.setBonus(settlement.getBonus() != null ? settlement.getBonus() : BigDecimal.ZERO);
        req.setIncentives(settlement.getIncentives() != null ? settlement.getIncentives() : BigDecimal.ZERO);
        req.setOvertime(settlement.getOvertime() != null ? settlement.getOvertime() : BigDecimal.ZERO);
        req.setReimbursements(
                settlement.getReimbursements() != null ? settlement.getReimbursements() : BigDecimal.ZERO);
        req.setGratuity(settlement.getGratuity() != null ? settlement.getGratuity() : BigDecimal.ZERO);
        req.setOtherAllowances(
                settlement.getOtherAllowances() != null ? settlement.getOtherAllowances() : BigDecimal.ZERO);
        req.setNoticePeriodRecovery(
                settlement.getNoticePeriodRecovery() != null ? settlement.getNoticePeriodRecovery() : BigDecimal.ZERO);
        req.setAssetDamage(settlement.getAssetDamage() != null ? settlement.getAssetDamage() : BigDecimal.ZERO);
        req.setLoanRecovery(settlement.getLoanRecovery() != null ? settlement.getLoanRecovery() : BigDecimal.ZERO);
        req.setTaxDeduction(settlement.getTaxDeduction() != null ? settlement.getTaxDeduction() : BigDecimal.ZERO);
        req.setOtherDeductions(
                settlement.getOtherDeductions() != null ? settlement.getOtherDeductions() : BigDecimal.ZERO);

        return captureSnapshot(exit, req,
                settlement.getSalaryAmount() != null ? settlement.getSalaryAmount() : BigDecimal.ZERO).getDto();
    }

    public String serializeToJson(FnfSnapshotDto dto) {
        try {
            return canonicalMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize FnfSnapshotDto", e);
        }
    }

    public FnfSnapshotDto deserializeFromJson(String json) {
        if (json == null || json.isBlank())
            return null;
        try {
            return canonicalMapper.readValue(json, FnfSnapshotDto.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize FnfSnapshotDto: {}", e.getMessage());
            return null;
        }
    }

    public String computeCanonicalHash(FnfSnapshotDto dto) {
        String json = serializeToJson(dto);
        return calculateSha256(json);
    }

    public boolean verifyIntegrity(String json, String expectedHash) {
        if (json == null || expectedHash == null) return false;
        try {
            FnfSnapshotDto dto = deserializeFromJson(json);
            if (dto != null) {
                String canonicalJson = canonicalMapper.writeValueAsString(dto);
                String calculated = calculateSha256(canonicalJson);
                if (expectedHash.equalsIgnoreCase(calculated)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Canonical normalization failed during integrity check: {}", e.getMessage());
        }
        String calculated = calculateSha256(json);
        return expectedHash.equalsIgnoreCase(calculated);
    }
                

    public String calculateSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}