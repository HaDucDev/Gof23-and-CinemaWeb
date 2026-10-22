package structural.decorator.problem.receipt;

// Hóa đơn có mã QR thanh toán, được viết thành class riêng trong bản Problem.
public class QrCodeReceipt extends BasicReceipt {
    public void print() {
        super.print();
        System.out.println("QR Code: [PAYMENT QR]");
    }
}
