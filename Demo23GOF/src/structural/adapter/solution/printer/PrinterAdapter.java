package structural.adapter.solution.printer;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import structural.adapter.solution.model.Order;
import structural.adapter.solution.model.OrderItem;

// Bộ chuyển tiếp từ yêu cầu in Order sang API văn bản của máy in mới (Adapter).
public class PrinterAdapter implements ReceiptPrinter {
    // Tham chiếu máy in mới mà Adapter sẽ gửi văn bản và số bản in tới.
    private final NewPrinter newPrinter;

    public PrinterAdapter(NewPrinter newPrinter) {
        this.newPrinter = Objects.requireNonNull(newPrinter, "Máy in không được null.");
    }

    @Override
    public void printReceipt(Order order) {
        // Đổi Order thành text, rồi chuyển lời gọi sang API của máy in mới.
        String text = formatReceipt(order);
        // Demo in một bản; Client không phải truyền tham số riêng của máy in.
        newPrinter.printTicket(text, 1);
    }

    // Phần chuyển đổi nằm ở Adapter, không đặt trong SalesApplication như Problem 2.
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
