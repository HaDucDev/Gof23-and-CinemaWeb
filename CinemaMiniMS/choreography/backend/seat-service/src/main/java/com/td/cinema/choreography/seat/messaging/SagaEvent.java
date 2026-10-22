package com.td.cinema.choreography.seat.messaging;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/**
 * Nội dung JSON gửi qua RabbitMQ; ba service giữ cùng hợp đồng để vẫn build độc lập.
 * eventId nhận diện một sự kiện để chống xử lý trùng; correlationId liên kết chuỗi theo đơn.
 * operationId phân biệt từng lượt Pay/hủy của cùng bookingId, tránh sự kiện cũ sửa thao tác mới.
 * type đồng thời là routing key khi gửi tới saga.events; operationType là PAY/CANCEL_PAID/CANCEL_UNPAID.
 */
public class SagaEvent {
    public int version = 1;
    public UUID eventId;
    public UUID correlationId;
    public UUID bookingId;
    public UUID holdId;
    public UUID operationId;
    public UUID paymentId;
    public String type;
    public String operationType;
    public Long accountId;
    public Long revision;
    public BigDecimal amount;
    public String paymentMethod;
    public String reason;
    public String holdStatus;
    public String occurredAt = Instant.now().toString();

    /**
     * Tạo sự kiện phản hồi, giữ thông tin đơn/thao tác và sinh eventId ổn định từ sự kiện gốc + loại phản hồi.
     * Phát lại cùng phản hồi sẽ giữ cùng ID để consumer phía sau nhận diện bản giao trùng.
     */
    public SagaEvent response(String nextType) {
        SagaEvent e = new SagaEvent();
        e.eventId = UUID.nameUUIDFromBytes((eventId + "|" + nextType).getBytes(StandardCharsets.UTF_8));
        e.correlationId = correlationId;
        e.bookingId = bookingId;
        e.holdId = holdId;
        e.operationId = operationId;
        e.paymentId = paymentId;
        e.type = nextType;
        e.operationType = operationType;
        e.accountId = accountId;
        e.revision = revision;
        e.amount = amount;
        e.paymentMethod = paymentMethod;
        e.reason = reason;
        e.holdStatus = holdStatus;
        return e;
    }

    /** Kiểm tra dữ liệu tối thiểu trước nghiệp vụ; SagaListener còn đối chiếu với metadata AMQP. */
    public void validate() {
        if (version != 1 || eventId == null || correlationId == null || bookingId == null
                || holdId == null || accountId == null || type == null
                || (!"HoldExpired".equals(type) && (operationId == null || operationType == null)))
            throw new IllegalArgumentException("Invalid Saga event envelope");
        if ("PaymentRequested".equals(type) || "SeatPaymentReady".equals(type)) {
            if (revision == null || amount == null || amount.signum() <= 0 || paymentMethod == null)
                throw new IllegalArgumentException("Missing payment facts");
        }
    }
}
