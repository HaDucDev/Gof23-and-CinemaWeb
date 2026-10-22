package com.td.cinema.choreography.seat.messaging;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Cửa nhận sự kiện của Seat từ queue cinema.choreo.seat, dùng kết nối spring.rabbitmq.
 * Luồng xử lý: JSON -> kiểm tra envelope -> EventTransactions -> SeatParticipant.onEvent().
 * Ví dụ PaymentRefunded làm Seat trả ghế rồi phát SeatReleased sau khi transaction commit.
 */
@Component
public class SagaListener {
    private final ObjectMapper json;
    private final EventTransactions transactions;
    private final SeatParticipant participant;
    public SagaListener(ObjectMapper json, EventTransactions transactions, SeatParticipant participant) {
        this.json = json; this.transactions = transactions; this.participant = participant;
    }
    /**
     * Đăng ký Consumer; với concurrency=1, một instance Seat thường có một consumer trên queue này.
     * Seat tắt -> Consumers=0, message đã vào queue nằm ở Ready; Seat chạy lại -> nhận message chờ.
     * ACK mặc định AUTO của Spring được gửi sau khi handler hoàn tất thành công, không gửi ngay
     * khi nhận message. Khi dừng ở breakpoint, message có thể ở Unacked, kể cả khi SQL đã commit.
     * Handler ném lỗi với cấu hình không requeue sẽ đưa message sang queue .dead.
     * Đặt breakpoint ở dòng đọc JSON để thấy message trước khi nghiệp vụ thay đổi trạng thái ghế.
     */
    @RabbitListener(queues = RabbitConfiguration.QUEUE)
    public void receive(Message message) {
        // Đọc body JSON và kiểm tra các thông tin Saga bắt buộc trước khi gọi nghiệp vụ.
        SagaEvent event = json.readValue(message.getBody(), SagaEvent.class);
        event.validate();
        // Publisher dùng event.type làm routing key và eventId làm messageId; hai nơi phải khớp.
        if (!event.type.equals(message.getMessageProperties().getReceivedRoutingKey())
                || !event.eventId.toString().equals(message.getMessageProperties().getMessageId()))
            throw new IllegalArgumentException("Envelope does not match AMQP properties");
        // Proxy @Transactional bao nghiệp vụ Seat, receipt chống trùng và phản hồi vào cùng local transaction.
        transactions.handle(event, participant::onEvent);
    }
}
