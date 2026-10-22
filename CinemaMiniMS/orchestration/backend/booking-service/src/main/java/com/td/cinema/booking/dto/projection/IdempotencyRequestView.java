package com.td.cinema.booking.dto.projection;


public interface IdempotencyRequestView extends ProjectionRow {
    Long getId();

    String getRequestKey();

    String getOperationType();

    String getPayloadFingerprint();

    Long getAccountId();

    String getBookingId();

    Short getResponseStatus();

    @JsonField
    String getResponseBody();

    String getCreatedAt();
}
