package com.td.CinemaMini.controller;

import com.td.CinemaMini.dto.BookingRequest;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.service.account.AccountService;
import com.td.CinemaMini.service.booking.*;
import java.security.Principal;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BookingController {
    private final AccountService accounts;
    private final BookingService bookings;
    private final BookingFacade facade;
    private final BookingViewService views;

    public BookingController(
            AccountService accounts,
            BookingService bookings,
            BookingFacade facade,
            BookingViewService views) {
        this.accounts = accounts;
        this.bookings = bookings;
        this.facade = facade;
        this.views = views;
    }

    /**
     * Tạo đơn đặt vé từ phiên giữ ghế và thông tin liên hệ mà khách gửi từ form.
     *
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @param holdId ID phiên giữ ghế từ trường ẩn của form, tương ứng HoldSession.id.
     * @param showtimeId ID suất chiếu từ trường ẩn của form, tương ứng Showtime.id; phải khớp suất
     *     của phiên giữ ghế.
     * @param holdToken Mã UUID của phiên giữ ghế từ form; dùng cùng holdId và tài khoản để kiểm tra
     *     quyền sử dụng phiên.
     * @param revision Số phiên bản của phiên giữ ghế từ form; phải khớp dữ liệu hiện tại để tránh
     *     tạo đơn từ dữ liệu cũ.
     * @param seatIds Danh sách Seat.id từ form, gồm 1-6 ghế khác nhau và phải khớp các ghế khách
     *     đang giữ.
     * @param contactName Tên người liên hệ do khách nhập cho đơn vé; có thể khác tên trong hồ sơ
     *     Customer.
     * @param contactEmail Email liên hệ do khách nhập cho đơn vé; có thể khác email dùng đăng nhập.
     * @param contactPhone Số điện thoại liên hệ từ form; không bắt buộc.
     * @param note Ghi chú khách nhập cho đơn, tối đa 1000 ký tự; không bắt buộc.
     * @param requestKey Mã của lần tạo đơn này từ form. Gửi lại cùng yêu cầu phải giữ nguyên mã để
     *     tránh tạo trùng đơn.
     * @return Chuyển sang trang thanh toán của đơn vừa tạo hoặc đơn đã được xử lý với cùng yêu cầu.
     */
    @PostMapping("/bookings")
    public String create(
            Principal principal,
            @RequestParam Long holdId,
            @RequestParam Long showtimeId,
            @RequestParam UUID holdToken,
            @RequestParam long revision,
            @RequestParam List<Long> seatIds,
            @RequestParam String contactName,
            @RequestParam String contactEmail,
            @RequestParam(required = false) String contactPhone,
            @RequestParam(required = false) String note,
            @RequestParam String requestKey) {
        // principal.getName() là email đăng nhập do Spring Security cung cấp.
        // Lấy tài khoản đang gửi yêu cầu; từ chối nếu không tồn tại hoặc đã bị khóa.
        // ID tài khoản kết hợp với requestKey giúp nhận diện yêu cầu và tránh tạo trùng đơn.
        UserAccount actor = accounts.account(principal.getName());
        // Lấy hồ sơ người mua vé liên kết với tài khoản qua Customer.accountId = UserAccount.id.
        // Chỉ tài khoản có quyền USER và có hồ sơ khách hàng mới được tiếp tục tạo đơn.
        // Customer.id dùng kiểm tra quyền giữ ghế và xác định chủ đơn;
        // Customer.customerType dùng chọn mức ưu đãi khi tính tiền vé.
        Customer customer = accounts.customer(actor);
        // GoF: Builder nội bộ DTO kiểm tra đầu vào; Facade phối hợp toàn bộ use case.
        BookingRequest request =
                BookingRequest.builder()
                        .customerId(customer.getId())
                        .showtimeId(showtimeId)
                        .holdId(holdId)
                        .holdToken(holdToken)
                        .revision(revision)
                        .seatIds(seatIds)
                        .contactName(contactName)
                        .contactEmail(contactEmail)
                        .contactPhone(contactPhone)
                        .note(note)
                        .build();
        Booking booking = facade.placeBooking(actor, customer, request, requestKey);
        return "redirect:/bookings/" + booking.getId() + "/payment";
    }

    /**
     * Hiển thị danh sách đơn đặt vé của khách đang đăng nhập.
     *
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @param model Model do Spring truyền vào để đưa dữ liệu từ controller sang trang HTML.
     * @return Tên trang bookings với danh sách đơn của khách hiện tại.
     */
    @GetMapping("/bookings")
    public String list(Principal principal, Model model) {
        model.addAttribute(
                "rows", views.list(accounts.customer(accounts.account(principal.getName()))));
        return "bookings";
    }

    /**
     * Hiển thị chi tiết một đơn sau khi kiểm tra đơn thuộc khách đang đăng nhập.
     *
     * @param id ID đơn lấy từ phần {id} trong URL, tương ứng Booking.id.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @param model Model do Spring truyền vào để đưa dữ liệu từ controller sang trang HTML.
     * @return Tên trang booking-detail với thông tin đơn, phim, ghế và giao dịch liên quan.
     */
    @GetMapping("/bookings/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        Customer customer = accounts.customer(accounts.account(principal.getName()));
        model.addAllAttributes(views.detail(bookings.owned(id, customer)));
        model.addAttribute("requestKey", UUID.randomUUID().toString());
        return "booking-detail";
    }
}
