package structural.decorator.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import structural.decorator.problem.receipt.BasicReceipt;
import structural.decorator.problem.receipt.LogoQrReceipt;
import structural.decorator.problem.receipt.LogoQrWatermarkReceipt;
import structural.decorator.problem.receipt.LogoReceipt;
import structural.decorator.problem.receipt.LogoWatermarkReceipt;

// Điểm chạy hóa đơn: chọn class riêng cho từng tổ hợp Logo, QR và dấu PAID.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("=== BASIC ===");
        new BasicReceipt().print();

        System.out.println();
        System.out.println("=== LOGO ===");
        new LogoReceipt().print();

        System.out.println();
        System.out.println("=== STORE A: LOGO + QR ===");
        // Cửa hàng A cần logo và mã QR thanh toán: chọn class cho tổ hợp đó.
        new LogoQrReceipt().print();

        System.out.println();
        System.out.println("=== STORE B: LOGO + WATERMARK ===");
        // Cửa hàng B cần logo và dấu PAID: lại có một class tổ hợp riêng.
        new LogoWatermarkReceipt().print();

        System.out.println();
        System.out.println("=== LOGO + QR + WATERMARK ===");
        new LogoQrWatermarkReceipt().print();

        // Nếu thêm DiscountInfo, có thể lại cần LogoDiscountReceipt,
        // LogoQrDiscountReceipt... cho các tổ hợp muốn sử dụng.
    }
}
