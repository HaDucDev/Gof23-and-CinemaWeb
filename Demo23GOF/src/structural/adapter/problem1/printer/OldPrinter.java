package structural.adapter.problem1.printer;

import java.text.NumberFormat;
import java.util.Locale;

import structural.adapter.problem1.model.Order;
import structural.adapter.problem1.model.OrderItem;

// Mô phỏng máy in cũ: nhận Order và tạo nội dung hóa đơn để in.
public class OldPrinter implements ReceiptPrinter {
    @Override
    public void printReceipt(Order order) {
        // Máy in cũ nhận Order trực tiếp và tự tạo nội dung hóa đơn.
        System.out.println("[Máy in cũ] Bản 1/1");
        System.out.println(formatReceipt(order));
    }

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
