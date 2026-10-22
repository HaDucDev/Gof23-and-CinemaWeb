package com.td.cinema.choreography.booking.dto.projection;


public interface SagaSummaryView extends ProjectionRow {
    String getCurrentStep();

    String getStatus();

    String getOperationType();

    String getLastError();

    String getUpdatedAt();
}
