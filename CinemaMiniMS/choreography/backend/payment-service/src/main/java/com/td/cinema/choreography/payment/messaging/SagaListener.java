package com.td.cinema.choreography.payment.messaging;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Cửa nhận sự kiện của Payment từ cinema.choreo.payment, dùng kết nối spring.rabbitmq.
 * SeatPaymentReady dẫn tới thu tiền; BookingCancellationRequested loại CANCEL_PAID dẫn tới hoàn tiền.
 * JSON hợp lệ được chuyển qua EventTransactions để chống trùng trước khi chạy PaymentParticipant.
 */
@Component
public class SagaListener {
    private final ObjectMapper json;
    private final EventTransactions transactions;
    private final PaymentParticipant participant;
    public SagaListener(ObjectMapper json, EventTransactions transactions, PaymentParticipant participant) {
        this.json = json; this.transactions = transactions; this.participant = participant;
    }
    /**
     * Đăng ký Consumer. Với concurrency=1, một instance Payment thường có một consumer ở queue này.
     * ACK mặc định AUTO của Spring được gửi sau khi handler hoàn tất thành công, không phải ngay
     * lúc nhận message. Dừng ở breakpoint có thể giữ message ở Unacked, dù SQL đã commit.
     * Handler ném lỗi với cấu hình không requeue sẽ chuyển message sang .dead; PaymentFailed
     * là kết quả nghiệp vụ hợp lệ, vẫn được xử lý và ACK bình thường nếu handler thành công.
     * Đặt breakpoint ở dòng đọc JSON để xem yêu cầu trước khi thu/hoàn tiền.
     */
    @RabbitListener(queues = RabbitConfiguration.QUEUE)
    public void receive(Message message) {
        // Body JSON và thuộc tính AMQP phải mô tả cùng một sự kiện.
        SagaEvent event = json.readValue(message.getBody(), SagaEvent.class);
        event.validate();
        if (!event.type.equals(message.getMessageProperties().getReceivedRoutingKey())
                || !event.eventId.toString().equals(message.getMessageProperties().getMessageId()))
            throw new IllegalArgumentException("Envelope does not match AMQP properties");
        // Thu/hoàn tiền, receipt và phản hồi cùng local transaction; EventPublisher gửi sau commit.
        transactions.handle(event, participant::onEvent);
    }
}
