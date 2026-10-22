package behavioral.observer.problem;

import behavioral.observer.problem.model.Order;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Bản Problem có Main riêng: giao một đơn và gọi trực tiếp ba service.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("===== PROBLEM: GỌI TRỰC TIẾP =====");
        System.out.println();

        OrderServiceBad orderService = new OrderServiceBad();
        Order order = new Order("ORD-001");
        orderService.deliverOrder(order);

        System.out.println();
        System.out.println(
                "Vấn đề: mỗi khi có thêm một bên quan tâm, OrderServiceBad lại phải biết và gọi thêm bên đó.");
    }
}
