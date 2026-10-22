package behavioral.observer.problem.service;

import behavioral.observer.problem.model.Order;

// Bên cộng điểm sau khi giao thành công; demo dùng mức cố định 10 điểm.
public class LoyaltyService {
    public void addPoint(Order order) {
        System.out.println("[LOYALTY] Cộng 10 điểm cho đơn hàng #" + order.getId());
    }
}
