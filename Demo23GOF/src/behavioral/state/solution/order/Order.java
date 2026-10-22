package behavioral.state.solution.order;

import java.util.Objects;

import behavioral.state.solution.state.NewState;
import behavioral.state.solution.state.OrderState;

// Context: đại diện một đơn hàng, giữ State hiện tại và chuyển yêu cầu cho State đó.
public final class Order {
    // Object đang quyết định đơn được làm gì ở thời điểm hiện tại.
    private OrderState currentState;

    public Order() {
        // this là chính Order đang được tạo; NewState giữ tham chiếu tới đơn này.
        currentState = new NewState(this);
    }

    // Concrete State gọi phương thức này sau một thao tác hợp lệ.
    public void setState(OrderState state) {
        currentState = Objects.requireNonNull(state, "Order state must not be null.");
    }

    public String getStateName() {
        return currentState.getName();
    }

    public void pay() {
        currentState.pay();
    }

    public void ship() {
        currentState.ship();
    }

    public void complete() {
        currentState.complete();
    }

    public void cancel() {
        currentState.cancel();
    }
}
