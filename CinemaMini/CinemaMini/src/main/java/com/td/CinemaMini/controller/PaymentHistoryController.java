package com.td.CinemaMini.controller;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.repository.*;
import com.td.CinemaMini.service.account.AccountService;
import com.td.CinemaMini.service.booking.*;
import java.security.Principal;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import static com.td.CinemaMini.dto.Values.map;

@Controller
public class PaymentHistoryController {
    private final PaymentRepository payments;
    private final BookingRepository bookings;
    private final BookingService bookingService;
    private final AccountService accounts;

    public PaymentHistoryController(
            PaymentRepository payments,
            BookingRepository bookings,
            BookingService bookingService,
            AccountService accounts) {
        this.payments = payments;
        this.bookings = bookings;
        this.bookingService = bookingService;
        this.accounts = accounts;
    }

    /**
     * Hiển thị lịch sử thu và hoàn tiền của một đơn thuộc khách đang đăng nhập.
     *
     * @param id ID đơn lấy từ phần {id} trong URL, tương ứng Booking.id; service kiểm tra đơn thuộc
     *     khách hiện tại.
     * @param principal Thông tin tài khoản đang đăng nhập do Spring Security truyền vào; getName()
     *     trả về email đăng nhập.
     * @param model Model do Spring truyền vào để đưa dữ liệu từ controller sang trang HTML.
     * @return Trang payment-history với các giao dịch của đơn theo thứ tự thời gian tăng dần.
     */
    @GetMapping("/bookings/{id}/payments")
    public String own(@PathVariable Long id, Principal principal, Model model) {
        bookingService.owned(id, accounts.customer(accounts.account(principal.getName())));
        return page(payments.findByBookingIdOrderByCreatedAtAsc(id), null, null, model);
    }

    /**
     * Hiển thị lịch sử giao dịch cho admin và lọc theo mã đơn hoặc loại thu/hoàn tiền.
     *
     * @param bookingCode Mã đơn hoặc một phần mã đơn lấy từ query string; không bắt buộc, để trống
     *     thì không lọc theo mã.
     * @param operation Loại giao dịch từ query string: PAYMENT là thu tiền, REFUND là hoàn tiền; để
     *     trống thì lấy cả hai.
     * @param model Model do Spring cấp để đưa các giao dịch đã lọc và giá trị bộ lọc ra giao diện.
     * @return Trang payment-history với giao dịch mới nhất trước.
     */
    @GetMapping("/admin/payments")
    public String admin(
            @RequestParam(required = false) String bookingCode,
            @RequestParam(required = false) String operation,
            Model model) {
        return page(payments.findAllByOrderByCreatedAtDesc(), bookingCode, operation, model);
    }

    private String page(List<Payment> source, String code, String operation, Model model) {
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Payment payment : source) {
            Booking booking =
                    bookings.findById(payment.getBookingId())
                            .orElseThrow(BusinessException::missing);
            if (code != null
                    && !code.trim().isEmpty()
                    && !booking.getCode().contains(code.trim().toUpperCase(Locale.ROOT))) {
                continue;
            }
            if (operation != null
                    && !operation.isEmpty()
                    && !operation.equals(payment.getOperationType())) {
                continue;
            }
            rows.add(map("payment", payment, "booking", booking));
        }
        model.addAttribute("rows", rows);
        model.addAttribute("bookingCode", code);
        model.addAttribute("operation", operation);
        return "payment-history";
    }
}
