package structural.decorator.problem.receipt;

// Hóa đơn có cả logo và dấu PAID, được viết thành một class tổ hợp riêng.
public class LogoWatermarkReceipt extends BasicReceipt {
    public void print() {
        super.print();
        System.out.println("Logo: MY SHOP");
        System.out.println("Watermark: PAID");
    }
}
