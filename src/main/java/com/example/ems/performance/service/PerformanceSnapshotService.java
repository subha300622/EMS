package com.example.ems.performance.service;

import com.example.ems.employee.entity.Employee;
import com.example.ems.goal.repository.GoalRepository;
import com.example.ems.performance.dto.PerformanceSnapshotDto;
import com.example.ems.performance.entity.PerformanceReviewCycle;
import com.example.ems.performance.entity.PerformanceReviewKpiScore;
import com.example.ems.performance.entity.PerformanceReviewRecord;
import com.example.ems.performance.repository.PerformanceReviewKpiScoreRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class PerformanceSnapshotService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceSnapshotService.class);
    private final ObjectMapper canonicalMapper;

    @Autowired(required = false)
    private GoalRepository goalRepository;

    @Autowired
    private PerformanceReviewKpiScoreRepository kpiScoreRepository;

    public PerformanceSnapshotService() {
        this.canonicalMapper = new ObjectMapper();
        this.canonicalMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.canonicalMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.canonicalMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    public static class SnapshotResult {
        private final String json;
        private final String hash;
        private final PerformanceSnapshotDto dto;

        public SnapshotResult(String json, String hash, PerformanceSnapshotDto dto) {
            this.json = json;
            this.hash = hash;
            this.dto = dto;
        }

        public String getJson() { return json; }
        public String getHash() { return hash; }
        public PerformanceSnapshotDto getDto() { return dto; }
    }

    public SnapshotResult captureSnapshot(PerformanceReviewRecord review) {
        PerformanceSnapshotDto dto = new PerformanceSnapshotDto();
        Employee emp = review.getEmployee();
        PerformanceReviewCycle cycle = review.getCycle();
        Long orgId = review.getOrganization().getId();

        // 1. Employee
        if (emp != null) {
            dto.getEmployee().setEmployeeId(emp.getId());
            dto.getEmployee().setEmployeeCode(emp.getEmployeeId());
            dto.getEmployee().setName(emp.getFullName());
            dto.getEmployee().setDepartment(emp.getDepartment());
            dto.getEmployee().setDesignation(emp.getDesignation());
        }

        // 2. Cycle
        if (cycle != null) {
            dto.getCycle().setCycleId(cycle.getId());
            dto.getCycle().setCode(cycle.getCode());
            dto.getCycle().setStartDate(cycle.getStartDate());
            dto.getCycle().setEndDate(cycle.getEndDate());
            dto.getCycle().setCalculationVersion(review.getCalculationVersion());
            dto.getCycle().setFormulaVersion(cycle.getFormulaVersion());
        }

        // 3. Attendance
        LocalDate start = cycle != null ? cycle.getStartDate() : LocalDate.now().minusMonths(3);
        LocalDate end = cycle != null ? cycle.getEndDate() : LocalDate.now();
        int workingDays = Math.max(1, (int) ChronoUnit.DAYS.between(start, end));
        int leaves = review.getLeavesTaken() != null ? review.getLeavesTaken() : 0;
        int presentDays = Math.max(0, workingDays - leaves);
        BigDecimal attPct = review.getAttendancePercentage() != null ? review.getAttendancePercentage()
                : BigDecimal.valueOf(presentDays).divide(BigDecimal.valueOf(workingDays), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal attScore = review.getAttendanceScore() != null ? review.getAttendanceScore() : attPct;

        dto.getAttendance().setSourceVersion("attendance-v1");
        dto.getAttendance().setWorkingDays(workingDays);
        dto.getAttendance().setPresentDays(presentDays);
        dto.getAttendance().setAttendancePercentage(attPct);
        dto.getAttendance().setAttendanceScore(attScore);

        // 4. Leave
        dto.getLeave().setSourceVersion("leave-v1");
        dto.getLeave().setTotalLeavesTaken(leaves);
        dto.getLeave().setUnpaidLeaves(0);

        // 5. Goals
        try {
            if (goalRepository != null && emp != null) {
                var goals = goalRepository.findByOrganizationIdAndOwnerIdAndIsDeletedFalse(orgId, emp.getId());
                if (goals != null) {
                    for (var g : goals) {
                        PerformanceSnapshotDto.GoalSnapshotItem item = new PerformanceSnapshotDto.GoalSnapshotItem();
                        item.setGoalId(g.getId());
                        item.setTitle(g.getGoalName());
                        item.setProgressPercent(g.getProgress());
                        item.setStatus(g.getStatus());
                        dto.getGoals().add(item);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Goal snapshot collection fallback: {}", e.getMessage());
        }

        // 6. KPIs
        List<PerformanceReviewKpiScore> kpiScores = kpiScoreRepository.findByOrganizationIdAndReviewId(orgId, review.getId());
        for (PerformanceReviewKpiScore score : kpiScores) {
            PerformanceSnapshotDto.KpiSnapshotItem item = new PerformanceSnapshotDto.KpiSnapshotItem();
            item.setKpiCode(score.getKpi().getCode());
            item.setMeasurementType(score.getKpi().getMeasurementType());
            item.setWeight(score.getWeight());
            item.setTargetValue(score.getTargetValue());
            item.setActualValue(score.getActualValue());
            item.setNormalizedScore(score.getNormalizedScore());
            dto.getKpis().add(item);
        }

        String json = toCanonicalJson(dto);
        String hash = calculateSha256(json);
        return new SnapshotResult(json, hash, dto);
    }

    public String serializeToJson(PerformanceSnapshotDto dto) {
        return toCanonicalJson(dto);
    }

    public PerformanceSnapshotDto deserializeFromJson(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return canonicalMapper.readValue(json, PerformanceSnapshotDto.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize PerformanceSnapshotDto: {}", e.getMessage());
            return null;
        }
    }

    public String computeCanonicalHash(PerformanceSnapshotDto dto) {
        String json = toCanonicalJson(dto);
        return calculateSha256(json);
    }

    public boolean verifyIntegrity(String json, String expectedHash) {
        if (json == null || expectedHash == null) return false;
        try {
            PerformanceSnapshotDto dto = deserializeFromJson(json);
            if (dto != null) {
                String canonicalJson = toCanonicalJson(dto);
                String calculated = calculateSha256(canonicalJson);
                if (expectedHash.equalsIgnoreCase(calculated)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug("Canonical normalization failed during integrity check: {}", e.getMessage());
        }
        String directCalc = calculateSha256(json);
        return expectedHash.equalsIgnoreCase(directCalc);
    }

    /**
     * Produces deterministic canonical JSON:
     * 1. Object properties sorted recursively
     * 2. Numeric scale stripped (e.g. 96.92 and 96.920 normalize to 96.92)
     * 3. Arrays preserve order
     * 4. Compact formatting without insignificant whitespace
     */
    public String toCanonicalJson(Object obj) {
        try {
            JsonNode tree = canonicalMapper.valueToTree(obj);
            JsonNode normalized = normalizeNode(tree);
            return canonicalMapper.writeValueAsString(normalized);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to produce canonical JSON", e);
        }
    }

    private JsonNode normalizeNode(JsonNode node) {
        if (node == null || node.isNull()) {
            return NullNode.getInstance();
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Map<String, JsonNode> sortedMap = new TreeMap<>();
            for (Map.Entry<String, JsonNode> entry : objectNode.properties()) {
                sortedMap.put(entry.getKey(), normalizeNode(entry.getValue()));
            }
            ObjectNode normalizedObj = canonicalMapper.createObjectNode();
            for (Map.Entry<String, JsonNode> entry : sortedMap.entrySet()) {
                normalizedObj.set(entry.getKey(), entry.getValue());
            }
            return normalizedObj;
        }
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            ArrayNode normalizedArr = canonicalMapper.createArrayNode();
            for (JsonNode item : arrayNode) {
                normalizedArr.add(normalizeNode(item));
            }
            return normalizedArr;
        }
        if (node.isNumber()) {
            BigDecimal bd = node.decimalValue().stripTrailingZeros();
            return new DecimalNode(bd);
        }
        return node;
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
