package com.td.CinemaMini.dto;

import com.td.CinemaMini.model.enums.PaymentMethod;
import java.math.BigDecimal;

public final class PaymentCommand {
    private final Long bookingId;
    private final BigDecimal amount;
    private final PaymentMethod method;

    public PaymentCommand(Long bookingId, BigDecimal amount, PaymentMethod method) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.method = method;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }
}
