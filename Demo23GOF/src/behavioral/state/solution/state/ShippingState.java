package behavioral.state.solution.state;

import behavioral.state.solution.order.Order;

// Đơn đang giao: cho phép hoàn tất sau khi giao thành công.
// Thao tác không ghi đè sẽ dùng cách từ chối mặc định của AbstractOrderState.
public class ShippingState extends AbstractOrderState {
    // Cùng một đơn hàng được truyền qua các State để chuyển trạng thái cho nó.
    private final Order order;

    public ShippingState(Order order) {
        this.order = order;
    }

    @Override
    public void complete() {
        System.out.println("Hoàn tất đơn hàng.");
        order.setState(new CompletedState());
    }

    @Override
    public String getName() {
        return "SHIPPING - Đang giao";
    }
}
