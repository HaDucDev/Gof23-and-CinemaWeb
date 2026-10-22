package behavioral.observer.problem.service;

import behavioral.observer.problem.model.Order;

// Bên nhận thông tin giao hàng để gửi email; demo mô phỏng bằng console.
public class EmailService {
    public void sendDeliveredEmail(Order order) {
        System.out.println("[EMAIL] Gửi email: Đơn hàng #" + order.getId() + " đã giao thành công");
    }
}
