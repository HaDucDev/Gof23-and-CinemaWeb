package structural.decorator.problem.receipt;

// Hóa đơn có logo, được viết thành class riêng trong bản Problem.
public class LogoReceipt extends BasicReceipt {
    public void print() {
        super.print();
        System.out.println("Logo: MY SHOP");
    }
}
