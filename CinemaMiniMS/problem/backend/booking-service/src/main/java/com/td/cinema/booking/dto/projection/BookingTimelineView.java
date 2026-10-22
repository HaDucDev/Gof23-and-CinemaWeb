package com.td.cinema.booking.dto.projection;


public interface BookingTimelineView extends ProjectionRow {
    String getServiceName();

    String getAction();

    String getResult();

    String getHappenedAt();

    @JsonField
    String getDetail();
}
