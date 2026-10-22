package com.td.cinema.choreography.booking.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.*;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Chiều gửi message của booking-service: Local Transaction commit -> RabbitTemplate -> saga.events.
 * RabbitTemplate được Spring Boot cấu hình từ spring.rabbitmq.*; constructor chỉ nhận bean có sẵn.
 * Exchange định tuyến theo event.type tới từng queue đã binding, không gửi REST tới service nhận.
 * Gửi RabbitMQ không nằm trong transaction SQL; broker confirm và consumer ACK là hai xác nhận khác nhau.
 */
@Component
public class EventPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(EventPublisher.class);
    private final RabbitTemplate rabbit;
    private final ObjectMapper json;
    public EventPublisher(RabbitTemplate rabbit, ObjectMapper json) { this.rabbit = rabbit; this.json = json; }

    /**
     * Đăng ký publish khi transaction hiện tại commit thành công; rollback sẽ không publish.
     * Cơ chế này chưa có durable outbox để tự khôi phục khi tiến trình chết giữa commit và gửi.
     */
    public void afterCommit(final SagaEvent event) {
        if (!TransactionSynchronizationManager.isActualTransactionActive())
            throw new IllegalStateException("Saga events must follow a local transaction");
        // Chỉ đăng ký callback lúc này; publish thực sự chạy sau COMMIT, không chạy nếu SQL rollback.
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() { publish(event); }
        });
    }

    /** Gửi message và chờ broker xác nhận; xác nhận này chưa phải kết quả nghiệp vụ của consumer. */
    private void publish(SagaEvent event) {
        // JSON là body; messageId/correlationId/type là metadata AMQP phục vụ kiểm tra và truy vết.
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setContentEncoding("UTF-8");
        // Kết hợp queue durable để broker lưu message bền vững; không làm DB và RabbitMQ thành một transaction.
        properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        properties.setMessageId(event.eventId.toString());
        // correlationId liên kết chuỗi sự kiện của cùng đơn; eventId nhận diện riêng một sự kiện.
        properties.setCorrelationId(event.correlationId.toString());
        properties.setType(event.type);
        // Đối tượng chờ confirm của riêng lần gửi, không phải sự kiện phản hồi từ Seat/Payment/Booking.
        CorrelationData confirmation = new CorrelationData(event.eventId.toString());
        try {
            // Đặt breakpoint ở đây để xem exchange, routing key (event.type), body và các ID trước khi gửi.
            rabbit.send(RabbitConfiguration.EXCHANGE, event.type,
                    new Message(json.writeValueAsString(event).getBytes(StandardCharsets.UTF_8), properties), confirmation);
            // Chờ tối đa 5 giây để broker xác nhận tiếp nhận, không đợi consumer hoàn tất nghiệp vụ.
            CorrelationData.Confirm result = confirmation.getFuture().get(5, TimeUnit.SECONDS);
            // NACK hoặc message bị trả vì không có queue phù hợp đều được xem là publish thất bại.
            if (!result.ack() || confirmation.getReturned() != null)
                throw new IllegalStateException("Broker rejected/unroutable Saga event " + event.eventId);
            LOG.info("SAGA published type={} event={} booking={} operation={}", event.type, event.eventId, event.bookingId, event.operationId);
        } catch (Exception failure) {
            // SQL đã commit trước callback này; lỗi publish không tự rollback dữ liệu đã lưu.
            // Chưa có durable outbox để tự gửi bù khi tiến trình chết giữa commit và publish.
            LOG.error("SAGA publish failed after DB commit; manual review required: event={} booking={}", event.eventId, event.bookingId, failure);
            throw new IllegalStateException("Saga publish failed after local commit", failure);
        }
    }
}
