package com.example.ems.performance.service;

import com.example.ems.performance.entity.KpiMeasurementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PerformanceCalculationEngineTest {

    private PerformanceCalculationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new PerformanceCalculationEngine();
    }

    @Test
    @DisplayName("HIGHER_IS_BETTER: normal achievement within bounds")
    void testHigherIsBetter_NormalAchievement() {
        PerformanceCalculationEngine.KpiEvaluationInput input = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_REVENUE",
                KpiMeasurementType.HIGHER_IS_BETTER,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                new BigDecimal("85.00")
        );

        BigDecimal score = engine.normalizeKpiScore(input);
        assertEquals(new BigDecimal("85.00"), score);
    }

    @Test
    @DisplayName("HIGHER_IS_BETTER: over-achievement is clamped to max threshold 100")
    void testHigherIsBetter_OverAchievementClamped() {
        PerformanceCalculationEngine.KpiEvaluationInput input = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_SALES",
                KpiMeasurementType.HIGHER_IS_BETTER,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                new BigDecimal("130.00")
        );

        BigDecimal score = engine.normalizeKpiScore(input);
        assertEquals(new BigDecimal("100.00"), score);
    }

    @Test
    @DisplayName("HIGHER_IS_BETTER: zero or negative target handled safely")
    void testHigherIsBetter_ZeroTarget() {
        PerformanceCalculationEngine.KpiEvaluationInput input = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_NEW",
                KpiMeasurementType.HIGHER_IS_BETTER,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                new BigDecimal("10.00")
        );

        BigDecimal score = engine.normalizeKpiScore(input);
        assertEquals(new BigDecimal("100.00"), score);
    }

    @Test
    @DisplayName("LOWER_IS_BETTER: achieving below target is rewarded with 100%")
    void testLowerIsBetter_UnderTargetRewarded() {
        PerformanceCalculationEngine.KpiEvaluationInput input = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_DEFECTS",
                KpiMeasurementType.LOWER_IS_BETTER,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("8.00")
        );

        BigDecimal score = engine.normalizeKpiScore(input);
        assertEquals(new BigDecimal("100.00"), score);
    }

    @Test
    @DisplayName("LOWER_IS_BETTER: exceeding target is penalized proportionally")
    void testLowerIsBetter_OverTargetPenalized() {
        PerformanceCalculationEngine.KpiEvaluationInput input = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_BUGS",
                KpiMeasurementType.LOWER_IS_BETTER,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                new BigDecimal("12.00") // 20% overrun -> 80% score
        );

        BigDecimal score = engine.normalizeKpiScore(input);
        assertEquals(new BigDecimal("80.00"), score);
    }

    @Test
    @DisplayName("LOWER_IS_BETTER: massive overrun is floored at 0.00")
    void testLowerIsBetter_MassiveOverrunFloored() {
        PerformanceCalculationEngine.KpiEvaluationInput input = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_INCIDENTS",
                KpiMeasurementType.LOWER_IS_BETTER,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                new BigDecimal("5.00"),
                new BigDecimal("25.00") // 400% overrun -> floored at 0
        );

        BigDecimal score = engine.normalizeKpiScore(input);
        assertEquals(new BigDecimal("0.00"), score);
    }

    @Test
    @DisplayName("LOWER_IS_BETTER: zero target with zero actual yields 100.00")
    void testLowerIsBetter_ZeroTargetZeroActual() {
        PerformanceCalculationEngine.KpiEvaluationInput input = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_DOWNTIME",
                KpiMeasurementType.LOWER_IS_BETTER,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        BigDecimal score = engine.normalizeKpiScore(input);
        assertEquals(new BigDecimal("100.00"), score);
    }

    @Test
    @DisplayName("PERCENTAGE: direct achievement clamped to [0, 100]")
    void testPercentage_DirectClamped() {
        PerformanceCalculationEngine.KpiEvaluationInput input = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_COMPLIANCE",
                KpiMeasurementType.PERCENTAGE,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                null,
                new BigDecimal("94.50")
        );

        BigDecimal score = engine.normalizeKpiScore(input);
        assertEquals(new BigDecimal("94.50"), score);
    }

    @Test
    @DisplayName("BOOLEAN: met produces 100, unmet produces 0")
    void testBoolean_TrueAndFalse() {
        PerformanceCalculationEngine.KpiEvaluationInput met = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_CERT",
                KpiMeasurementType.BOOLEAN,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                null,
                BigDecimal.ONE
        );
        assertEquals(new BigDecimal("100.00"), engine.normalizeKpiScore(met));

        PerformanceCalculationEngine.KpiEvaluationInput unmet = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_CERT",
                KpiMeasurementType.BOOLEAN,
                new BigDecimal("50.00"),
                BigDecimal.ZERO,
                new BigDecimal("100.00"),
                null,
                BigDecimal.ZERO
        );
        assertEquals(new BigDecimal("0.00"), engine.normalizeKpiScore(unmet));
    }

    @Test
    @DisplayName("RATING_BASED: 1-5 scale normalized to 0-100")
    void testRatingBased_ScaleNormalization() {
        // 1.0 -> 0.00
        PerformanceCalculationEngine.KpiEvaluationInput star1 = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_PEER", KpiMeasurementType.RATING_BASED, new BigDecimal("50.00"), BigDecimal.ZERO, new BigDecimal("100.00"), null, new BigDecimal("1.00")
        );
        assertEquals(new BigDecimal("0.00"), engine.normalizeKpiScore(star1));

        // 3.0 -> 50.00
        PerformanceCalculationEngine.KpiEvaluationInput star3 = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_PEER", KpiMeasurementType.RATING_BASED, new BigDecimal("50.00"), BigDecimal.ZERO, new BigDecimal("100.00"), null, new BigDecimal("3.00")
        );
        assertEquals(new BigDecimal("50.00"), engine.normalizeKpiScore(star3));

        // 5.0 -> 100.00
        PerformanceCalculationEngine.KpiEvaluationInput star5 = new PerformanceCalculationEngine.KpiEvaluationInput(
                "KPI_PEER", KpiMeasurementType.RATING_BASED, new BigDecimal("50.00"), BigDecimal.ZERO, new BigDecimal("100.00"), null, new BigDecimal("5.00")
        );
        assertEquals(new BigDecimal("100.00"), engine.normalizeKpiScore(star5));
    }

    @Test
    @DisplayName("Level 1 KPI Weighting: Sum equals 100.00 succeeds")
    void testLevel1KpiWeighting_SumEquals100_Succeeds() {
        List<PerformanceCalculationEngine.KpiEvaluationInput> inputs = List.of(
                new PerformanceCalculationEngine.KpiEvaluationInput("KPI_A", KpiMeasurementType.HIGHER_IS_BETTER, new BigDecimal("30.00"), BigDecimal.ZERO, new BigDecimal("100.00"), new BigDecimal("100.00"), new BigDecimal("90.00")),
                new PerformanceCalculationEngine.KpiEvaluationInput("KPI_B", KpiMeasurementType.HIGHER_IS_BETTER, new BigDecimal("40.00"), BigDecimal.ZERO, new BigDecimal("100.00"), new BigDecimal("100.00"), new BigDecimal("80.00")),
                new PerformanceCalculationEngine.KpiEvaluationInput("KPI_C", KpiMeasurementType.LOWER_IS_BETTER, new BigDecimal("30.00"), BigDecimal.ZERO, new BigDecimal("100.00"), new BigDecimal("10.00"), new BigDecimal("8.00"))
        );

        List<PerformanceCalculationEngine.KpiEvaluationResult> results = engine.evaluateKpis(inputs);
        BigDecimal weightedScore = engine.calculateKpiWeightedScore(results);

        // KPI A = 90.00 * 0.30 = 27.00
        // KPI B = 80.00 * 0.40 = 32.00
        // KPI C = 100.00 * 0.30 = 30.00
        // Total = 89.00
        assertEquals(new BigDecimal("89.00"), weightedScore);
    }

    @Test
    @DisplayName("Level 1 KPI Weighting: Sum not equal to 100 throws IllegalArgumentException")
    void testLevel1KpiWeighting_SumNot100_ThrowsException() {
        List<PerformanceCalculationEngine.KpiEvaluationInput> inputs = List.of(
                new PerformanceCalculationEngine.KpiEvaluationInput("KPI_A", KpiMeasurementType.HIGHER_IS_BETTER, new BigDecimal("40.00"), BigDecimal.ZERO, new BigDecimal("100.00"), new BigDecimal("100.00"), new BigDecimal("90.00")),
                new PerformanceCalculationEngine.KpiEvaluationInput("KPI_B", KpiMeasurementType.HIGHER_IS_BETTER, new BigDecimal("40.00"), BigDecimal.ZERO, new BigDecimal("100.00"), new BigDecimal("100.00"), new BigDecimal("80.00"))
        );

        assertThrows(IllegalArgumentException.class, () -> engine.evaluateKpis(inputs));
    }

    @Test
    @DisplayName("Level 2 Component Weighting: Computes composite final score and rating band")
    void testLevel2ComponentWeighting_CorrectFinalScoreAndBand() {
        // KPI: 84.50 (50%), Mgr: 90.00 (30%), Self: 70.00 (10%), Attendance: 95.00 (10%)
        // 84.50 * 0.50 = 42.25
        // 90.00 * 0.30 = 27.00
        // 70.00 * 0.10 = 7.00
        // 95.00 * 0.10 = 9.50
        // Final Score = 85.75 -> EXCEEDS_EXPECTATIONS
        PerformanceCalculationEngine.OverallCalculationResult result = engine.calculateOverallScore(
                new BigDecimal("84.50"),
                new BigDecimal("90.00"),
                new BigDecimal("70.00"),
                new BigDecimal("95.00"),
                new BigDecimal("50.00"),
                new BigDecimal("30.00"),
                new BigDecimal("10.00"),
                new BigDecimal("10.00"),
                List.of()
        );

        assertEquals(new BigDecimal("85.75"), result.getFinalScore());
        assertEquals("EXCEEDS_EXPECTATIONS", result.getRatingBand());
    }

    @Test
    @DisplayName("Rating Band Tiers: maps scores accurately")
    void testRatingBandTiers() {
        assertEquals("OUTSTANDING", engine.determineRatingBand(new BigDecimal("92.00")));
        assertEquals("EXCEEDS_EXPECTATIONS", engine.determineRatingBand(new BigDecimal("80.00")));
        assertEquals("MEETS_EXPECTATIONS", engine.determineRatingBand(new BigDecimal("65.00")));
        assertEquals("NEEDS_IMPROVEMENT", engine.determineRatingBand(new BigDecimal("48.00")));
        assertEquals("UNSATISFACTORY", engine.determineRatingBand(new BigDecimal("30.00")));
    }
}
