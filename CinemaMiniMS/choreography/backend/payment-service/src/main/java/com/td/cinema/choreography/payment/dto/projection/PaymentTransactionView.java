package com.td.cinema.choreography.payment.dto.projection;

import java.math.BigDecimal;

public interface PaymentTransactionView extends ProjectionRow {
    String getTransactionId();

    String getPaymentId();

    String getBookingId();

    String getOperationType();

    BigDecimal getAmount();

    String getCurrency();

    String getPaymentMethod();

    String getOriginalTransactionId();

    String getIdempotencyKey();

    String getReferenceCode();

    String getCreatedAt();

    String getReason();
}
