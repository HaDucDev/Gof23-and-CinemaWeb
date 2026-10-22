package structural.adapter.problem1;

import java.util.Objects;

import structural.adapter.problem1.model.Order;
import structural.adapter.problem1.printer.ReceiptPrinter;

// Đại diện phần mềm bán hàng, yêu cầu in hóa đơn qua giao diện ReceiptPrinter.
public class SalesApplication {
    // Tham chiếu bên in hóa đơn qua giao diện ReceiptPrinter.
    private final ReceiptPrinter printer;

    public SalesApplication(ReceiptPrinter printer) {
        this.printer = Objects.requireNonNull(printer, "Máy in không được null.");
    }

    public void checkout(Order order) {
        // Problem 1 và Solution giữ nguyên lời gọi này.
        printer.printReceipt(order);
    }
}
