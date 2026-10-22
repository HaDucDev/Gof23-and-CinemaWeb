package structural.facade.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import structural.facade.problem.model.Order;
import structural.facade.problem.service.DeliveryService;
import structural.facade.problem.service.PaymentService;
import structural.facade.problem.service.RestaurantService;

// Client đặt món: tự phối hợp nhà hàng, thanh toán và giao đến phòng.
public class Main {
    // Service tạo đơn và xác nhận món còn được phục vụ.
    private final RestaurantService restaurantService = new RestaurantService();
    // Service thanh toán đơn bằng số dư ví khách.
    private final PaymentService paymentService = new PaymentService();
    // Service sắp xếp giao món cho đơn đã thanh toán.
    private final DeliveryService deliveryService = new DeliveryService();

    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("=== PROBLEM: Client trực tiếp phối hợp ba Service ===");
        Main client = new Main();
        client.runScenario("1. Đặt món thành công", "Cơm gà", 2, 302);
        client.runScenario("2. Nhà hàng hết món", "Phở bò", 1, 302);
        client.runScenario("3. Ví không đủ tiền", "Cơm gà", 1, 302);
    }

    private void runScenario(String title, String dishName, int quantity, int roomNumber) {
        System.out.println();
        System.out.println("--- " + title + " ---");
        Order order = placeOrder(dishName, quantity, roomNumber);
        System.out.println(order);
    }

    // Đây là logic nằm trong Client / UI, chưa có đầu mối Facade riêng.
    private Order placeOrder(String dishName, int quantity, int roomNumber) {
        Order order = restaurantService.createOrder(dishName, quantity, roomNumber);

        // Client phải nhớ: xác nhận trước khi thanh toán; bị từ chối thì dừng.
        if (!restaurantService.confirmOrder(order)) {
            return order;
        }

        // Client phải kiểm tra thanh toán; thất bại thì không gọi giao món.
        if (!paymentService.pay(order)) {
            return order;
        }

        deliveryService.arrangeDelivery(order);
        return order;
    }
}
