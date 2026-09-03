package com.example.ems.payroll.payment;

public class PaymentAccountResult {

    private final String contactId;
    private final String fundAccountId;
    private final boolean success;
    private final String errorMessage;

    public PaymentAccountResult(String contactId, String fundAccountId, boolean success, String errorMessage) {
        this.contactId = contactId;
        this.fundAccountId = fundAccountId;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public static PaymentAccountResult success(String contactId, String fundAccountId) {
        return new PaymentAccountResult(contactId, fundAccountId, true, null);
    }

    public static PaymentAccountResult failure(String errorMessage) {
        return new PaymentAccountResult(null, null, false, errorMessage);
    }

    public String getContactId() {
        return contactId;
    }

    public String getFundAccountId() {
        return fundAccountId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
