package com.example.ems.payroll.statutory;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class ProfessionalTaxService {

    public PtCalculationResult calculatePt(BigDecimal grossSalary, String stateCode, LocalDate payrollDate) {
        if (grossSalary == null || grossSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return new PtCalculationResult(false, stateCode, BigDecimal.ZERO, "EXEMPT_ZERO_SALARY");
        }

        String state = stateCode != null ? stateCode.toUpperCase().trim() : "KA";
        int month = payrollDate != null ? payrollDate.getMonthValue() : LocalDate.now().getMonthValue();

        double gross = grossSalary.doubleValue();
        BigDecimal pt = BigDecimal.ZERO;
        String slabDesc;

        switch (state) {
            case "MH":
            case "MAHARASHTRA":
                if (gross <= 7500) {
                    pt = BigDecimal.ZERO;
                    slabDesc = "MH: Up to ₹7,500 (₹0)";
                } else if (gross <= 10000) {
                    pt = BigDecimal.valueOf(175);
                    slabDesc = "MH: ₹7,501 to ₹10,000 (₹175)";
                } else {
                    // February has ₹300 deduction to align with ₹2,500 annual statutory ceiling in MH
                    pt = (month == 2) ? BigDecimal.valueOf(300) : BigDecimal.valueOf(200);
                    slabDesc = "MH: Above ₹10,000 (₹" + pt.intValue() + ")";
                }
                break;

            case "TN":
            case "TAMIL_NADU":
            case "TAMILNADU":
                if (gross <= 21000) {
                    pt = BigDecimal.ZERO;
                    slabDesc = "TN: Up to ₹21,000 (₹0)";
                } else if (gross <= 30000) {
                    pt = BigDecimal.valueOf(20);
                    slabDesc = "TN: ₹21,001 to ₹30,000 (₹20)";
                } else if (gross <= 45000) {
                    pt = BigDecimal.valueOf(48);
                    slabDesc = "TN: ₹30,001 to ₹45,000 (₹48)";
                } else if (gross <= 60000) {
                    pt = BigDecimal.valueOf(97);
                    slabDesc = "TN: ₹45,001 to ₹60,000 (₹97)";
                } else if (gross <= 75000) {
                    pt = BigDecimal.valueOf(160);
                    slabDesc = "TN: ₹60,001 to ₹75,000 (₹160)";
                } else {
                    pt = BigDecimal.valueOf(208);
                    slabDesc = "TN: Above ₹75,000 (₹208)";
                }
                break;

            case "TS":
            case "TELANGANA":
            case "AP":
            case "ANDHRA_PRADESH":
                if (gross <= 15000) {
                    pt = BigDecimal.ZERO;
                    slabDesc = "TS/AP: Up to ₹15,000 (₹0)";
                } else if (gross <= 20000) {
                    pt = BigDecimal.valueOf(150);
                    slabDesc = "TS/AP: ₹15,001 to ₹20,000 (₹150)";
                } else {
                    pt = BigDecimal.valueOf(200);
                    slabDesc = "TS/AP: Above ₹20,000 (₹200)";
                }
                break;

            case "DL":
            case "DELHI":
            case "EXEMPT":
            case "NONE":
                pt = BigDecimal.ZERO;
                slabDesc = "EXEMPT: No Professional Tax applicable in " + state;
                return new PtCalculationResult(false, state, BigDecimal.ZERO, slabDesc);

            case "KA":
            case "KARNATAKA":
            default:
                if (gross <= 15000) {
                    pt = BigDecimal.ZERO;
                    slabDesc = "KA: Up to ₹15,000 (₹0)";
                } else {
                    pt = BigDecimal.valueOf(200);
                    slabDesc = "KA: Above ₹15,000 (₹200)";
                }
                break;
        }

        return new PtCalculationResult(pt.compareTo(BigDecimal.ZERO) > 0, state, pt, slabDesc);
    }
}
