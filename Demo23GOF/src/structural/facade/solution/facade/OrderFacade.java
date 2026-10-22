package structural.facade.solution.facade;

import structural.facade.solution.model.Order;
import structural.facade.solution.service.DeliveryService;
import structural.facade.solution.service.PaymentService;
import structural.facade.solution.service.RestaurantService;

// Đầu mối đặt món: phối hợp nhà hàng, thanh toán và giao món theo đúng thứ tự (Facade).
public class OrderFacade {
    // Service tạo đơn và xác nhận món còn được phục vụ.
    private final RestaurantService restaurantService = new RestaurantService();
    // Service thanh toán đơn bằng số dư ví khách.
    private final PaymentService paymentService = new PaymentService();
    // Service sắp xếp giao món cho đơn đã thanh toán.
    private final DeliveryService deliveryService = new DeliveryService();

    // Đầu mối nghiệp vụ: Client chỉ gửi yêu cầu đặt món và nhận kết quả.
    public Order placeOrder(String dishName, int quantity, int roomNumber) {
        Order order = restaurantService.createOrder(dishName, quantity, roomNumber);

        // Facade giữ thứ tự xử lý và quyết định dừng khi nhà hàng từ chối.
        if (!restaurantService.confirmOrder(order)) {
            return order;
        }

        // Thanh toán thất bại thì trả kết quả, không yêu cầu giao món.
        if (!paymentService.pay(order)) {
            return order;
        }

        deliveryService.arrangeDelivery(order);
        return order;
    }
}
