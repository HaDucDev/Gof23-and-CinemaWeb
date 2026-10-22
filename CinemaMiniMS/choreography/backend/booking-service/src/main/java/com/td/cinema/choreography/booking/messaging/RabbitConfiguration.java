package com.td.cinema.choreography.booking.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Khai báo cách Booking nhận sự kiện trên RabbitMQ, không cấu hình địa chỉ kết nối tại đây.
 * Host/port/tài khoản/vhost nằm ở spring.rabbitmq trong application.yaml và .env của Booking.
 * Spring Boot tạo RabbitAdmin; khi mở kết nối, RabbitAdmin khai báo các thành phần trong
 * Declarables này trên broker. Không cần tạo exchange/queue/binding thủ công trên trang quản lý.
 */
@Configuration
public class RabbitConfiguration {
    // Exchange chung của ba service: nhận message rồi định tuyến theo routing key = tên sự kiện.
    // Exchange không phải hàng đợi riêng của Booking; một sự kiện có thể vào nhiều queue phù hợp.
    public static final String EXCHANGE = "saga.events";
    // Queue riêng của Booking để tổng hợp kết quả tiền/ghế và cập nhật trạng thái đơn.
    // SagaListener đăng ký consumer tại queue này; Queue trên broker và Consumer là hai thứ khác nhau.
    public static final String QUEUE = "cinema.choreo.booking";

    /** Khai báo exchange, queue nghiệp vụ, queue lỗi và các quy tắc nối chúng với nhau. */
    @Bean
    public Declarables sagaTopology() {
        // durable=true, autoDelete=false: giữ khai báo exchange qua lần khởi động lại broker.
        TopicExchange events = new TopicExchange(EXCHANGE, true, false);
        // Exchange nhận message bị từ chối; khác với sự kiện nghiệp vụ PaymentFailed hợp lệ.
        DirectExchange dead = new DirectExchange("saga.dead", true, false);
        // Queue durable vẫn tồn tại khi Booking tắt; message đã được broker nhận có thể nằm chờ.
        // Khi listener từ chối không requeue, DLX/DLK chuyển message tới queue lỗi của Booking.
        Queue queue = QueueBuilder.durable(QUEUE).deadLetterExchange("saga.dead")
                .deadLetterRoutingKey(QUEUE).build();
        // Queue .dead phục vụ kiểm tra lỗi; code hiện tại không đăng ký consumer tự xử lý lại nó.
        Queue failures = QueueBuilder.durable(QUEUE + ".dead").build();
        java.util.List<Declarable> declarations = new java.util.ArrayList<Declarable>();
        declarations.add(events); declarations.add(dead); declarations.add(queue); declarations.add(failures);
        declarations.add(BindingBuilder.bind(failures).to(dead).with(QUEUE));
        // Binding là quy tắc: routing key nào khớp thì exchange đưa một bản message vào queue này.
        // PaymentSucceeded/Failed/Refunded: kết quả thu/hoàn tiền; SeatConfirmed/Released: kết quả ghế.
        // SeatPaymentRejected: Seat không cho bắt đầu thanh toán; HoldExpired: phiên giữ ghế hết hạn.
        // Booking chỉ chốt CONFIRMED/CANCELLED khi observe() đã có đủ bằng chứng nghiệp vụ tương ứng.
        for (String event : new String[] { "SeatPaymentRejected", "PaymentSucceeded", "PaymentFailed", "SeatConfirmed", "SeatReleased", "PaymentRefunded", "HoldExpired" })
            declarations.add(BindingBuilder.bind(queue).to(events).with(event));
        return new Declarables(declarations);
    }
}
