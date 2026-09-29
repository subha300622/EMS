package com.example.ems.performance.service;

import com.example.ems.performance.entity.KpiMeasurementType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class PerformanceCalculationEngine {

    public static final BigDecimal HUNDRED = new BigDecimal("100.00");
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    public static class KpiEvaluationInput {
        private String kpiCode;
        private KpiMeasurementType measurementType;
        private BigDecimal weight;
        private BigDecimal minThreshold;
        private BigDecimal maxThreshold;
        private BigDecimal targetValue;
        private BigDecimal actualValue;

        public KpiEvaluationInput(String kpiCode, KpiMeasurementType measurementType, BigDecimal weight,
                                  BigDecimal minThreshold, BigDecimal maxThreshold, BigDecimal targetValue,
                                  BigDecimal actualValue) {
            this.kpiCode = kpiCode;
            this.measurementType = measurementType;
            this.weight = weight != null ? weight : BigDecimal.ZERO;
            this.minThreshold = minThreshold != null ? minThreshold : BigDecimal.ZERO;
            this.maxThreshold = maxThreshold != null ? maxThreshold : HUNDRED;
            this.targetValue = targetValue;
            this.actualValue = actualValue != null ? actualValue : BigDecimal.ZERO;
        }

        public String getKpiCode() { return kpiCode; }
        public KpiMeasurementType getMeasurementType() { return measurementType; }
        public BigDecimal getWeight() { return weight; }
        public BigDecimal getMinThreshold() { return minThreshold; }
        public BigDecimal getMaxThreshold() { return maxThreshold; }
        public BigDecimal getTargetValue() { return targetValue; }
        public BigDecimal getActualValue() { return actualValue; }
    }

    public static class KpiEvaluationResult {
        private String kpiCode;
        private BigDecimal weight;
        private BigDecimal normalizedScore;
        private BigDecimal weightedContribution;

        public KpiEvaluationResult(String kpiCode, BigDecimal weight, BigDecimal normalizedScore, BigDecimal weightedContribution) {
            this.kpiCode = kpiCode;
            this.weight = weight;
            this.normalizedScore = normalizedScore;
            this.weightedContribution = weightedContribution;
        }

        public String getKpiCode() { return kpiCode; }
        public BigDecimal getWeight() { return weight; }
        public BigDecimal getNormalizedScore() { return normalizedScore; }
        public BigDecimal getWeightedContribution() { return weightedContribution; }
    }

    public static class OverallCalculationResult {
        private BigDecimal kpiWeightedScore;
        private BigDecimal managerScore;
        private BigDecimal selfScore;
        private BigDecimal attendanceScore;
        private BigDecimal finalScore;
        private String ratingBand;
        private List<KpiEvaluationResult> kpiResults;

        public OverallCalculationResult(BigDecimal kpiWeightedScore, BigDecimal managerScore, BigDecimal selfScore,
                                        BigDecimal attendanceScore, BigDecimal finalScore, String ratingBand,
                                        List<KpiEvaluationResult> kpiResults) {
            this.kpiWeightedScore = kpiWeightedScore;
            this.managerScore = managerScore;
            this.selfScore = selfScore;
            this.attendanceScore = attendanceScore;
            this.finalScore = finalScore;
            this.ratingBand = ratingBand;
            this.kpiResults = kpiResults;
        }

        public BigDecimal getKpiWeightedScore() { return kpiWeightedScore; }
        public BigDecimal getManagerScore() { return managerScore; }
        public BigDecimal getSelfScore() { return selfScore; }
        public BigDecimal getAttendanceScore() { return attendanceScore; }
        public BigDecimal getFinalScore() { return finalScore; }
        public String getRatingBand() { return ratingBand; }
        public List<KpiEvaluationResult> getKpiResults() { return kpiResults; }
    }

    // ── LEVEL 1: KPI NORMALIZATION ──────────────────────────────────────────
    public BigDecimal normalizeKpiScore(KpiEvaluationInput input) {
        if (input == null) return ZERO;
        KpiMeasurementType type = input.getMeasurementType() != null ? input.getMeasurementType() : KpiMeasurementType.HIGHER_IS_BETTER;
        BigDecimal actual = input.getActualValue() != null ? input.getActualValue() : BigDecimal.ZERO;
        BigDecimal target = input.getTargetValue();
        BigDecimal min = input.getMinThreshold() != null ? input.getMinThreshold() : BigDecimal.ZERO;
        BigDecimal max = input.getMaxThreshold() != null ? input.getMaxThreshold() : HUNDRED;

        BigDecimal rawScore;

        switch (type) {
            case HIGHER_IS_BETTER:
                if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) {
                    rawScore = actual.compareTo(BigDecimal.ZERO) > 0 ? HUNDRED : ZERO;
                } else {
                    rawScore = actual.divide(target, 6, RoundingMode.HALF_UP).multiply(HUNDRED);
                }
                break;

            case LOWER_IS_BETTER:
                if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) {
                    rawScore = actual.compareTo(BigDecimal.ZERO) <= 0 ? HUNDRED : ZERO;
                } else if (actual.compareTo(target) <= 0) {
                    rawScore = HUNDRED;
                } else {
                    BigDecimal overrun = actual.subtract(target).divide(target, 6, RoundingMode.HALF_UP).multiply(HUNDRED);
                    rawScore = HUNDRED.subtract(overrun).max(BigDecimal.ZERO);
                }
                break;

            case PERCENTAGE:
                rawScore = actual.max(BigDecimal.ZERO);
                break;

            case BOOLEAN:
                rawScore = actual.compareTo(BigDecimal.ONE) >= 0 ? HUNDRED : ZERO;
                break;

            case RATING_BASED:
                // Map 1.0 - 5.0 to 0.0 - 100.0
                if (actual.compareTo(BigDecimal.ONE) <= 0) {
                    rawScore = ZERO;
                } else if (actual.compareTo(new BigDecimal("5.00")) >= 0) {
                    rawScore = HUNDRED;
                } else {
                    rawScore = actual.subtract(BigDecimal.ONE).divide(new BigDecimal("4.00"), 6, RoundingMode.HALF_UP).multiply(HUNDRED);
                }
                break;

            default:
                rawScore = ZERO;
        }

        // Bound to [min, max] and [0, 100]
        BigDecimal clamped = rawScore.max(min).min(max).max(BigDecimal.ZERO).min(HUNDRED);
        return clamped.setScale(2, RoundingMode.HALF_UP);
    }

    public List<KpiEvaluationResult> evaluateKpis(List<KpiEvaluationInput> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            throw new IllegalArgumentException("Cannot evaluate KPIs: KPI inputs list cannot be null or empty.");
        }

        BigDecimal totalWeight = BigDecimal.ZERO;
        for (KpiEvaluationInput input : inputs) {
            totalWeight = totalWeight.add(input.getWeight());
        }

        if (totalWeight.subtract(HUNDRED).abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new IllegalArgumentException("Invalid KPI weights configuration: Total weight must equal 100.00%, but was " + totalWeight + "%");
        }

        return inputs.stream().map(input -> {
            BigDecimal score = normalizeKpiScore(input);
            BigDecimal weightedContribution = score.multiply(input.getWeight()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
            return new KpiEvaluationResult(input.getKpiCode(), input.getWeight(), score, weightedContribution);
        }).toList();
    }

    public BigDecimal calculateKpiWeightedScore(List<KpiEvaluationResult> results) {
        BigDecimal totalScore = BigDecimal.ZERO;
        for (KpiEvaluationResult res : results) {
            totalScore = totalScore.add(res.getWeightedContribution());
        }
        return totalScore.setScale(2, RoundingMode.HALF_UP).min(HUNDRED).max(ZERO);
    }

    public BigDecimal normalizeFivePointScore(BigDecimal score) {
        if (score == null) return ZERO;
        if (score.compareTo(BigDecimal.ZERO) <= 0) return ZERO;
        if (score.compareTo(new BigDecimal("5.00")) <= 0) {
            return score.divide(new BigDecimal("5.00"), 4, RoundingMode.HALF_UP)
                    .multiply(HUNDRED)
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return score.setScale(2, RoundingMode.HALF_UP).min(HUNDRED).max(ZERO);
    }

    // ── LEVEL 2: OVERALL COMPONENT WEIGHTING ────────────────────────────────
    public OverallCalculationResult calculateOverallScore(
            BigDecimal kpiScore,
            BigDecimal managerScore,
            BigDecimal selfScore,
            BigDecimal attendanceScore,
            BigDecimal kpiWeight,
            BigDecimal managerWeight,
            BigDecimal selfWeight,
            BigDecimal attendanceWeight,
            List<KpiEvaluationResult> kpiResults) {

        BigDecimal kw = kpiWeight != null ? kpiWeight : new BigDecimal("50.00");
        BigDecimal mw = managerWeight != null ? managerWeight : new BigDecimal("30.00");
        BigDecimal sw = selfWeight != null ? selfWeight : new BigDecimal("10.00");
        BigDecimal aw = attendanceWeight != null ? attendanceWeight : new BigDecimal("10.00");

        BigDecimal totalComponentWeight = kw.add(mw).add(sw).add(aw);
        if (totalComponentWeight.subtract(HUNDRED).abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new IllegalArgumentException("Invalid component weights configuration: Level-2 weights must sum to 100.00%, but was " + totalComponentWeight + "%");
        }

        BigDecimal safeKpi = (kpiScore != null ? kpiScore : ZERO).min(HUNDRED).max(ZERO);
        BigDecimal safeMgr = normalizeFivePointScore(managerScore);
        BigDecimal safeSelf = normalizeFivePointScore(selfScore);
        BigDecimal safeAtt = (attendanceScore != null ? attendanceScore : ZERO).min(HUNDRED).max(ZERO);

        BigDecimal weightedKpi = safeKpi.multiply(kw).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        BigDecimal weightedMgr = safeMgr.multiply(mw).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        BigDecimal weightedSelf = safeSelf.multiply(sw).divide(HUNDRED, 4, RoundingMode.HALF_UP);
        BigDecimal weightedAtt = safeAtt.multiply(aw).divide(HUNDRED, 4, RoundingMode.HALF_UP);

        BigDecimal finalScore = weightedKpi.add(weightedMgr).add(weightedSelf).add(weightedAtt)
                .setScale(2, RoundingMode.HALF_UP).min(HUNDRED).max(ZERO);

        String ratingBand = determineRatingBand(finalScore);

        return new OverallCalculationResult(safeKpi, safeMgr, safeSelf, safeAtt, finalScore, ratingBand, kpiResults);
    }

    public String determineRatingBand(BigDecimal finalScore) {
        if (finalScore == null) return "UNSATISFACTORY";
        if (finalScore.compareTo(new BigDecimal("90.00")) >= 0) return "OUTSTANDING";
        if (finalScore.compareTo(new BigDecimal("75.00")) >= 0) return "EXCEEDS_EXPECTATIONS";
        if (finalScore.compareTo(new BigDecimal("60.00")) >= 0) return "MEETS_EXPECTATIONS";
        if (finalScore.compareTo(new BigDecimal("45.00")) >= 0) return "NEEDS_IMPROVEMENT";
        return "UNSATISFACTORY";
    }
}
