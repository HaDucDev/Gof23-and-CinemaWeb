package structural.adapter.problem2;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import structural.adapter.problem2.model.Order;
import structural.adapter.problem2.model.OrderItem;
import structural.adapter.problem2.printer.NewPrinter;

// Điểm chạy thay máy in mới: Client phải đổi dữ liệu Order thành văn bản.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("=== PROBLEM 2: Thay máy in mới, phải sửa Client ===");

        Order order =
                new Order(
                        "HD001",
                        Arrays.asList(
                                new OrderItem("Sữa tươi", 2, "hộp", 25000),
                                new OrderItem("Bánh mì", 1, "ổ", 15000)));

        System.out.println(
                "Luồng gọi: SalesApplication.checkout() tự đổi Order thành text, rồi gọi NewPrinter.printTicket(text, 1).");
        System.out.println();

        // THAY ĐỔI 4: khởi tạo NewPrinter thay cho OldPrinter của Problem 1.
        SalesApplication application = new SalesApplication(new NewPrinter());
        application.checkout(order);
    }
}
