package structural.decorator.solution.decorator;

import java.util.Objects;

import structural.decorator.solution.component.Receipt;

// Lớp cha của các lớp bọc hóa đơn; giữ một Receipt để chuyển lời gọi in vào bên trong.
public abstract class ReceiptDecorator implements Receipt {
    // Object được bọc: hóa đơn cơ bản hoặc Decorator khác.
    protected final Receipt receipt;

    protected ReceiptDecorator(Receipt receipt) {
        // Chỉ ghi nhớ object bên trong; constructor chưa thực hiện việc in.
        this.receipt = Objects.requireNonNull(receipt, "Hóa đơn được bọc không được null.");
    }

    public void print() {
        // Chuyển lời gọi đến object được bọc, giữ lại chức năng đã có.
        receipt.print();
    }
}
