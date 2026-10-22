package structural.decorator.problem.receipt;

// Hóa đơn có cả logo và mã QR, được viết thành một class tổ hợp riêng.
public class LogoQrReceipt extends BasicReceipt {
    public void print() {
        super.print();
        System.out.println("Logo: MY SHOP");
        System.out.println("QR Code: [PAYMENT QR]");
    }
}
