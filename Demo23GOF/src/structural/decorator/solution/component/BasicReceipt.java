package structural.decorator.solution.component;

// Đại diện hóa đơn cơ bản: in sản phẩm và tổng tiền trước khi thêm chức năng.
public class BasicReceipt implements Receipt {
    public void print() {
        System.out.println("=== BASIC RECEIPT ===");
        System.out.println("Product: Coffee");
        System.out.println("Total: 50,000 VND");
    }
}
