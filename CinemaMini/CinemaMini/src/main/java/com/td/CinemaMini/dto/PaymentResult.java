package com.td.CinemaMini.dto;

public final class PaymentResult {
    private final String referenceCode;

    public PaymentResult(String referenceCode) {
        this.referenceCode = referenceCode;
    }

    public String getReferenceCode() {
        return referenceCode;
    }
}
