package com.td.CinemaMini.controller;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.repository.*;
import com.td.CinemaMini.service.account.AccountService;
import com.td.CinemaMini.service.booking.*;
import com.td.CinemaMini.service.showtime.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import static com.td.CinemaMini.dto.Values.map;

@Controller
public class AdminShowtimeController {
    private final AccountService accounts;
    private final ShowtimeService service;
    private final TemplateCatalog catalog;
    private final ShowtimeRepository showtimes;
    private final MovieRepository movies;
    private final BookingRepository bookings;
    private final BookingViewService views;

    public AdminShowtimeController(
            AccountService accounts,
            ShowtimeService service,
            TemplateCatalog catalog,
            ShowtimeRepository showtimes,
            MovieRepository movies,
            BookingRepository bookings,
            BookingViewService views) {
        this.accounts = accounts;
        this.service = service;
        this.catalog = catalog;
        this.showtimes = showtimes;
        this.movies = movies;
        this.bookings = bookings;
        this.views = views;
    }

    /**
     * Hiển thị form tạo suất từ mẫu và danh sách lịch chiếu cho admin.
     *
     * @param model Model do Spring cấp để đưa danh sách phim, mẫu giá và ghế, giờ gợi ý và lịch
     *     chiếu ra trang quản lý.
     * @return Tên trang admin-showtimes.
     */
    @GetMapping("/admin/showtimes")
    public String page(Model model) {
        model.addAttribute("movies", movies.findAllByOrderByIdAsc());
        model.addAttribute("template", catalog.standard());
        model.addAttribute(
                "defaultStart",
                LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1) + "T10:00");
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Showtime showtime : showtimes.findAllByOrderByStartsAtDesc()) {
            rows.add(
                    map(
                            "showtime",
                            showtime,
                            "movie",
                            movies.findById(showtime.getMovieId())
                                    .orElseThrow(BusinessException::missing)));
        }
        model.addAttribute("rows", rows);
        return "admin-showtimes";
    }

    /**
     * Nhận form admin để tạo một suất mới từ bản sao của mẫu giá và ghế.
     *
     * @param principal Thông tin tài khoản admin do Spring Security truyền vào; getName() trả về
     *     email đăng nhập.
     * @param movieId ID phim được chọn trong form, tương ứng Movie.id.
     * @param startsAt Ngày giờ bắt đầu từ form datetime-local theo định dạng ISO; service hiểu đây
     *     là giờ Việt Nam.
     * @param basePrice Giá cơ bản của một ghế do admin gửi từ form, tính bằng đồng; phải là số
     *     nguyên lớn hơn 0.
     * @param vipSurcharge Phụ thu cho một ghế VIP do admin gửi từ form, tính bằng đồng; phải là số
     *     nguyên không âm.
     * @param disabledSeats Tập mã ghế admin đánh dấu không mở bán, ví dụ A1 hoặc E6. Không gửi
     *     trường này nghĩa là mở bán toàn bộ ghế trong mẫu.
     * @return Chuyển về trang quản lý suất với ID suất vừa được tạo.
     */
    @PostMapping("/admin/showtimes/from-template")
    public String create(
            Principal principal,
            @RequestParam Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                    LocalDateTime startsAt,
            @RequestParam BigDecimal basePrice,
            @RequestParam BigDecimal vipSurcharge,
            @RequestParam(required = false) Set<String> disabledSeats) {
        Showtime showtime =
                service.createFromTemplate(
                        accounts.account(principal.getName()),
                        movieId,
                        startsAt,
                        basePrice,
                        vipSurcharge,
                        disabledSeats == null ? Collections.<String>emptySet() : disabledSeats);
        return "redirect:/admin/showtimes?created=" + showtime.getId();
    }

    /**
     * Hiển thị chi tiết một đơn để admin theo dõi hoạt động đặt vé.
     *
     * @param id ID đơn admin muốn xem từ phần {id} trong URL, tương ứng Booking.id.
     * @param model Model do Spring truyền vào để đưa dữ liệu từ controller sang trang HTML.
     * @return Tên trang booking-detail với thông tin của đơn được chọn.
     */
    @GetMapping("/admin/bookings/{id}")
    public String booking(@PathVariable Long id, Model model) {
        model.addAllAttributes(
                views.detail(bookings.findById(id).orElseThrow(BusinessException::missing)));
        return "booking-detail";
    }
}
