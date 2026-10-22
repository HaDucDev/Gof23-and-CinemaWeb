package com.td.cinema.choreography.seat.dto.projection;

import java.math.BigDecimal;

public interface HeldSeatPriceView extends ProjectionRow {
    Long getSeatId();

    String getSeatNo();

    String getSeatType();

    BigDecimal getBasePrice();

    BigDecimal getVipSurcharge();
}
