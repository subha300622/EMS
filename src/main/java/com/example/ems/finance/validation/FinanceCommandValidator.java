package com.example.ems.finance.validation;

import com.example.ems.finance.dto.FinanceCommandEnvelope;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class FinanceCommandValidator {

    public void validate(FinanceCommandEnvelope envelope) {
        if (envelope == null) {
            throw new IllegalArgumentException("Command envelope cannot be null");
        }
        if (envelope.getVersion() == null || envelope.getVersion() != 1) {
            throw new IllegalArgumentException("Unsupported command version: " + envelope.getVersion());
        }
        if (envelope.getAction() == null || envelope.getAction().isBlank()) {
            throw new IllegalArgumentException("Action is required in command envelope");
        }

        String action = envelope.getAction().toUpperCase();
        Map<String, Object> payload = envelope.getPayload();

        switch (action) {
            case "UPDATE_BANK":
                validatePresent(payload, "bankName");
                validatePresent(payload, "bankAccountNumber");
                validatePresent(payload, "bankIfsc");
                break;
            case "UPDATE_TAX":
                validatePresent(payload, "panNumber");
                break;
            case "UPDATE_STATUTORY":
                validatePresent(payload, "uanNumber");
                break;
            case "ASSIGN_SALARY":
                if (payload == null || (!payload.containsKey("salaryStructureId") && 
                    (!payload.containsKey("basicSalary") || !payload.containsKey("hra") || !payload.containsKey("allowances")))) {
                    throw new IllegalArgumentException("ASSIGN_SALARY requires either 'salaryStructureId' or basicSalary, hra, and allowances");
                }
                break;
            case "SUBMIT":
            case "APPROVE":
            case "REJECT":
            case "REINITIALIZE":
            case "ACTIVATE":
                // No mandatory inputs, optional notes
                break;
            default:
                throw new IllegalArgumentException("Unsupported action type: " + action);
        }
    }

    private void validatePresent(Map<String, Object> payload, String field) {
        if (payload == null || !payload.containsKey(field) || payload.get(field) == null || payload.get(field).toString().isBlank()) {
            throw new IllegalArgumentException("Field '" + field + "' is required for this action");
        }
    }
}
