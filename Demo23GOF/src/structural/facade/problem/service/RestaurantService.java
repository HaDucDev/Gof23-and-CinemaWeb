package structural.facade.problem.service;

import structural.facade.problem.model.Order;
import structural.facade.problem.model.OrderStatus;

// Mô phỏng nhà hàng: cung cấp giá, tạo đơn và xác nhận hoặc từ chối món.
public class RestaurantService {
    private int nextOrderNumber = 1; // Số thứ tự dùng tạo mã cho đơn đặt món tiếp theo.

    public Order createOrder(String dishName, int quantity, int roomNumber) {
        // Giá do nhà hàng cung cấp; Client chỉ gửi món, số lượng và số phòng.
        long unitPrice = getUnitPrice(dishName);
        Order order =
                new Order("DH00" + nextOrderNumber++, dishName, quantity, roomNumber, unitPrice);

        System.out.println(
                "[Nhà hàng] Tạo "
                        + order.getId()
                        + ": "
                        + quantity
                        + " x "
                        + dishName
                        + ", phòng "
                        + roomNumber
                        + ", giá "
                        + unitPrice
                        + " đồng/phần.");
        return order;
    }

    public boolean confirmOrder(Order order) {
        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalStateException("Chỉ xác nhận đơn vừa được tạo.");
        }

        // Menu demo: cơm gà đang phục vụ, phở bò tạm hết món.
        if ("Phở bò".equals(order.getDishName())) {
            order.setStatus(OrderStatus.REJECTED);
            System.out.println("[Nhà hàng] Từ chối " + order.getId() + ": Phở bò tạm hết món.");
            return false;
        }

        order.setStatus(OrderStatus.CONFIRMED);
        System.out.println("[Nhà hàng] Đã xác nhận " + order.getId() + ".");
        return true;
    }

    private long getUnitPrice(String dishName) {
        if ("Cơm gà".equals(dishName)) {
            return 80000;
        }
        if ("Phở bò".equals(dishName)) {
            return 90000;
        }
        throw new IllegalArgumentException("Món không có trong menu: " + dishName);
    }
}
