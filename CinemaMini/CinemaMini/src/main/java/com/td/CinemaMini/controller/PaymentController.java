package com.td.CinemaMini.controller;

import com.td.CinemaMini.model.*;
import com.td.CinemaMini.model.enums.BookingStatus;
import com.td.CinemaMini.model.enums.PaymentMethod;
import com.td.CinemaMini.service.account.AccountService;
import com.td.CinemaMini.service.booking.*;
import com.td.CinemaMini.service.payment.PaymentService;
import java.security.Principal;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PaymentController {
    private final AccountService accounts;
    private final BookingService bookings;
    private final BookingViewService views;
    private final PaymentService payments;
    private final CancellationService cancellation;

    public PaymentController(
            AccountService accounts,
            BookingService bookings,
            BookingViewService views,
            PaymentService payments,
            CancellationService cancellation) {
        this.accounts = accounts;
        this.bookings = bookings;
        this.views = views;
        this.payments = payments;
        this.cancellation = cancellation;
    }

    /**
     * Hiển thị form chọn phương thức thanh toán cho đơn còn chờ thanh toán.
     *
     * @param id ID đơn cần thanh toán lấy từ phần {id} trong URL, tương ứng Booking.id.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @param model Model do Spring cấp để đưa thông tin đơn, danh sách phương thức và mã yêu cầu ra
     *     form.
     * @return Trang payment nếu đơn còn PENDING; chuyển sang chi tiết đơn nếu đơn ở trạng thái
     *     khác.
     */
    @GetMapping("/bookings/{id}/payment")
    public String page(@PathVariable Long id, Principal principal, Model model) {
        Customer customer = accounts.customer(accounts.account(principal.getName()));
        model.addAllAttributes(views.detail(bookings.owned(id, customer)));
        Booking current = (Booking) model.getAttribute("booking");
        if (current.getStatus() != BookingStatus.PENDING) {
            return "redirect:/bookings/" + id;
        }
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("requestKey", UUID.randomUUID().toString());
        model.addAttribute("cancelKey", UUID.randomUUID().toString());
        return "payment";
    }

    /**
     * Nhận phương thức khách chọn từ form và thanh toán đơn thuộc khách đang đăng nhập.
     *
     * @param id ID đơn cần thanh toán lấy từ phần {id} trong URL, tương ứng Booking.id.
     * @param paymentMethod Mã phương thức từ form thanh toán, ví dụ MOMO, VISA hoặc CASH; Spring
     *     đổi sang enum PaymentMethod.
     * @param requestKey Mã của lần thanh toán này từ trường ẩn của form. Gửi lại cùng thao tác phải
     *     giữ nguyên mã để tránh thu tiền hai lần.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @return Chuyển về trang chi tiết đơn sau khi xử lý thanh toán.
     */
    @PostMapping("/bookings/{id}/payments")
    public String pay(
            @PathVariable Long id,
            @RequestParam PaymentMethod paymentMethod,
            @RequestParam String requestKey,
            Principal principal) {
        UserAccount actor = accounts.account(principal.getName());
        payments.pay(actor, accounts.customer(actor), id, paymentMethod, requestKey);
        return "redirect:/bookings/" + id;
    }

    /**
     * Hủy đơn của khách đang đăng nhập; service hoàn tiền nếu đơn đã thanh toán và còn được phép
     * hủy.
     *
     * @param id ID đơn cần hủy lấy từ phần {id} trong URL, tương ứng Booking.id.
     * @param requestKey Mã của lần hủy này từ trường ẩn của form. Dùng lại mã khi gửi lại cùng thao
     *     tác để tránh xử lý hủy hoặc hoàn tiền hai lần.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @return Chuyển về trang chi tiết đơn sau khi xử lý hủy.
     */
    @PostMapping("/bookings/{id}/cancel")
    public String cancel(
            @PathVariable Long id, @RequestParam String requestKey, Principal principal) {
        UserAccount actor = accounts.account(principal.getName());
        cancellation.cancel(actor, accounts.customer(actor), id, requestKey);
        return "redirect:/bookings/" + id;
    }
}
