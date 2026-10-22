package com.td.cinema.choreography.booking.dto.projection;

import java.math.BigDecimal;

public interface BookingView extends ProjectionRow {
    String getBookingId();

    String getIdempotencyKey();

    String getRequestHash();

    Long getAccountId();

    Long getCustomerId();

    String getShowtimeId();

    String getHoldId();

    Long getHoldRevision();

    String getStatus();

    BigDecimal getAmount();

    BigDecimal getSubtotal();

    Short getDiscountPercent();

    String getCustomerType();

    String getMovieTitle();

    String getStartsAt();

    String getExpiresAt();

    String getTicketCode();

    String getPaymentId();

    String getPaymentMethod();

    String getOperationId();

    String getContactName();

    String getContactEmail();

    String getContactPhone();

    String getNote();

    String getFailureReason();

    String getCreatedAt();

    String getUpdatedAt();
}
