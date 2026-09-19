package com.example.ems.payroll.calculation;

import com.example.ems.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SalaryFormulaEvaluatorTest {

    private SalaryFormulaEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new SalaryFormulaEvaluator();
    }

    @Test
    @DisplayName("Evaluate simple multiplication and addition with operator precedence")
    void testPrecedenceEvaluation() {
        Map<String, BigDecimal> variables = Map.of(
                "BASIC", BigDecimal.valueOf(50000)
        );

        BigDecimal result = evaluator.evaluate("BASIC * 0.40 + 500", variables);

        // 50000 * 0.40 = 20000 + 500 = 20500.00
        assertEquals(new BigDecimal("20500.00"), result);
    }

    @Test
    @DisplayName("Evaluate parentheses expression")
    void testParenthesesEvaluation() {
        Map<String, BigDecimal> variables = Map.of(
                "BASIC", BigDecimal.valueOf(50000),
                "HOUSING", BigDecimal.valueOf(12500)
        );

        BigDecimal result = evaluator.evaluate("(BASIC + HOUSING) * 0.10", variables);

        // (50000 + 12500) * 0.10 = 62500 * 0.10 = 6250.00
        assertEquals(new BigDecimal("6250.00"), result);
    }

    @Test
    @DisplayName("Evaluate division")
    void testDivision() {
        Map<String, BigDecimal> variables = Map.of(
                "BASIC", BigDecimal.valueOf(50000)
        );

        BigDecimal result = evaluator.evaluate("BASIC / 2", variables);

        assertEquals(new BigDecimal("25000.00"), result);
    }

    @Test
    @DisplayName("Division by zero throws BadRequestException")
    void testDivisionByZero_ThrowsBadRequestException() {
        Map<String, BigDecimal> variables = Map.of(
                "BASIC", BigDecimal.valueOf(50000)
        );

        assertThrows(BadRequestException.class, () -> evaluator.evaluate("BASIC / 0", variables));
    }

    @Test
    @DisplayName("Missing variable throws BadRequestException")
    void testMissingVariable_ThrowsBadRequestException() {
        Map<String, BigDecimal> variables = Map.of(
                "BASIC", BigDecimal.valueOf(50000)
        );

        assertThrows(BadRequestException.class, () -> evaluator.evaluate("BASIC + UNKNOWN_COMP", variables));
    }

    @Test
    @DisplayName("Invalid characters or injections throw BadRequestException")
    void testInvalidCharacters_ThrowsBadRequestException() {
        Map<String, BigDecimal> variables = Map.of(
                "BASIC", BigDecimal.valueOf(50000)
        );

        assertThrows(BadRequestException.class, () -> evaluator.evaluate("BASIC; System.exit(0)", variables));
    }
}
