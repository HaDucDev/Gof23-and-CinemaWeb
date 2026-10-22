package behavioral.state.solution.state;

import behavioral.state.solution.order.Order;

// Đơn đã thanh toán: cho phép giao hàng hoặc hủy đơn kèm hoàn tiền.
// Thao tác không ghi đè sẽ dùng cách từ chối mặc định của AbstractOrderState.
public class PaidState extends AbstractOrderState {
    // Cùng một đơn hàng được truyền qua các State để chuyển trạng thái cho nó.
    private final Order order;

    public PaidState(Order order) {
        this.order = order;
    }

    @Override
    public void ship() {
        System.out.println("Bắt đầu giao hàng.");
        order.setState(new ShippingState(order));
    }

    @Override
    public void cancel() {
        System.out.println("Hủy đơn và hoàn tiền.");
        // Hủy cả đơn và hoàn tiền; đơn kết thúc ở Cancelled, không quay về New.
        order.setState(new CancelledState());
    }

    @Override
    public String getName() {
        return "PAID - Đã thanh toán";
    }
}
