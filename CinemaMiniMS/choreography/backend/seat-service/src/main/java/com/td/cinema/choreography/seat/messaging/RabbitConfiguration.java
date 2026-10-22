package com.td.cinema.choreography.seat.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Khai báo exchange/queue/binding của Seat trên cùng broker và vhost với Booking, Payment.
 * Thông tin kết nối nằm ở spring.rabbitmq trong application.yaml và .env riêng của Seat.
 * RabbitAdmin do Spring Boot tạo sẽ khai báo các thành phần này khi mở kết nối tới RabbitMQ;
 * không cần thêm queue bằng tay trên trang quản lý. SagaListener mới là nơi đăng ký consumer.
 */
@Configuration
public class RabbitConfiguration {
    // Exchange chung, định tuyến theo routing key là tên sự kiện, ví dụ PaymentRefunded.
    public static final String EXCHANGE = "saga.events";
    // Queue riêng của Seat; khi Seat tắt, message đã vào queue này chờ Seat chạy lại để nhận.
    public static final String QUEUE = "cinema.choreo.seat";

    /** Tạo khai báo topology; không gọi nghiệp vụ giữ, bán hay trả ghế trong cấu hình này. */
    @Bean
    public Declarables sagaTopology() {
        // durable=true, autoDelete=false: khai báo exchange được giữ qua lần broker khởi động lại.
        TopicExchange events = new TopicExchange(EXCHANGE, true, false);
        DirectExchange dead = new DirectExchange("saga.dead", true, false);
        // Durable giữ khai báo queue; EventPublisher còn gửi message dạng PERSISTENT.
        // Listener từ chối không requeue sẽ đưa message qua saga.dead với routing key bằng tên queue.
        Queue queue = QueueBuilder.durable(QUEUE).deadLetterExchange("saga.dead")
                .deadLetterRoutingKey(QUEUE).build();
        // Queue .dead giữ message lỗi để kiểm tra; hiện không có consumer tự động xử lý lại.
        Queue failures = QueueBuilder.durable(QUEUE + ".dead").build();
        java.util.List<Declarable> declarations = new java.util.ArrayList<Declarable>();
        declarations.add(events); declarations.add(dead); declarations.add(queue); declarations.add(failures);
        declarations.add(BindingBuilder.bind(failures).to(dead).with(QUEUE));
        // Binding chọn sự kiện cần đưa vào queue Seat; nhiều service có thể nhận cùng một sự kiện.
        // PaymentRequested -> kiểm tra phiên giữ ghế; PaymentSucceeded -> xác nhận bán ghế.
        // PaymentFailed -> trả ghế giữ; PaymentRefunded -> trả ghế đã bán của yêu cầu CANCEL_PAID.
        // BookingCancellationRequested được nhận cả hai loại hủy, nhưng Seat chỉ trả ngay khi CANCEL_UNPAID.
        for (String event : new String[] { "PaymentRequested", "PaymentSucceeded", "PaymentFailed", "BookingCancellationRequested", "PaymentRefunded" })
            declarations.add(BindingBuilder.bind(queue).to(events).with(event));
        return new Declarables(declarations);
    }
}
