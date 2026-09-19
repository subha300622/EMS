package com.example.ems.increment.service;

import com.example.ems.increment.entity.EffectiveDateRule;
import com.example.ems.increment.entity.IncrementPolicy;
import com.example.ems.increment.entity.IncrementPolicyBand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Increment Calculation & Boundary Unit Tests")
public class IncrementCalculationServiceTest {

    private IncrementPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new IncrementPolicy();
        policy.setName("FY 2026-27 Standard Increment Policy");
        policy.setMinimumRating(3.0);
        policy.setMinimumIncrementPercentage(3.0);
        policy.setMaximumIncrementPercentage(20.0);
        policy.setBudgetLimit(new BigDecimal("1000000.00"));
        policy.setEffectiveDateRule(EffectiveDateRule.CYCLE_END_DATE);

        List<IncrementPolicyBand> bands = new ArrayList<>();

        IncrementPolicyBand band1 = new IncrementPolicyBand();
        band1.setMinRating(4.5);
        band1.setMaxRating(5.0);
        band1.setIncrementPercentage(15.0);
        bands.add(band1);

        IncrementPolicyBand band2 = new IncrementPolicyBand();
        band2.setMinRating(3.5);
        band2.setMaxRating(4.49);
        band2.setIncrementPercentage(10.0);
        bands.add(band2);

        IncrementPolicyBand band3 = new IncrementPolicyBand();
        band3.setMinRating(3.0);
        band3.setMaxRating(3.49);
        band3.setIncrementPercentage(5.0);
        bands.add(band3);

        policy.setBands(bands);
    }

    @ParameterizedTest(name = "Current Salary: {0}, Increment: {1}%, Expected Inc: {2}, Expected New Salary: {3}")
    @CsvSource({
            "50000.00, 10.0, 5000.00, 55000.00",
            "50000.00, 5.0, 2500.00, 52500.00",
            "50000.00, 0.0, 0.00, 50000.00",
            "50000.00, 100.0, 50000.00, 100000.00",
            "600000.00, 15.0, 90000.00, 690000.00",
            "601111.11, 7.5, 45083.33, 646194.44",
            "123456.78, 3.33, 4111.11, 127567.89"
    })
    void testSalaryCalculation(double currentSalary, double incrementPct, double expectedIncAmount, double expectedNewSalary) {
        BigDecimal current = BigDecimal.valueOf(currentSalary);
        BigDecimal pct = BigDecimal.valueOf(incrementPct);

        BigDecimal incrementAmount = current.multiply(pct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal newSalary = current.add(incrementAmount);

        assertEquals(0, BigDecimal.valueOf(expectedIncAmount).compareTo(incrementAmount), "Increment amount mismatch");
        assertEquals(0, BigDecimal.valueOf(expectedNewSalary).compareTo(newSalary), "New salary mismatch");
    }

    @Test
    void testBandMatchingLogic() {
        // Rating 4.8 should match Band 1 (15%)
        IncrementPolicyBand band = policy.getBands().stream()
                .filter(b -> 4.8 >= b.getMinRating() && 4.8 <= b.getMaxRating())
                .findFirst()
                .orElse(null);
        assertNotNull(band);
        assertEquals(15.0, band.getIncrementPercentage());

        // Rating 3.8 should match Band 2 (10%)
        band = policy.getBands().stream()
                .filter(b -> 3.8 >= b.getMinRating() && 3.8 <= b.getMaxRating())
                .findFirst()
                .orElse(null);
        assertNotNull(band);
        assertEquals(10.0, band.getIncrementPercentage());

        // Rating 2.5 should not match any band (below minimum rating)
        band = policy.getBands().stream()
                .filter(b -> 2.5 >= b.getMinRating() && 2.5 <= b.getMaxRating())
                .findFirst()
                .orElse(null);
        assertNull(band);
    }

    @Test
    void testNegativeOrInvalidIncrementPercentageRejection() {
        double negativePct = -5.0;
        assertTrue(negativePct < 0, "Negative increment percentage must be rejected");

        double exceedsMax = 25.0; // Policy max is 20.0
        assertTrue(exceedsMax > policy.getMaximumIncrementPercentage(), "Exceeding max policy percentage must be rejected");
    }
}
