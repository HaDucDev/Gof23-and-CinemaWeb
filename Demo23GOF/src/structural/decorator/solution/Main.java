package structural.decorator.solution;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import structural.decorator.solution.component.BasicReceipt;
import structural.decorator.solution.component.Receipt;
import structural.decorator.solution.decorator.LogoDecorator;
import structural.decorator.solution.decorator.QrCodeDecorator;
import structural.decorator.solution.decorator.WatermarkDecorator;

// Điểm chạy Decorator: ghép các lớp bọc để bổ sung chức năng in hóa đơn.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("=== BASIC ===");
        Receipt basicReceipt = new BasicReceipt();
        basicReceipt.print();

        System.out.println();
        System.out.println("=== LOGO ===");
        Receipt logoBase = new BasicReceipt();
        Receipt logoReceipt = new LogoDecorator(logoBase);
        logoReceipt.print();

        System.out.println();
        System.out.println("=== STORE A: LOGO + QR ===");
        // Cửa hàng A: tạo hóa đơn cơ bản, bọc Logo, rồi bọc thêm QR.
        Receipt storeABasicReceipt = new BasicReceipt();
        Receipt storeALogoReceipt = new LogoDecorator(storeABasicReceipt);
        Receipt storeAReceipt = new QrCodeDecorator(storeALogoReceipt);
        storeAReceipt.print();

        System.out.println();
        System.out.println("=== STORE B: LOGO + WATERMARK ===");
        // Cửa hàng B: ghép Logo và dấu PAID bằng các Decorator dùng lại.
        Receipt storeBBasicReceipt = new BasicReceipt();
        Receipt storeBLogoReceipt = new LogoDecorator(storeBBasicReceipt);
        Receipt storeBReceipt = new WatermarkDecorator(storeBLogoReceipt);
        storeBReceipt.print();

        System.out.println();
        System.out.println("=== LOGO + QR + WATERMARK ===");
        // Mỗi dòng tạo một lớp bọc; chỉ gọi print() trên lớp ngoài cùng.
        Receipt fullBasicReceipt = new BasicReceipt();
        Receipt fullLogoReceipt = new LogoDecorator(fullBasicReceipt);
        Receipt fullLogoQrReceipt = new QrCodeDecorator(fullLogoReceipt);
        Receipt fullReceipt = new WatermarkDecorator(fullLogoQrReceipt);
        fullReceipt.print();
    }
}
