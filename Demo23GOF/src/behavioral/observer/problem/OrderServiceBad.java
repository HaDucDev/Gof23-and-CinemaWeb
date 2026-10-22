package behavioral.observer.problem;

import behavioral.observer.problem.model.Order;
import behavioral.observer.problem.model.OrderStatus;
import behavioral.observer.problem.service.EmailService;
import behavioral.observer.problem.service.LoyaltyService;
import behavioral.observer.problem.service.SmsService;

// PROBLEM: vừa đổi trạng thái vừa biết và gọi trực tiếp tất cả bên quan tâm.
public class OrderServiceBad {
    // Bên gửi email bị OrderService gọi trực tiếp.
    private final EmailService emailService = new EmailService();
    // Bên gửi SMS bị OrderService gọi trực tiếp.
    private final SmsService smsService = new SmsService();
    // Bên cộng điểm bị OrderService gọi trực tiếp.
    private final LoyaltyService loyaltyService = new LoyaltyService();

    public void deliverOrder(Order order) {
        order.setStatus(OrderStatus.DELIVERED);
        System.out.println("[ORDER] Đơn hàng #" + order.getId() + " -> " + order.getStatus());

        // Thêm bên nhận mới thì phải bổ sung field và lời gọi tại đây.
        System.out.println();
        System.out.println("[OrderServiceBad] Gọi trực tiếp EmailService");
        emailService.sendDeliveredEmail(order);

        System.out.println();
        System.out.println("[OrderServiceBad] Gọi trực tiếp SmsService");
        smsService.sendDeliveredSms(order);

        System.out.println();
        System.out.println("[OrderServiceBad] Gọi trực tiếp LoyaltyService");
        loyaltyService.addPoint(order);
    }
}
