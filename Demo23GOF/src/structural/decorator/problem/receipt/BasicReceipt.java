package structural.decorator.problem.receipt;

// Đại diện hóa đơn cơ bản: in sản phẩm và tổng tiền trước khi thêm chức năng.
public class BasicReceipt {
    public void print() {
        System.out.println("=== BASIC RECEIPT ===");
        System.out.println("Product: Coffee");
        System.out.println("Total: 50,000 VND");
    }
}
