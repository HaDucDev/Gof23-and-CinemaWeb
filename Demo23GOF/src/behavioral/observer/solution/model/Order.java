package behavioral.observer.solution.model;

// Dữ liệu đơn hàng: id là mã đơn, status là trạng thái hiện tại.
public class Order {
    private final String id; // Mã đơn hàng được gửi cùng sự kiện cho các Observer.
    // Trạng thái hiện tại; đổi dữ liệu tại đây chưa tự phát thông báo.
    private OrderStatus status;

    public Order(String id) {
        this.id = id;
        this.status = OrderStatus.NEW;
    }

    public String getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    // Chỉ đổi dữ liệu; OrderService sẽ chủ động phát thông báo sau đó.
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
