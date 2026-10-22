package behavioral.strategy.solution;

import behavioral.strategy.solution.service.ShippingService;
import behavioral.strategy.solution.strategy.ExpressShippingStrategy;
import behavioral.strategy.solution.strategy.SameDayShippingStrategy;
import behavioral.strategy.solution.strategy.StandardShippingStrategy;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

// Điểm chạy Solution: giữ cùng service và thay Strategy khi chương trình đang chạy.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        double distance = 10; // Quãng đường giao từ cửa hàng đến người nhận: 10 km.
        NumberFormat moneyFormat = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));

        // Tạo cách tính cụ thể rồi truyền vào service; interface không được new trực tiếp.
        StandardShippingStrategy standardStrategy = new StandardShippingStrategy();
        ShippingService shippingService = new ShippingService(standardStrategy);

        System.out.println("=== WITH STRATEGY ===");
        System.out.println("Distance: " + distance + " km");
        System.out.println();

        double standardFee = shippingService.calculateShippingFee(distance);
        System.out.println("Current strategy: Standard");
        System.out.println("Shipping fee: " + moneyFormat.format(standardFee) + " VND");

        System.out.println();
        // Giữ service cũ, thay object tính phí đang được service sử dụng.
        ExpressShippingStrategy expressStrategy = new ExpressShippingStrategy();
        shippingService.setShippingStrategy(expressStrategy);
        double expressFee = shippingService.calculateShippingFee(distance);
        System.out.println("Change strategy -> Express");
        System.out.println("Shipping fee: " + moneyFormat.format(expressFee) + " VND");

        System.out.println();
        SameDayShippingStrategy sameDayStrategy = new SameDayShippingStrategy();
        shippingService.setShippingStrategy(sameDayStrategy);
        double sameDayFee = shippingService.calculateShippingFee(distance);
        System.out.println("Change strategy -> Same-day");
        System.out.println("Shipping fee: " + moneyFormat.format(sameDayFee) + " VND");
    }
}
