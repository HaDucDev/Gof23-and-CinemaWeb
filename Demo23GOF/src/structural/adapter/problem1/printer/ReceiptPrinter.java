package structural.adapter.problem1.printer;

import structural.adapter.problem1.model.Order;

// Interface khai báo printReceipt(Order), là cách gọi in mà phần mềm bán hàng sử dụng (Target của
// Adapter).
public interface ReceiptPrinter {
    // In hóa đơn từ dữ liệu Order theo cách gọi mà phần mềm bán hàng sử dụng.
    void printReceipt(Order order);
}
