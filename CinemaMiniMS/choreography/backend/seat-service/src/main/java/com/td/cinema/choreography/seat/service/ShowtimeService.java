package com.td.cinema.choreography.seat.service;

import com.td.cinema.choreography.seat.dto.request.ShowtimeCreateRequest;
import com.td.cinema.choreography.seat.exception.ApiException;
import com.td.cinema.choreography.seat.repository.ShowtimeRepository;
import com.td.cinema.choreography.seat.util.Data;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Tạo suất chiếu từ mẫu phòng, kiểm tra trùng lịch và khởi tạo trạng thái từng ghế. */
@Service
public class ShowtimeService {
    private final ShowtimeRepository showtimeRepository;

    /** Nhận repository quản lý lịch chiếu và tồn kho ghế của suất. */
    public ShowtimeService(ShowtimeRepository showtimeRepository) {
        this.showtimeRepository = showtimeRepository;
    }

    /** Tạo dữ liệu mẫu gồm giá, phụ thu VIP và tập ghế độc lập cho mỗi lần gọi. */
    public Map<String, Object> template() {
        // Mỗi lần gọi tạo dữ liệu ghế/giá riêng; sửa suất chiếu không làm thay đổi mẫu.
        List<Map<String, Object>> seats = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : showtimeRepository.findTemplateSeats()) {
            row.put("enabled", true);
            seats.add(row);
        }
        return Data.map(
                "name",
                "Suat tieu chuan",
                "basePrice",
                80000,
                "vipSurcharge",
                20000,
                "cleanupMinutes",
                20,
                "seats",
                seats);
    }

    /**
     * Khóa lịch phòng, kiểm tra giờ tương lai, giá và trùng lịch gồm 20 phút dọn phòng, rồi tạo
     * suất cùng trạng thái từng ghế.
     */
    @Transactional
    public Map<String, Object> create(ShowtimeCreateRequest request) {
        showtimeRepository.lockOperation("showtime-schedule");
        OffsetDateTime start;
        try {
            start = OffsetDateTime.parse(request.startsAt);
        } catch (Exception e) {
            throw ApiException.bad(
                    "startsAt can ngay gio kem mui gio, vi du 2026-10-25T09:00:00+07:00.");
        }
        if (!start.isAfter(showtimeRepository.currentTime()))
            throw ApiException.bad("Suat moi phai nam trong tuong lai.");
        if (request.basePrice.scale() > 0 || request.vipSurcharge.scale() > 0)
            throw ApiException.bad("Gia phai la so nguyen dong.");
        Map<String, Object> movie = showtimeRepository.findMovie(request.movieId);
        OffsetDateTime end = start.plusMinutes(Data.number(movie, "durationMinutes")),
                available = end.plusMinutes(20);
        if (showtimeRepository.findOverlappingShowtime(available, start) != null)
            throw ApiException.conflict("Lich phong chieu bi trung, bao gom 20 phut don phong.");
        Set<String> disabled = new HashSet<String>();
        if (request.disabledSeatNos != null)
            for (String seat : request.disabledSeatNos) {
                if (seat == null || !seat.matches("[A-E][1-6]") || !disabled.add(seat))
                    throw ApiException.bad("Danh sach ghe khong mo ban khong hop le.");
            }
        String id =
                "SHOW-"
                        + start.atZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh"))
                                .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + "-"
                        + UUID.randomUUID().toString().substring(0, 12);
        showtimeRepository.insertShowtime(
                id,
                request.movieId,
                start,
                end,
                available,
                Data.number(movie, "durationMinutes"),
                request.basePrice,
                request.vipSurcharge);
        for (Map<String, Object> seat : showtimeRepository.findPhysicalSeats())
            showtimeRepository.insertInventory(
                    id,
                    Data.number(seat, "seatId"),
                    disabled.contains(Data.text(seat, "seatNo")) ? "DISABLED" : "AVAILABLE");
        return showtimeRepository.findShowtimeDetail(id);
    }
}
