package structural.facade.solution;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import structural.facade.solution.facade.OrderFacade;
import structural.facade.solution.model.Order;

// Client đặt món qua OrderFacade: gửi yêu cầu và nhận kết quả xử lý.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("=== SOLUTION: Client đặt món qua OrderFacade ===");
        OrderFacade facade = new OrderFacade();
        runScenario(facade, "1. Đặt món thành công", "Cơm gà", 2, 302);
        runScenario(facade, "2. Nhà hàng hết món", "Phở bò", 1, 302);
        runScenario(facade, "3. Ví không đủ tiền", "Cơm gà", 1, 302);
    }

    private static void runScenario(
            OrderFacade facade, String title, String dishName, int quantity, int roomNumber) {
        System.out.println();
        System.out.println("--- " + title + " ---");

        // Một lời gọi nghiệp vụ; Client không gọi hoặc phối hợp các Service.
        Order order = facade.placeOrder(dishName, quantity, roomNumber);
        System.out.println(order);
    }
}
