package com.td.CinemaMini.controller;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.repository.ShowtimeRepository;
import com.td.CinemaMini.service.booking.stream.SeatEventStream;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class SeatEventController {
    private final SeatEventStream stream;
    private final ShowtimeRepository showtimes;

    public SeatEventController(SeatEventStream stream, ShowtimeRepository showtimes) {
        this.stream = stream;
        this.showtimes = showtimes;
    }

    /**
     * Mở kết nối SSE để trình duyệt nhận cập nhật trạng thái ghế của một suất chiếu.
     *
     * @param id ID suất chiếu cần theo dõi từ phần {id} trong URL, tương ứng Showtime.id.
     * @return Kết nối text/event-stream gửi các cập nhật ghế và tín hiệu duy trì kết nối.
     */
    @GetMapping(value = "/showtimes/{id}/seat-events", produces = "text/event-stream")
    public SseEmitter events(@PathVariable Long id) {
        showtimes.findById(id).orElseThrow(BusinessException::missing);
        return stream.subscribe(id);
    }
}
