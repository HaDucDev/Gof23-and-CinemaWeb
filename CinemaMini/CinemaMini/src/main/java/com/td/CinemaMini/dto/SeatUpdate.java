package com.td.CinemaMini.dto;

import com.td.CinemaMini.model.ShowtimeSeat;

/** Payload công khai cho SSE: không có token hay danh tính khách. */
public final class SeatUpdate {
    private final Long id;
    private final Long seatId;
    private final Long version;
    private final String status;

    public SeatUpdate(ShowtimeSeat seat) {
        id = seat.getId();
        seatId = seat.getSeatId();
        version = seat.getVersion();
        status = seat.getStatus().name();
    }

    public Long getId() {
        return id;
    }

    public Long getSeatId() {
        return seatId;
    }

    public Long getVersion() {
        return version;
    }

    public String getStatus() {
        return status;
    }
}
