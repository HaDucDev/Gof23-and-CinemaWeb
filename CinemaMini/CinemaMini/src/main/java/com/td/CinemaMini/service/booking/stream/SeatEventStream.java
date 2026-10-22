package com.td.CinemaMini.service.booking.stream;

import com.td.CinemaMini.dto.SeatUpdate;
import com.td.CinemaMini.model.ShowtimeSeat;
import com.td.CinemaMini.repository.ShowtimeSeatRepository;
import com.td.CinemaMini.service.notification.AfterCommit;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Gửi trạng thái ghế theo thời gian thực qua SSE đến các trình duyệt đang xem cùng suất. */
@Component
public class SeatEventStream {
    // Nhóm kết nối theo Showtime.id; một suất có thể có nhiều người đang mở sơ đồ ghế.
    private final Map<Long, List<SseEmitter>> streams =
            new ConcurrentHashMap<Long, List<SseEmitter>>();
    private final ShowtimeSeatRepository seats;

    public SeatEventStream(ShowtimeSeatRepository seats) {
        this.seats = seats;
    }

    public SseEmitter subscribe(Long showtimeId) {
        // Tạo kết nối SSE không đặt thời hạn phía emitter; đưa kết nối vào nhóm của suất chiếu.
        final SseEmitter emitter = new SseEmitter(0L);
        final List<SseEmitter> group =
                streams.computeIfAbsent(showtimeId, k -> new CopyOnWriteArrayList<SseEmitter>());
        group.add(emitter);
        // Gỡ kết nối đã đóng, lỗi hoặc hết thời gian để tránh giữ kết nối không còn sử dụng.
        emitter.onCompletion(() -> group.remove(emitter));
        emitter.onTimeout(() -> group.remove(emitter));
        emitter.onError(error -> group.remove(emitter));
        try {
            emitter.send(SseEmitter.event().name("connected").data("ready"));
        } catch (Exception error) {
            group.remove(emitter);
        }
        return emitter;
    }

    public void changed(Long showtimeId) {
        seats.flush(); // @Version tăng trước khi chụp payload.
        // Chụp trạng thái và version sau khi flush để trình duyệt đối chiếu phiên bản ghế.
        final List<SeatUpdate> payload = new ArrayList<SeatUpdate>();
        for (ShowtimeSeat seat : seats.findByShowtimeIdOrderByIdAsc(showtimeId)) {
            payload.add(new SeatUpdate(seat));
        }
        // Chỉ gửi thay đổi khi transaction đã lưu thành công; dữ liệu rollback không được phát đi.
        AfterCommit.run(() -> send(showtimeId, "availability", payload));
    }

    private void send(Long id, String name, Object payload) {
        // Chỉ gửi cho các trình duyệt đăng ký đúng suất chiếu có thay đổi.
        List<SseEmitter> group = streams.get(id);
        if (group == null) {
            return;
        }
        for (SseEmitter emitter : group) {
            try {
                emitter.send(SseEmitter.event().name(name).data(payload));
            } catch (Exception error) {
                // Kết nối không gửi được được gỡ khỏi danh sách và kết thúc phía server.
                group.remove(emitter);
                emitter.complete();
            }
        }
    }

    // Gửi tín hiệu định kỳ 15 giây để duy trì hoạt động của kết nối SSE.
    @Scheduled(fixedDelay = 15000)
    public void heartbeat() {
        for (Long id : streams.keySet()) {
            send(id, "heartbeat", "alive");
        }
    }
}
