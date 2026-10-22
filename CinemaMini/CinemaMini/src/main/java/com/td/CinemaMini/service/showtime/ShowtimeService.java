package com.td.CinemaMini.service.showtime;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.SeatStatus;
import com.td.CinemaMini.repository.*;
import com.td.CinemaMini.service.account.AccountService;
import com.td.CinemaMini.service.booking.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Tạo suất chiếu từ mẫu cấu hình và lựa chọn của admin, kèm trạng thái ghế ban đầu. */
@Service
public class ShowtimeService {
    private final TemplateCatalog catalog;
    private final MovieRepository movies;
    private final SeatRepository seats;
    private final ShowtimeRepository showtimes;
    private final ShowtimeSeatRepository inventory;
    private final DatabaseClock clock;
    private final AccountService accounts;
    private final JdbcTemplate jdbc;

    public ShowtimeService(
            TemplateCatalog catalog,
            MovieRepository movies,
            SeatRepository seats,
            ShowtimeRepository showtimes,
            ShowtimeSeatRepository inventory,
            DatabaseClock clock,
            AccountService accounts,
            JdbcTemplate jdbc) {
        this.catalog = catalog;
        this.movies = movies;
        this.seats = seats;
        this.showtimes = showtimes;
        this.inventory = inventory;
        this.clock = clock;
        this.accounts = accounts;
        this.jdbc = jdbc;
    }

    @Transactional
    public Showtime createFromTemplate(
            UserAccount actor,
            Long movieId,
            LocalDateTime localStart,
            BigDecimal base,
            BigDecimal surcharge,
            Set<String> disabled) {
        // Chỉ tài khoản ADMIN được tạo suất; kiểm tra lại quyền ở tầng service.
        accounts.requireAdmin(actor);
        // Giờ chiếu và giá phải được cung cấp; giá dùng số nguyên đồng, không nhận phần thập phân.
        // Giá cơ bản phải dương, phụ thu không âm và mỗi giá có tối đa 13 chữ số phần nguyên.
        if (localStart == null
                || base == null
                || surcharge == null
                || base.signum() <= 0
                || surcharge.signum() < 0
                || base.stripTrailingZeros().scale() > 0
                || surcharge.stripTrailingZeros().scale() > 0
                || base.precision() - base.scale() > 13
                || surcharge.precision() - surcharge.scale() > 13) {
            throw BusinessException.invalid(
                    "Giá vé phải là số nguyên đồng hợp lệ; giá cơ bản lớn hơn 0, phụ thu không âm.");
        }
        // Lấy thời lượng phim đã lưu để tính giờ kết thúc; giờ admin nhập được hiểu là giờ Việt
        // Nam.
        Movie movie = movies.findById(movieId).orElseThrow(BusinessException::missing);
        Instant start = localStart.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
        if (!start.isAfter(clock.now())) {
            throw BusinessException.invalid("Giờ bắt đầu phải nằm trong tương lai.");
        }
        // Sau khi phim kết thúc, phòng cần thêm 20 phút dọn trước khi có thể chiếu suất tiếp theo.
        Instant end = start.plusSeconds(movie.getDurationMinutes() * 60L),
                available = end.plusSeconds(1200);
        // Một phòng: tuần tự hóa thao tác tạo lịch trước khi kiểm tra chồng lấn.
        // Các yêu cầu tạo suất cùng lấy khóa số 4031901 trong database trước khi kiểm tra lịch.
        // Khóa được giữ đến hết transaction để hai yêu cầu không cùng chọn một khoảng giờ trống.
        jdbc.execute("select pg_advisory_xact_lock(4031901)");
        if (showtimes.countOverlaps(start, available) > 0) {
            throw BusinessException.conflict("Lịch chiếu chồng lấn phim hoặc 20 phút dọn phòng.");
        }
        // GoF: Prototype. Lấy bản sao của mẫu đã đọc từ config/showtime-templates.json.
        // Giá trong JSON điền sẵn form; giá lưu cho suất mới lấy từ dữ liệu admin gửi.
        ShowtimeTemplate working = catalog.standard().copy();
        working.getPricing().setBasePrice(base);
        working.getPricing().setVipSurcharge(surcharge);
        Set<String> known = new HashSet<String>();
        // Lựa chọn trên form quyết định ghế mở bán, ghi đè cờ openSale ban đầu trong JSON.
        for (SeatConfiguration config : working.getSeats()) {
            known.add(config.getCode());
            config.setOpenSale(!disabled.contains(config.getCode()));
        }
        // Từ chối mã ghế ngoài danh sách mẫu, tránh đóng/mở bán nhầm ghế không tồn tại trong mẫu.
        if (!known.containsAll(disabled)) {
            throw BusinessException.invalid("Danh sách ghế không mở bán không hợp lệ.");
        }
        // Chốt thời lượng phim, thời gian dọn phòng và giá cho riêng suất chiếu mới.
        Showtime showtime = new Showtime();
        showtime.setMovieId(movieId);
        showtime.setStartsAt(start);
        showtime.setEndsAt(end);
        showtime.setRoomAvailableAt(available);
        showtime.setDurationMinutes(movie.getDurationMinutes());
        showtime.setCleanupMinutes((short) 20);
        showtime.setBasePrice(working.getPricing().getBasePrice());
        showtime.setVipSurcharge(working.getPricing().getVipSurcharge());
        // Lưu suất để có Showtime.id trước khi tạo trạng thái từng ghế của suất.
        showtimes.saveAndFlush(showtime);
        // Tra ghế gốc theo mã như A1, D2 để liên kết cấu hình mẫu với Seat.id trong database.
        Map<String, Seat> physical = new HashMap<String, Seat>();
        for (Seat seat : seats.findAll()) {
            physical.put(seat.getCode(), seat);
        }
        for (SeatConfiguration config : working.getSeats()) {
            Seat seat = physical.get(config.getCode());
            if (seat == null) {
                throw BusinessException.conflict(
                        "Thiếu dữ liệu ghế. Hãy chuẩn bị database bằng SQL.");
            }
            // Mỗi ghế của mẫu có một bản ghi riêng trong suất: AVAILABLE nếu mở bán, DISABLED nếu
            // đóng.
            ShowtimeSeat row = new ShowtimeSeat();
            row.setShowtimeId(showtime.getId());
            row.setSeatId(seat.getId());
            row.setStatus(config.isOpenSale() ? SeatStatus.AVAILABLE : SeatStatus.DISABLED);
            inventory.save(row);
        }
        return showtime;
    }
}
