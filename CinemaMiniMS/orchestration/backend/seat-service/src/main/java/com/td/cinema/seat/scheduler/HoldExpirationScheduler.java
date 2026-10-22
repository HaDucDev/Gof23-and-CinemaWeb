package com.td.cinema.seat.scheduler;

import com.td.cinema.seat.service.SeatService;
import com.td.cinema.seat.util.Data;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HoldExpirationScheduler {
    private final SeatService service;

    public HoldExpirationScheduler(SeatService service) {
        this.service = service;
    }

    @Scheduled(fixedDelay = 2000L)
    public void expire() {
        for (Map<String, Object> row : service.expiringShowtimes())
            service.expire(Data.text(row, "showtimeId"));
    }
}
