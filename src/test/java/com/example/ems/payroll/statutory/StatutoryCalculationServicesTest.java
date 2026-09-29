package com.example.ems.payroll.statutory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class StatutoryCalculationServicesTest {

    private PfCalculationService pfService;
    private EsiCalculationService esiService;
    private ProfessionalTaxService ptService;
    private TdsCalculationService tdsService;

    @BeforeEach
    void setUp() {
        pfService = new PfCalculationService();
        esiService = new EsiCalculationService();
        ptService = new ProfessionalTaxService();
        tdsService = new TdsCalculationService();
    }

    // ── PF TESTS ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("PF: Basic below statutory ceiling (₹10,000) calculates exact 12% employee and EPF/EPS breakdown")
    void testPfBelowCeiling() {
        PfCalculationResult result = pfService.calculatePf(BigDecimal.valueOf(10000));

        assertTrue(result.isApplicable());
        assertEquals(0, new BigDecimal("10000.00").compareTo(result.getWageBase()));
        assertEquals(0, new BigDecimal("1200.00").compareTo(result.getEmployeeContribution())); // 10000 * 12%
        assertEquals(0, new BigDecimal("833.00").compareTo(result.getEmployerEpsContribution()));  // 10000 * 8.33%
        assertEquals(0, new BigDecimal("367.00").compareTo(result.getEmployerEpfContribution()));  // 1200 - 833
        assertEquals(0, new BigDecimal("1200.00").compareTo(result.getTotalEmployerContribution()));
    }

    @Test
    @DisplayName("PF: Basic above statutory ceiling (₹50,000) capped at ₹15,000 with EPS capped at ₹1,250")
    void testPfAboveCeiling() {
        PfCalculationResult result = pfService.calculatePf(BigDecimal.valueOf(50000));

        assertTrue(result.isApplicable());
        assertEquals(0, new BigDecimal("15000.00").compareTo(result.getWageBase()));
        assertEquals(0, new BigDecimal("1800.00").compareTo(result.getEmployeeContribution())); // 15000 * 12%
        assertEquals(0, new BigDecimal("1249.50").compareTo(result.getEmployerEpsContribution())); // 15000 * 8.33% = 1249.50 (<= 1250)
        assertEquals(0, new BigDecimal("550.50").compareTo(result.getEmployerEpfContribution()));
        assertEquals(0, new BigDecimal("1800.00").compareTo(result.getTotalEmployerContribution()));
    }

    @Test
    @DisplayName("PF: Uncapped policy calculates on full Basic wage")
    void testPfUncappedPolicy() {
        PfCalculationResult result = pfService.calculatePf(BigDecimal.valueOf(50000), false, null);

        assertTrue(result.isApplicable());
        assertEquals(0, new BigDecimal("50000.00").compareTo(result.getWageBase()));
        assertEquals(0, new BigDecimal("6000.00").compareTo(result.getEmployeeContribution())); // 50000 * 12%
        assertEquals(0, new BigDecimal("6000.00").compareTo(result.getTotalEmployerContribution()));
    }

    // ── ESI TESTS ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("ESI: Gross ₹20,000 (<= ₹21,000) calculates 0.75% employee and 3.25% employer contribution")
    void testEsiBelowCeiling() {
        EsiCalculationResult result = esiService.calculateEsi(BigDecimal.valueOf(20000), 22);

        assertTrue(result.isApplicable());
        assertEquals("COVERED_STANDARD", result.getReason());
        assertEquals(0, new BigDecimal("150.00").compareTo(result.getEmployeeContribution())); // 20000 * 0.75%
        assertEquals(0, new BigDecimal("650.00").compareTo(result.getEmployerContribution())); // 20000 * 3.25%
        assertFalse(result.isLowWageExempt());
    }

    @Test
    @DisplayName("ESI: Gross ₹50,000 (> ₹21,000 ceiling) marks employee exempt as WAGES_ABOVE_CEILING")
    void testEsiAboveCeilingExempt() {
        EsiCalculationResult result = esiService.calculateEsi(BigDecimal.valueOf(50000), 22);

        assertFalse(result.isApplicable());
        assertEquals("WAGES_ABOVE_CEILING", result.getReason());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getEmployeeContribution()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getEmployerContribution()));
    }

    @Test
    @DisplayName("ESI: Low daily wage employee (<= ₹176/day) exempts employee contribution while keeping employer contribution")
    void testEsiLowDailyWageExemption() {
        // ₹3,500 gross for 22 days = ₹159.09/day (<= ₹176)
        EsiCalculationResult result = esiService.calculateEsi(BigDecimal.valueOf(3500), 22);

        assertTrue(result.isApplicable());
        assertTrue(result.isLowWageExempt());
        assertEquals("COVERED_LOW_WAGE_EMPLOYEE_EXEMPT", result.getReason());
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getEmployeeContribution())); // Employee exempt
        assertEquals(0, new BigDecimal("113.75").compareTo(result.getEmployerContribution())); // 3500 * 3.25%
    }

    // ── PROFESSIONAL TAX TESTS ───────────────────────────────────────────────

    @Test
    @DisplayName("PT: Karnataka state evaluates gross slabs correctly")
    void testPtKarnataka() {
        PtCalculationResult highSalary = ptService.calculatePt(BigDecimal.valueOf(80000), "KA", LocalDate.of(2026, 9, 1));
        assertTrue(highSalary.isApplicable());
        assertEquals(0, new BigDecimal("200.00").compareTo(highSalary.getPtAmount()));

        PtCalculationResult lowSalary = ptService.calculatePt(BigDecimal.valueOf(12000), "KA", LocalDate.of(2026, 9, 1));
        assertFalse(lowSalary.isApplicable());
        assertEquals(0, BigDecimal.ZERO.compareTo(lowSalary.getPtAmount()));
    }

    @Test
    @DisplayName("PT: Maharashtra evaluates ₹200 in regular months and ₹300 in February")
    void testPtMaharashtra() {
        PtCalculationResult sept = ptService.calculatePt(BigDecimal.valueOf(50000), "MH", LocalDate.of(2026, 9, 1));
        assertEquals(0, new BigDecimal("200.00").compareTo(sept.getPtAmount()));

        PtCalculationResult feb = ptService.calculatePt(BigDecimal.valueOf(50000), "MH", LocalDate.of(2027, 2, 1));
        assertEquals(0, new BigDecimal("300.00").compareTo(feb.getPtAmount()));
    }

    @Test
    @DisplayName("PT: Tamil Nadu evaluates state slabs")
    void testPtTamilNadu() {
        PtCalculationResult tn = ptService.calculatePt(BigDecimal.valueOf(80000), "TN", LocalDate.of(2026, 9, 1));
        assertEquals(0, new BigDecimal("208.00").compareTo(tn.getPtAmount()));
    }

    @Test
    @DisplayName("PT: Delhi / Exempt state returns 0")
    void testPtDelhiExempt() {
        PtCalculationResult dl = ptService.calculatePt(BigDecimal.valueOf(80000), "DL", LocalDate.of(2026, 9, 1));
        assertFalse(dl.isApplicable());
        assertEquals(0, BigDecimal.ZERO.compareTo(dl.getPtAmount()));
    }

    // ── TDS TESTS ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("TDS: New Regime with income under ₹12 Lakhs qualifies for Section 87A full rebate (Tax = 0)")
    void testTdsNewRegimeRebate() {
        // Monthly gross = ₹80,000 -> Annual Projected = ₹80,000 * 12 = ₹9,60,000
        // Standard deduction = ₹75,000 -> Taxable Income = ₹8,85,000 (<= ₹12,00,000) -> Rebate applies -> Tax = 0
        TdsCalculationContext ctx = new TdsCalculationContext(
                101L, 1L, "NEW", LocalDate.of(2026, 4, 1),
                BigDecimal.valueOf(80000), BigDecimal.ZERO, BigDecimal.ZERO
        );

        TdsCalculationResult result = tdsService.calculateTds(ctx);

        assertEquals(0, new BigDecimal("960000.00").compareTo(result.getProjectedAnnualGross()));
        assertEquals(0, new BigDecimal("885000.00").compareTo(result.getTaxableIncome()));
        assertTrue(result.getSection87aRebate().compareTo(BigDecimal.ZERO) > 0);
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getTotalProjectedAnnualTax()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.getMonthlyTds()));
    }

    @Test
    @DisplayName("TDS: New Regime above ₹12 Lakhs calculates slab tax, 4% cess, and amortizes remaining periods")
    void testTdsNewRegimeHigherIncome() {
        // Monthly gross = ₹1,50,000 in September (7 periods remaining in FY 2026-27)
        // YTD (April-August: 5 months) = ₹7,50,000
        // Total Projected Annual Gross = ₹7,50,000 + (₹1,50,000 * 7) = ₹18,00,000
        // Standard Deduction = ₹75,000 -> Taxable Income = ₹17,25,000
        TdsCalculationContext ctx = new TdsCalculationContext(
                101L, 1L, "NEW", LocalDate.of(2026, 9, 1),
                BigDecimal.valueOf(150000), BigDecimal.valueOf(750000), BigDecimal.valueOf(60000)
        );

        TdsCalculationResult result = tdsService.calculateTds(ctx);

        assertEquals(0, new BigDecimal("1800000.00").compareTo(result.getProjectedAnnualGross()));
        assertEquals(0, new BigDecimal("1725000.00").compareTo(result.getTaxableIncome()));
        assertEquals(7, result.getRemainingPeriods());
        assertTrue(result.getTotalProjectedAnnualTax().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(result.getMonthlyTds().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(result.getHealthAndEducationCess().compareTo(BigDecimal.ZERO) > 0);
    }
}
