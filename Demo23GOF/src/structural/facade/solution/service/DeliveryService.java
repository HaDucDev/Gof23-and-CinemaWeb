package structural.facade.solution.service;

import structural.facade.solution.model.Order;
import structural.facade.solution.model.OrderStatus;

// Mô phỏng việc sắp xếp giao món đến phòng, chỉ thực hiện cho đơn đã thanh toán.
public class DeliveryService {
    public void arrangeDelivery(Order order) {
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException("Chỉ giao món cho đơn đã thanh toán.");
        }

        order.setStatus(OrderStatus.DELIVERY_SCHEDULED);
        System.out.println(
                "[Giao món] Đã sắp xếp giao "
                        + order.getQuantity()
                        + " x "
                        + order.getDishName()
                        + " đến phòng "
                        + order.getRoomNumber()
                        + " cho "
                        + order.getId()
                        + ".");
    }
}
