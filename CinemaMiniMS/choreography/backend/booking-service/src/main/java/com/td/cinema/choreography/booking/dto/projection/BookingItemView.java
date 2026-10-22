package com.td.cinema.choreography.booking.dto.projection;

import java.math.BigDecimal;

public interface BookingItemView extends ProjectionRow {
    Long getSeatId();

    String getSeatNoSnapshot();

    String getSeatTypeSnapshot();

    BigDecimal getBasePriceSnapshot();

    BigDecimal getVipSurchargeSnapshot();

    BigDecimal getUnitPriceSnapshot();
}
