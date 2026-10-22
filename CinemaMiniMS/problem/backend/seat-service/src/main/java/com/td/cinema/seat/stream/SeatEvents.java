package com.td.cinema.seat.stream;

import com.td.cinema.seat.util.Data;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;
import java.util.concurrent.*;

@Component
public class SeatEvents {
    private final ConcurrentMap<String, CopyOnWriteArrayList<SseEmitter>> listeners =
            new ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>>();

    public SseEmitter subscribe(String showtime) {
        SseEmitter emitter = new SseEmitter(1200000L);
        CopyOnWriteArrayList<SseEmitter> group =
                listeners.computeIfAbsent(showtime, k -> new CopyOnWriteArrayList<SseEmitter>());
        group.add(emitter);
        Runnable remove =
                () -> {
                    group.remove(emitter);
                    if (group.isEmpty()) listeners.remove(showtime, group);
                };
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(error -> remove.run());
        try {
            emitter.send(SseEmitter.event().name("ready").data(Data.map("showtimeId", showtime)));
        } catch (Exception e) {
            remove.run();
        }
        return emitter;
    }

    public void changed(String showtime) {
        Runnable send = () -> send(showtime, "seats-changed");
        if (TransactionSynchronizationManager.isSynchronizationActive())
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            send.run();
                        }
                    });
        else send.run();
    }

    private void send(String showtime, String event) {
        List<SseEmitter> group = listeners.get(showtime);
        if (group == null) return;
        for (SseEmitter emitter : group)
            try {
                emitter.send(SseEmitter.event().name(event).data(Data.map("showtimeId", showtime)));
            } catch (Exception e) {
                group.remove(emitter);
                // Spring MVC handles async cleanup after a failed send. Completing an
                // already failed AsyncContext can throw after the seat transaction committed.
            }
    }

    @Scheduled(fixedDelay = 20000L)
    public void heartbeat() {
        for (String showtime : listeners.keySet()) send(showtime, "heartbeat");
    }
}
