package com.td.cinema.seat.dto.projection;

import java.math.BigDecimal;

public interface SeatAvailabilityView extends ProjectionRow {
    Long getSeatId();

    String getSeatNo();

    String getSeatType();

    String getStatus();

    String getHoldExpiresAt();

    Long getVersion();

    Boolean getHeldByMe();

    BigDecimal getBasePrice();

    BigDecimal getVipSurcharge();
}
