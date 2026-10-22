package com.td.cinema.booking.service;

import com.td.cinema.booking.client.PaymentClient;
import com.td.cinema.booking.dto.request.BookingCreateRequest;
import com.td.cinema.booking.dto.request.CancelRequest;
import com.td.cinema.booking.dto.request.PayRequest;
import com.td.cinema.booking.util.Data;

import org.springframework.stereotype.Service;

import java.util.*;

/** Điểm gọi nghiệp vụ đặt vé từ controller; kết hợp dữ liệu đơn, luồng xử lý và kết quả Payment. */
@Service
public class BookingService {
    private final BookingStore store;
    private final BookingWithoutSagaService workflow;
    private final PaymentClient payment;

    /** Nhận nơi lưu dữ liệu đơn, luồng xử lý tương ứng bản demo và client đọc Payment. */
    public BookingService(
            BookingStore store,
            BookingWithoutSagaService workflow,
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

    /** Xử lý thanh toán theo luồng REST đồng bộ rồi trả chi tiết kết quả hiện có của đơn. */
    public Map<String, Object> pay(UUID id, long actor, String key, PayRequest request) {
        workflow.execute(id, actor, key, "PAY", Data.paymentMethod(request.paymentMethod));
        return detail(id, actor, false);
    }

    /**
     * Xử lý hủy đơn hoặc hủy vé đã thanh toán theo luồng REST đồng bộ rồi trả chi tiết kết quả hiện
     * có của đơn.
     */
    public Map<String, Object> cancel(UUID id, long actor, String key, CancelRequest request) {
        workflow.execute(
                id,
                actor,
                key,
                "CANCEL",
                request == null || request.reason == null ? "Khach huy ve" : request.reason);
        return detail(id, actor, false);
    }

}
