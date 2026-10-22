package com.td.CinemaMini.dto;

import com.td.CinemaMini.model.Payment;
import com.td.CinemaMini.model.enums.PaymentMethod;
import java.math.BigDecimal;

public final class RefundCommand {
    private final BigDecimal amount;
    private final PaymentMethod method;
    private final String originalReference;

    public RefundCommand(Payment payment) {
        amount = payment.getAmount();
        method = payment.getPaymentMethod();
        originalReference = payment.getReferenceCode();
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public String getOriginalReference() {
        return originalReference;
    }
}
