package com.td.cinema.seat.dto.projection;


public interface OperationView extends ProjectionRow {
    String getOperationKey();

    Long getAccountId();

    String getRequestHash();

    String getHoldId();

    String getCreatedAt();
}
