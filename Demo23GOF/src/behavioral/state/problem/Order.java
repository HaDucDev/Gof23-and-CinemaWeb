package behavioral.state.problem;

// Bản Problem: mỗi thao tác đều kiểm tra trạng thái để chọn cách xử lý và trạng thái tiếp theo.
public class Order {
    // Tên trạng thái dùng để so sánh và hiển thị.
    private static final String NEW = "NEW - Mới tạo";
    // Có thể bắt đầu giao hoặc hủy kèm hoàn tiền.
    private static final String PAID = "PAID - Đã thanh toán";
    // Chỉ được chuyển tiếp sang hoàn tất.
    private static final String SHIPPING = "SHIPPING - Đang giao";
    // Trạng thái kết thúc sau khi giao xong.
    private static final String COMPLETED = "COMPLETED - Đã hoàn tất";
    // Trạng thái kết thúc sau khi hủy đơn.
    private static final String CANCELLED = "CANCELLED - Đã hủy";

    private String currentState = NEW; // Trạng thái hiện tại của riêng đơn này.

    public String getStateName() {
        return currentState;
    }

    // Cùng các trạng thái lại được kiểm tra ở từng thao tác phía dưới.
    public void pay() {
        if (NEW.equals(currentState)) {
            System.out.println("Thanh toán thành công.");
            currentState = PAID;
        } else if (PAID.equals(currentState)) {
            System.out.println("Đơn đã thanh toán, không thể thanh toán lại.");
        } else if (SHIPPING.equals(currentState)) {
            System.out.println("Đơn đang giao hàng, không thể thanh toán.");
        } else if (COMPLETED.equals(currentState)) {
            System.out.println("Đơn đã hoàn tất, không thể thanh toán.");
        } else {
            System.out.println("Đơn đã hủy, không thể thanh toán.");
        }
    }

    public void ship() {
        if (NEW.equals(currentState)) {
            System.out.println("Đơn chưa thanh toán, không thể giao hàng.");
        } else if (PAID.equals(currentState)) {
            System.out.println("Bắt đầu giao hàng.");
            currentState = SHIPPING;
        } else if (SHIPPING.equals(currentState)) {
            System.out.println("Đơn đang giao hàng, không thể giao lần nữa.");
        } else if (COMPLETED.equals(currentState)) {
            System.out.println("Đơn đã hoàn tất, không thể giao hàng.");
        } else {
            System.out.println("Đơn đã hủy, không thể giao hàng.");
        }
    }

    public void complete() {
        if (NEW.equals(currentState)) {
            System.out.println("Đơn chưa thanh toán, không thể hoàn tất.");
        } else if (PAID.equals(currentState)) {
            System.out.println("Đơn chưa được giao, không thể hoàn tất.");
        } else if (SHIPPING.equals(currentState)) {
            System.out.println("Hoàn tất đơn hàng.");
            currentState = COMPLETED;
        } else if (COMPLETED.equals(currentState)) {
            System.out.println("Đơn đã hoàn tất, không thể hoàn tất lần nữa.");
        } else {
            System.out.println("Đơn đã hủy, không thể hoàn tất.");
        }
    }

    // Cùng lệnh hủy: New hủy ngay, Paid hủy kèm hoàn tiền, các trạng thái khác từ chối.
    public void cancel() {
        if (NEW.equals(currentState)) {
            System.out.println("Hủy đơn thành công.");
            currentState = CANCELLED;
        } else if (PAID.equals(currentState)) {
            System.out.println("Hủy đơn và hoàn tiền.");
            currentState = CANCELLED;
        } else if (SHIPPING.equals(currentState)) {
            System.out.println("Đơn đang giao hàng, không thể hủy đơn.");
        } else if (COMPLETED.equals(currentState)) {
            System.out.println("Đơn đã hoàn tất, không thể hủy đơn.");
        } else {
            System.out.println("Đơn đã hủy, không thể hủy lần nữa.");
        }
    }
}
