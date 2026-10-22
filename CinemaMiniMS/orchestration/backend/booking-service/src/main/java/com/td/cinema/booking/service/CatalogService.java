package com.td.cinema.booking.service;

import com.td.cinema.booking.client.SeatClient;
import com.td.cinema.booking.client.SeatStreamRelay;
import com.td.cinema.booking.exception.ApiException;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDate;
import java.util.*;

/** Kiểm tra bộ lọc danh mục rồi lấy phim, suất, ghế và luồng SSE từ Seat cho frontend. */
@Service
public class CatalogService {
    private final SeatClient seat;
    private final SeatStreamRelay streams;

    /** Nhận các thành phần đọc danh mục và cung cấp luồng cập nhật ghế. */
    public CatalogService(SeatClient seat, SeatStreamRelay streams) {
        this.seat = seat;
        this.streams = streams;
    }

    /** Lấy danh sách phim qua API nội bộ của Seat. */
    public List<Map<String, Object>> movies() {
        return seat.movies();
    }

    /** Kiểm tra mã phim/ngày hợp lệ rồi lấy các suất chiếu theo bộ lọc từ Seat service. */
    public List<Map<String, Object>> showtimes(Long movie, String date) {
        if (movie != null && movie.longValue() < 1) throw ApiException.bad("movieId khong hop le.");
        if (date != null)
            try {
                LocalDate.parse(date);
            } catch (Exception e) {
                throw ApiException.bad("Ngay can co dang YYYY-MM-DD.");
            }
        return seat.showtimes(movie, date);
    }

    /** Kiểm tra mã suất rồi lấy sơ đồ ghế từ Seat theo tài khoản người đang xem. */
    public List<Map<String, Object>> seats(String id, long actor) {
        return seat.inventory(showtime(id), actor);
    }

    /** Kiểm tra mã suất rồi mở luồng chuyển tiếp SSE từ Seat tới controller. */
    public StreamingResponseBody events(String id) {
        return streams.stream(showtime(id));
    }

    /** Chặn mã suất rỗng hoặc sai định dạng trước khi dùng trong URL gọi Seat. */
    private String showtime(String id) {
        if (id == null || !id.matches("[A-Za-z0-9-]{1,40}"))
            throw ApiException.bad("Ma suat khong hop le.");
        return id;
    }
}
