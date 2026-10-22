package structural.decorator.problem.receipt;

// Hóa đơn có dấu PAID, được viết thành class riêng trong bản Problem.
public class WatermarkReceipt extends BasicReceipt {
    public void print() {
        super.print();
        System.out.println("Watermark: PAID");
    }
}
