package structural.decorator.solution.decorator;

import structural.decorator.solution.component.Receipt;

// Lớp bọc bổ sung logo cửa hàng vào hóa đơn được truyền vào.
public class LogoDecorator extends ReceiptDecorator {
    public LogoDecorator(Receipt receipt) {
        super(receipt);
    }

    public void print() {
        // In chức năng đã có của object bên trong, rồi bổ sung chức năng của lớp này.
        super.print();
        System.out.println("Logo: MY SHOP");
    }
}
