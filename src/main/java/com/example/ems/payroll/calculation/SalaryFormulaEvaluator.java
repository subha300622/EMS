package com.example.ems.payroll.calculation;

import com.example.ems.common.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SalaryFormulaEvaluator {

    private static final MathContext MATH_CONTEXT = new MathContext(10, RoundingMode.HALF_UP);
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\\s*([A-Za-z_][A-Za-z0-9_]*|[0-9]+(?:\\.[0-9]+)?|[+\\-*/()]|[^\\s])");

    /**
     * Evaluates an arithmetic formula string replacing variable component codes with their computed amounts.
     * Example: "BASIC * 0.40 + 500" with variables {BASIC=50000} returns 20500.00
     *
     * @param formula The mathematical expression
     * @param variables Map of component codes to their BigDecimal values
     * @return Evaluated BigDecimal amount rounded to 2 decimal places
     */
    public BigDecimal evaluate(String formula, Map<String, BigDecimal> variables) {
        if (formula == null || formula.trim().isEmpty()) {
            throw new BadRequestException("Formula expression cannot be empty.");
        }

        List<String> tokens = tokenize(formula);
        List<String> rpn = toRpn(tokens, variables);
        BigDecimal result = evaluateRpn(rpn);

        return result.setScale(2, RoundingMode.HALF_UP);
    }

    private List<String> tokenize(String formula) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(formula);

        while (matcher.find()) {
            String token = matcher.group(1);
            // Validate token format
            if (!isValidToken(token)) {
                throw new BadRequestException("Invalid character or token in formula: '" + token + "'");
            }
            tokens.add(token);
        }

        if (tokens.isEmpty()) {
            throw new BadRequestException("No valid tokens found in formula expression.");
        }
        return tokens;
    }

    private boolean isValidToken(String token) {
        if (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("(") || token.equals(")")) {
            return true;
        }
        // Check if identifier (component code)
        if (token.matches("^[A-Za-z_][A-Za-z0-9_]*$")) {
            return true;
        }
        // Check if numeric constant
        return token.matches("^[0-9]+(?:\\.[0-9]+)?$");
    }

    private int precedence(String op) {
        if (op.equals("+") || op.equals("-")) return 1;
        if (op.equals("*") || op.equals("/")) return 2;
        return 0;
    }

    /**
     * Shunting-yard algorithm: Convert infix tokens to Reverse Polish Notation (RPN).
     */
    private List<String> toRpn(List<String> tokens, Map<String, BigDecimal> variables) {
        List<String> output = new ArrayList<>();
        Deque<String> operators = new ArrayDeque<>();

        String prevToken = null;

        for (String token : tokens) {
            if (isNumber(token)) {
                output.add(token);
            } else if (isIdentifier(token)) {
                BigDecimal val = variables.get(token.toUpperCase());
                if (val == null) {
                    throw new BadRequestException("Unknown or uncomputed component variable in formula: '" + token + "'");
                }
                output.add(val.toPlainString());
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }
                if (operators.isEmpty()) {
                    throw new BadRequestException("Mismatched parentheses in formula.");
                }
                operators.pop(); // Pop '('
            } else if (isOperator(token)) {
                // Handle unary minus (e.g. -5 or (-BASIC))
                if (token.equals("-") && (prevToken == null || prevToken.equals("(") || isOperator(prevToken))) {
                    output.add("0"); // Convert -x to (0 - x)
                }
                while (!operators.isEmpty() && isOperator(operators.peek()) && precedence(operators.peek()) >= precedence(token)) {
                    output.add(operators.pop());
                }
                operators.push(token);
            }
            prevToken = token;
        }

        while (!operators.isEmpty()) {
            String op = operators.pop();
            if (op.equals("(") || op.equals(")")) {
                throw new BadRequestException("Mismatched parentheses in formula.");
            }
            output.add(op);
        }

        return output;
    }

    private BigDecimal evaluateRpn(List<String> rpn) {
        Deque<BigDecimal> stack = new ArrayDeque<>();

        for (String token : rpn) {
            if (isOperator(token)) {
                if (stack.size() < 2) {
                    throw new BadRequestException("Malformed formula expression.");
                }
                BigDecimal b = stack.pop();
                BigDecimal a = stack.pop();
                BigDecimal res;

                switch (token) {
                    case "+":
                        res = a.add(b, MATH_CONTEXT);
                        break;
                    case "-":
                        res = a.subtract(b, MATH_CONTEXT);
                        break;
                    case "*":
                        res = a.multiply(b, MATH_CONTEXT);
                        break;
                    case "/":
                        if (b.compareTo(BigDecimal.ZERO) == 0) {
                            throw new BadRequestException("Division by zero in formula calculation.");
                        }
                        res = a.divide(b, MATH_CONTEXT);
                        break;
                    default:
                        throw new BadRequestException("Unsupported operator: " + token);
                }
                stack.push(res);
            } else {
                stack.push(new BigDecimal(token));
            }
        }

        if (stack.size() != 1) {
            throw new BadRequestException("Malformed formula expression: could not resolve to a single numeric value.");
        }
        return stack.pop();
    }

    private boolean isOperator(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }

    private boolean isNumber(String token) {
        return token.matches("^[0-9]+(?:\\.[0-9]+)?$");
    }

    private boolean isIdentifier(String token) {
        return token.matches("^[A-Za-z_][A-Za-z0-9_]*$");
    }
}
