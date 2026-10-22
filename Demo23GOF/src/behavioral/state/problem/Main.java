package behavioral.state.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Chạy ba tình huống; mỗi tình huống dùng một Order riêng để dễ theo dõi.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("=== CHƯA ÁP DỤNG STATE ===");

        System.out.println();
        System.out.println("--- 1. Hủy đơn đã thanh toán và hoàn tiền ---");
        Order paymentOrder = new Order();
        System.out.println("Trạng thái: " + paymentOrder.getStateName());

        System.out.println("> pay()");
        paymentOrder.pay();
        System.out.println("Trạng thái: " + paymentOrder.getStateName());

        System.out.println("> cancel()");
        paymentOrder.cancel();
        System.out.println("Trạng thái: " + paymentOrder.getStateName());

        System.out.println();
        System.out.println("--- 2. Giao hàng, hoàn tất và thử thao tác không hợp lệ ---");
        Order deliveryOrder = new Order();
        System.out.println("Trạng thái: " + deliveryOrder.getStateName());

        System.out.println("> pay()");
        deliveryOrder.pay();
        System.out.println("Trạng thái: " + deliveryOrder.getStateName());

        System.out.println("> ship()");
        deliveryOrder.ship();
        System.out.println("Trạng thái: " + deliveryOrder.getStateName());

        System.out.println("> cancel()");
        deliveryOrder.cancel();
        System.out.println("Trạng thái: " + deliveryOrder.getStateName());

        System.out.println("> complete()");
        deliveryOrder.complete();
        System.out.println("Trạng thái: " + deliveryOrder.getStateName());

        System.out.println("> pay()");
        deliveryOrder.pay();
        System.out.println("Trạng thái: " + deliveryOrder.getStateName());

        System.out.println();
        System.out.println("--- 3. Hủy đơn khi chưa thanh toán ---");
        Order cancellationOrder = new Order();
        System.out.println("Trạng thái: " + cancellationOrder.getStateName());

        System.out.println("> cancel()");
        cancellationOrder.cancel();
        System.out.println("Trạng thái: " + cancellationOrder.getStateName());

        System.out.println("> pay()");
        cancellationOrder.pay();
        System.out.println("Trạng thái: " + cancellationOrder.getStateName());
    }
}
