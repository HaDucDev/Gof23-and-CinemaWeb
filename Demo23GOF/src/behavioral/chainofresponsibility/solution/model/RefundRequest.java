package behavioral.chainofresponsibility.solution.model;

import java.text.NumberFormat;
import java.util.Locale;

// Dữ liệu yêu cầu hoàn tiền; amount là số tiền VND, được truyền nguyên qua các cấp.
public class RefundRequest {
    // Số tiền cần hoàn, đơn vị VND; chuyển nguyên yêu cầu qua chuỗi xử lý.
    private final long amount;

    public RefundRequest(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số tiền hoàn phải lớn hơn 0.");
        }
        this.amount = amount;
    }

    public long getAmount() {
        return amount;
    }

    // Chỉ định dạng để hiển thị, không thay đổi số tiền của yêu cầu.
    public String getFormattedAmount() {
        return NumberFormat.getIntegerInstance(new Locale("vi", "VN")).format(amount) + " VND";
    }
}
