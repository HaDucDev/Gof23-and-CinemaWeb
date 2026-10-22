package com.td.cinema.booking.dto.projection;


public interface SagaStateView extends ProjectionRow {
    String getBookingId();

    String getCurrentStep();

    String getStatus();

    String getOperationType();

    String getOperationId();

    String getLastError();

    Long getVersion();

    String getCreatedAt();

    String getUpdatedAt();

    Integer getAttemptCount();

    String getLeaseToken();
}
