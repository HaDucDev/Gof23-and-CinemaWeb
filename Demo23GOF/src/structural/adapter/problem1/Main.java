package structural.adapter.problem1;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import structural.adapter.problem1.model.Order;
import structural.adapter.problem1.model.OrderItem;
import structural.adapter.problem1.printer.OldPrinter;
import structural.adapter.problem1.printer.ReceiptPrinter;

// Điểm chạy bán hàng với máy in cũ nhận trực tiếp dữ liệu Order.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("=== PROBLEM 1: Hệ thống đang dùng máy in cũ, in bình thường ===");

        Order order =
                new Order(
                        "HD001",
                        Arrays.asList(
                                new OrderItem("Sữa tươi", 2, "hộp", 25000),
                                new OrderItem("Bánh mì", 1, "ổ", 15000)));

        System.out.println(
                "Luồng gọi: SalesApplication.checkout() -> OldPrinter.printReceipt(order).");
        System.out.println();

        ReceiptPrinter printer = new OldPrinter();
        SalesApplication application = new SalesApplication(printer);
        application.checkout(order);
    }
}
