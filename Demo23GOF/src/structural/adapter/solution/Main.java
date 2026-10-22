package structural.adapter.solution;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import structural.adapter.solution.app.SalesApplication;
import structural.adapter.solution.model.Order;
import structural.adapter.solution.model.OrderItem;
import structural.adapter.solution.printer.NewPrinter;
import structural.adapter.solution.printer.PrinterAdapter;
import structural.adapter.solution.printer.ReceiptPrinter;

// Điểm chạy Adapter: ghép máy in mới vào cách gọi ReceiptPrinter đã có.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("=== SOLUTION: Dùng máy in mới, giữ Client như Problem 1 ===");

        Order order =
                new Order(
                        "HD001",
                        Arrays.asList(
                                new OrderItem("Sữa tươi", 2, "hộp", 25000),
                                new OrderItem("Bánh mì", 1, "ổ", 15000)));

        System.out.println(
                "Luồng gọi: SalesApplication.checkout() -> PrinterAdapter.printReceipt(order) -> NewPrinter.printTicket(text, 1).");
        System.out.println();

        // So với Problem 1, chỉ thay cách khởi tạo máy in; Client giữ nguyên.
        ReceiptPrinter printer = new PrinterAdapter(new NewPrinter());
        SalesApplication application = new SalesApplication(printer);
        application.checkout(order);
    }
}
