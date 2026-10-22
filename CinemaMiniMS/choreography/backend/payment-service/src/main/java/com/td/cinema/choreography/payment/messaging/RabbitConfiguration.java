package com.td.cinema.choreography.payment.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Khai báo exchange/queue/binding của Payment trên cùng broker và vhost với Booking, Seat.
 * Host/port/tài khoản/vhost lấy từ application.yaml và .env riêng của Payment.
 * RabbitAdmin do Spring Boot tạo khai báo các thành phần này khi mở kết nối tới broker;
 * không cần tạo queue bằng tay. SagaListener đăng ký consumer để Payment thực sự nhận message.
 */
@Configuration
public class RabbitConfiguration {
    // Exchange chung nhận sự kiện; routing key là event.type, không phải tên service nhận.
    public static final String EXCHANGE = "saga.events";
    // Queue riêng của Payment để nhận yêu cầu thu/hoàn; không dùng chung queue với Booking hoặc Seat.
    public static final String QUEUE = "cinema.choreo.payment";

    /** Khai báo đường đi của message nghiệp vụ và message bị listener từ chối. */
    @Bean
    public Declarables sagaTopology() {
        // durable=true, autoDelete=false: khai báo exchange được giữ qua lần broker khởi động lại.
        TopicExchange events = new TopicExchange(EXCHANGE, true, false);
        DirectExchange dead = new DirectExchange("saga.dead", true, false);
        // Queue durable còn tồn tại khi Payment tắt; message đã vào queue chờ consumer quay lại.
        // Cấu hình listener không requeue message bị từ chối; DLX/DLK chuyển nó tới queue .dead.
        Queue queue = QueueBuilder.durable(QUEUE).deadLetterExchange("saga.dead")
                .deadLetterRoutingKey(QUEUE).build();
        // Queue lỗi không có consumer tự xử lý lại. PaymentFailed hợp lệ không tự đi vào queue này.
        Queue failures = QueueBuilder.durable(QUEUE + ".dead").build();
        java.util.List<Declarable> declarations = new java.util.ArrayList<Declarable>();
        declarations.add(events); declarations.add(dead); declarations.add(queue); declarations.add(failures);
        declarations.add(BindingBuilder.bind(failures).to(dead).with(QUEUE));
        // Binding chọn routing key đưa vào queue Payment:
        // SeatPaymentReady -> Seat đã cho phép thu tiền; Payment không thu ngay từ PaymentRequested.
        // BookingCancellationRequested -> chỉ hoàn tiền khi operationType là CANCEL_PAID.
        for (String event : new String[] { "SeatPaymentReady", "BookingCancellationRequested" })
            declarations.add(BindingBuilder.bind(queue).to(events).with(event));
        return new Declarables(declarations);
    }
}
