package behavioral.observer.problem.model;

// Dữ liệu đơn hàng: id là mã đơn, status là trạng thái hiện tại.
public class Order {
    private final String id; // Mã đơn hàng để các bên nhận biết đơn vừa được giao.
    private OrderStatus status; // Trạng thái hiện tại; khởi tạo là NEW trong constructor.

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
