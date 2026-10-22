package com.td.cinema.choreography.booking.service;

import com.td.cinema.choreography.booking.client.PaymentClient;
import com.td.cinema.choreography.booking.dto.request.BookingCreateRequest;
import com.td.cinema.choreography.booking.dto.request.CancelRequest;
import com.td.cinema.choreography.booking.dto.request.PayRequest;
import com.td.cinema.choreography.booking.service.saga.BookingCheckoutService;
import com.td.cinema.choreography.booking.service.saga.BookingTransactionService;
import com.td.cinema.choreography.booking.util.Data;

import org.springframework.stereotype.Service;

import java.util.*;

/** Điểm gọi nghiệp vụ đặt vé từ controller; kết hợp dữ liệu đơn, luồng xử lý và kết quả Payment. */
@Service
public class BookingService {
    private final BookingTransactionService store;
    private final BookingCheckoutService workflow;
    private final PaymentClient payment;

    /** Nhận nơi lưu dữ liệu đơn, luồng xử lý tương ứng bản demo và client đọc Payment. */
    public BookingService(
            BookingTransactionService store,
            BookingCheckoutService workflow,
            PaymentClient payment) {
        this.store = store;
        this.workflow = workflow;
        this.payment = payment;
    }

    /** Chuyển yêu cầu tạo đơn sang luồng xử lý của bản demo hiện tại. */
    public UUID create(long actor, String key, BookingCreateRequest request) {
        return workflow.create(actor, key, request);
    }

    /** Lấy dữ liệu đơn và kiểm tra quyền xem trước khi trả cho controller. */
    public Map<String, Object> view(UUID id, long actor, boolean admin) {
        return store.view(id, actor, admin);
    }

    /** Ghép thông tin đơn từ Booking với kết quả và sổ thu/hoàn lấy từ Payment. */
    public Map<String, Object> detail(UUID id, long actor, boolean admin) {
        Map<String, Object> result = store.view(id, actor, admin);
        result.put("payment", payment.byBooking(id));
        return result;
    }

    /** Lấy lịch sử đơn theo tài khoản khách hàng. */
    public List<Map<String, Object>> mine(long actor) {
        return store.mine(actor);
    }

    /** Lấy các đơn gần đây phục vụ màn hình quản trị. */
    public List<Map<String, Object>> adminList() {
        return store.adminList();
    }

    /** Đọc nhật ký của một đơn sau khi kiểm tra quyền khách hoặc quyền quản trị. */
    public List<Map<String, Object>> timeline(UUID id, long actor, boolean admin) {
        return store.timeline(id, actor, admin);
    }

    /**
     * Tiếp nhận yêu cầu thanh toán để xử lý bất đồng bộ; phản hồi đã nhận chưa đồng nghĩa nghiệp vụ
     * đã hoàn tất.
     */
    public Map<String, Object> pay(UUID id, long actor, String key, PayRequest request) {
        workflow.start(id, actor, key, "PAY", Data.paymentMethod(request.paymentMethod));
        return accepted(id, actor);
    }

    /**
     * Tiếp nhận yêu cầu hủy đơn hoặc hủy vé đã thanh toán để xử lý bất đồng bộ; phản hồi đã nhận
     * chưa đồng nghĩa nghiệp vụ đã hoàn tất.
     */
    public Map<String, Object> cancel(UUID id, long actor, String key, CancelRequest request) {
        workflow.start(
                id,
                actor,
                key,
                "CANCEL",
                request == null || request.reason == null ? "Khach huy ve" : request.reason);
        return accepted(id, actor);
    }

    /**
     * Trả trạng thái hiện tại cùng statusUrl để frontend theo dõi yêu cầu bất đồng bộ; chưa khẳng
     * định thanh toán đã xong.
     */
    private Map<String, Object> accepted(UUID id, long actor) {
        Map<String, Object> result = store.view(id, actor, false);
        result.put("statusUrl", "/api/bookings/" + id);
        return result;
    }
}
