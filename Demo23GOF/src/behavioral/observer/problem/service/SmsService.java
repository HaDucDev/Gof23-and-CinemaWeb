package behavioral.observer.problem.service;

import behavioral.observer.problem.model.Order;

// Bên nhận thông tin giao hàng để gửi SMS.
public class SmsService {
    public void sendDeliveredSms(Order order) {
        System.out.println("[SMS] Gửi SMS: Đơn hàng #" + order.getId() + " đã giao");
    }
}
