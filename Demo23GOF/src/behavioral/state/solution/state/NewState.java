package behavioral.state.solution.state;

import behavioral.state.solution.order.Order;

// Đơn mới tạo: cho phép thanh toán hoặc hủy đơn.
// Thao tác không ghi đè sẽ dùng cách từ chối mặc định của AbstractOrderState.
public class NewState extends AbstractOrderState {
    // Cùng một đơn hàng được truyền qua các State để chuyển trạng thái cho nó.
    private final Order order;

    public NewState(Order order) {
        this.order = order;
    }

    @Override
    public void pay() {
        System.out.println("Thanh toán thành công.");
        order.setState(new PaidState(order));
    }

    @Override
    public void cancel() {
        System.out.println("Hủy đơn thành công.");
        order.setState(new CancelledState());
    }

    @Override
    public String getName() {
        return "NEW - Mới tạo";
    }
}
