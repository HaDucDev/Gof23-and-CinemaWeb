package com.td.cinema.choreography.booking.messaging;
import com.td.cinema.choreography.booking.service.saga.BookingTransactionService;
import com.td.cinema.choreography.booking.client.AccountClient;
import com.td.cinema.choreography.booking.util.Data;
import java.util.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Cửa nhận sự kiện của Booking: RabbitMQ giao message từ cinema.choreo.booking vào receive().
 * Listener dùng kết nối spring.rabbitmq đã cấu hình, không phải REST controller để frontend gọi.
 * Luồng tiếp theo: đọc JSON -> kiểm tra envelope -> chống trùng/local transaction -> observe().
 */
@Component
public class SagaListener {
    private final ObjectMapper json;
    private final EventTransactions transactions;
    private final BookingTransactionService participant;
    private final AccountClient accounts;
    public SagaListener(ObjectMapper json, EventTransactions transactions, BookingTransactionService participant, AccountClient accounts) {
        this.json = json; this.transactions = transactions; this.participant = participant; this.accounts = accounts;
    }
    /**
     * Annotation tạo bộ nhận message (Consumer). Với concurrency=1, một instance Booking
     * thường có một consumer ở queue này. Tắt service làm mất consumer, không xóa queue durable.
     * Chế độ ACK mặc định AUTO của Spring: container ACK sau khi handler hoàn tất thành công;
     * đây không phải tự ACK ngay khi broker vừa giao message. Nếu dừng ở breakpoint, message
     * có thể đang Unacked. Nếu handler ném lỗi và không requeue, message được đưa sang .dead.
     * Đặt breakpoint ở dòng đọc JSON bên dưới để kiểm tra sự kiện Booking thực sự nhận được.
     */
    @RabbitListener(queues = RabbitConfiguration.QUEUE)
    public void receive(Message message) {
        // Body là JSON SagaEvent; các thuộc tính AMQP được publisher gắn riêng bên ngoài body.
        SagaEvent event = json.readValue(message.getBody(), SagaEvent.class);
        event.validate();
        // Tên sự kiện và eventId trong JSON phải khớp routing key/messageId, tránh xử lý sai envelope.
        if (!event.type.equals(message.getMessageProperties().getReceivedRoutingKey())
                || !event.eventId.toString().equals(message.getMessageProperties().getMessageId()))
            throw new IllegalArgumentException("Envelope does not match AMQP properties");
        // Đọc danh sách admin qua Account REST để chuẩn bị thông báo, không gọi REST thu/hoàn/trả ghế.
        List<Long> admins = new ArrayList<Long>();
        for (Map<String,Object> admin : accounts.admins()) admins.add(Data.number(admin, "accountId"));
        // handle() chống trùng theo eventId; observe() ghi nhận kết quả và chốt đơn khi đủ bằng chứng.
        transactions.handle(event, e -> participant.observe(e, admins));
    }
}
