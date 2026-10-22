package com.td.cinema.choreography.booking.dto.projection;
public interface ProgressView extends ProjectionRow {
    String getOperationType(); String getOperationId(); String getPaymentStatus(); String getSeatStatus(); String getLastEvent(); String getReason();
}
