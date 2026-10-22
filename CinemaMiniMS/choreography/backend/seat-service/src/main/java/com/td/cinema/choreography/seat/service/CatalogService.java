package com.td.cinema.choreography.seat.service;

import com.td.cinema.choreography.seat.exception.ApiException;
import com.td.cinema.choreography.seat.repository.CatalogRepository;
import com.td.cinema.choreography.seat.stream.SeatEvents;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.*;

/** Đọc danh mục phim/suất từ database Seat và mở đăng ký SSE theo suất chiếu. */
@Service
public class CatalogService {
    private final CatalogRepository repository;
    private final SeatEvents events;

    /** Nhận các thành phần đọc danh mục và cung cấp luồng cập nhật ghế. */
    public CatalogService(CatalogRepository repository, SeatEvents events) {
        this.repository = repository;
        this.events = events;
    }

    /** Đọc danh sách phim từ repository của Seat. */
    public List<Map<String, Object>> movies() {
        return repository.findMovies();
    }

    /** Kiểm tra mã phim/ngày hợp lệ rồi lấy các suất chiếu theo bộ lọc từ database Seat. */
    public List<Map<String, Object>> showtimes(Long movieId, String date) {
        if (movieId != null && movieId.longValue() < 1)
            throw ApiException.bad("movieId khong hop le.");
        if (date != null)
            try {
                LocalDate.parse(date);
            } catch (Exception e) {
                throw ApiException.bad("Ngay can co dang YYYY-MM-DD.");
            }
        return repository.findShowtimes(movieId, movieId, date, date);
    }

    /** Xác nhận suất tồn tại rồi đăng ký SseEmitter để nhận các thay đổi ghế đã commit. */
    public SseEmitter subscribe(String id) {
        repository.requireShowtime(id);
        return events.subscribe(id);
    }
}
