package com.td.cinema.choreography.payment.controller;

import com.td.cinema.choreography.payment.dto.request.SimulationRequest;
import com.td.cinema.choreography.payment.dto.response.PaymentResponse;
import com.td.cinema.choreography.payment.mapper.ApiProjectionMapper;
import com.td.cinema.choreography.payment.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.*;

/** API nội bộ để cấu hình kết quả giả lập, thu tiền, hoàn tiền và đọc lịch sử Payment. */
@RestController
@RequestMapping("/internal")
public class PaymentController {
    private final PaymentService service;
    private final ApiProjectionMapper mapper;

    /** Nhận dịch vụ Payment và bộ chuyển đổi DTO phản hồi thanh toán. */
    public PaymentController(PaymentService service, ApiProjectionMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * Đọc cấu hình kết quả thanh toán giả lập đang giữ trong bộ nhớ Payment.
     * Đường dẫn: {@code GET /internal/payment-simulation}.
     */
    @GetMapping("/payment-simulation")
    public Map<String, Object> simulation() {
        return service.simulation();
    }

    /**
     * Nhận cấu hình SUCCESS/FAIL từ chức năng quản trị qua API nội bộ.
     * Đường dẫn: {@code PUT /internal/payment-simulation}.
     */
    @PutMapping("/payment-simulation")
    public Map<String, Object> configure(@Valid @RequestBody SimulationRequest request) {
        return service.configure(request);
    }

    /**
     * Lấy kết quả một lần thanh toán theo paymentId.
     * Đường dẫn: {@code GET /internal/payments/{id}}.
     */
    @GetMapping("/payments/{id}")
    public PaymentResponse get(@PathVariable UUID id) {
        return mapper.response(service.get(id), PaymentResponse.class);
    }

    /**
     * Lấy kết quả Payment cùng các dòng thu/hoàn gắn với một đơn.
     * Đường dẫn: {@code GET /internal/payments/by-booking/{bookingId}}.
     */
    @GetMapping("/payments/by-booking/{bookingId}")
    public Map<String, Object> byBooking(@PathVariable UUID bookingId) {
        return service.byBooking(bookingId);
    }

    /**
     * Lấy danh sách dòng sổ thu/hoàn với giới hạn số lượng.
     * Đường dẫn: {@code GET /internal/payments}.
     */
    @GetMapping("/payments")
    public List<Map<String, Object>> ledger(@RequestParam(defaultValue = "100") int limit) {
        return service.ledger(limit);
    }
}
