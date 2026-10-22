package structural.decorator.problem.receipt;

// Hóa đơn có logo, mã QR và dấu PAID, được viết thành một class tổ hợp riêng.
public class LogoQrWatermarkReceipt extends BasicReceipt {
    public void print() {
        super.print();
        System.out.println("Logo: MY SHOP");
        System.out.println("QR Code: [PAYMENT QR]");
        System.out.println("Watermark: PAID");
    }
}
