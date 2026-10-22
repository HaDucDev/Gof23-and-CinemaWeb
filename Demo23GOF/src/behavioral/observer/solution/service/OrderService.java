package behavioral.observer.solution.service;

import behavioral.observer.solution.model.Order;
import behavioral.observer.solution.model.OrderStatus;
import behavioral.observer.solution.subject.OrderSubject;

// Đổi trạng thái và phát thông báo; chỉ biết Subject, không biết từng Observer cụ thể.
public class OrderService {
    // Đầu mối phát sự kiện cho danh sách Observer mà Main đã đăng ký.
    private final OrderSubject subject;

    public OrderService(OrderSubject subject) {
        this.subject = subject; // Giữ đúng Subject mà Main đã đăng ký các Observer.
    }

    public void deliverOrder(Order order) {
        order.setStatus(OrderStatus.DELIVERED);
        System.out.println("[ORDER] Đơn hàng #" + order.getId() + " -> " + order.getStatus());
        // Bên trong Subject mới gọi observer.update(order).
        subject.notifyObservers(order);
    }
}
