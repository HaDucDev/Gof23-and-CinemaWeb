package structural.adapter.problem2;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import structural.adapter.problem2.model.Order;
import structural.adapter.problem2.model.OrderItem;
import structural.adapter.problem2.printer.NewPrinter;

// Đại diện phần mềm bán hàng; phải tự chuyển dữ liệu và gọi API máy in mới.
public class SalesApplication {
    // THAY ĐỔI 1: đổi field và constructor từ ReceiptPrinter sang NewPrinter.
    // Tham chiếu máy in mới; Client phải gọi API nhận văn bản.
    private final NewPrinter printer;

    public SalesApplication(NewPrinter printer) {
        this.printer = Objects.requireNonNull(printer, "Máy in không được null.");
    }

    public void checkout(Order order) {
        // THAY ĐỔI 2: Client phải tự chuyển Order thành văn bản.
        String text = formatReceipt(order);
        // THAY ĐỔI 3: bỏ printReceipt(order), gọi printTicket(text, copies).
        printer.printTicket(text, 1);
    }

    // THAY ĐỔI 2: trước đây OldPrinter nhận Order và tự tạo chuỗi hóa đơn.
    // Nay NewPrinter chỉ nhận String, nên Client phải chuyển Order thành text.
    // Nội dung và cách trình bày hóa đơn vẫn giữ nguyên.
    private String formatReceipt(Order order) {
        NumberFormat money = NumberFormat.getIntegerInstance(new Locale("vi", "VN"));
        StringBuilder text = new StringBuilder();
        text.append("===== HÓA ĐƠN SIÊU THỊ =====\n");
        text.append("Mã đơn: ").append(order.getId()).append("\n");

        for (OrderItem item : order.getItems()) {
            text.append("- ")
                    .append(item.getName())
                    .append(": ")
                    .append(item.getQuantity())
                    .append(" ")
                    .append(item.getUnit())
                    .append(" x ")
                    .append(money.format(item.getUnitPrice()))
                    .append(" VNĐ")
                    .append(" = ")
                    .append(money.format(item.getTotalPrice()))
                    .append(" VNĐ\n");
        }

        text.append("Tổng tiền: ").append(money.format(order.getTotalPrice())).append(" VNĐ\n");
        text.append("==========================");
        return text.toString();
    }
}
