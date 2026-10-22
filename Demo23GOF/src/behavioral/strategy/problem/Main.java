package behavioral.strategy.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

// Điểm chạy Problem: mỗi lần tính phí, truyền tên loại giao hàng vào cùng service.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        double distance = 10; // Quãng đường giao từ cửa hàng đến người nhận: 10 km.
        NumberFormat moneyFormat = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));
        ShippingService shippingService = new ShippingService();

        System.out.println("=== WITHOUT STRATEGY ===");
        System.out.println("Distance: " + distance + " km");
        System.out.println();

        // Service tự chọn công thức dựa trên chuỗi được truyền vào.
        double standardFee = shippingService.calculateShippingFee("STANDARD", distance);
        System.out.println("Current shipping type: Standard");
        System.out.println("Shipping fee: " + moneyFormat.format(standardFee) + " VND");

        System.out.println();
        double expressFee = shippingService.calculateShippingFee("EXPRESS", distance);
        System.out.println("Change shipping type -> Express");
        System.out.println("Shipping fee: " + moneyFormat.format(expressFee) + " VND");

        System.out.println();
        double sameDayFee = shippingService.calculateShippingFee("SAME_DAY", distance);
        System.out.println("Change shipping type -> Same-day");
        System.out.println("Shipping fee: " + moneyFormat.format(sameDayFee) + " VND");
    }
}
