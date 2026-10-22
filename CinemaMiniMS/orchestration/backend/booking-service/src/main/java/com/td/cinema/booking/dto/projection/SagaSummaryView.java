package com.td.cinema.booking.dto.projection;


public interface SagaSummaryView extends ProjectionRow {
    String getCurrentStep();

    String getStatus();

    String getOperationType();

    String getLastError();

    String getUpdatedAt();

    Integer getAttemptCount();

    String getNextAttemptAt();
}
