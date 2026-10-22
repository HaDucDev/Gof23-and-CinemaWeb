package com.td.cinema.payment.dto.projection;

import java.math.BigDecimal;

public interface PaymentView extends ProjectionRow {
    String getPaymentId();

    String getBookingId();

    BigDecimal getAmount();

    String getStatus();

    String getIdempotencyKey();

    String getRequestHash();

    String getPaymentMethod();

    String getSimulatedOutcome();

    String getFailureReason();

    String getCreatedAt();

    String getUpdatedAt();
}
