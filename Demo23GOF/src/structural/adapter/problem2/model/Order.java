package structural.adapter.problem2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Đại diện đơn hàng siêu thị: mã đơn và danh sách các dòng sản phẩm.
public class Order {
    private final String id; // Mã đơn hàng, ví dụ: "HD001".
    private final List<OrderItem> items; // Danh sách các dòng sản phẩm trong đơn hàng.

    public Order(String id, List<OrderItem> items) {
        this.id = id;
        // Sao chép và bảo vệ danh sách khỏi việc thêm, xóa hoặc thay thế phần tử.
        this.items = Collections.unmodifiableList(new ArrayList<>(items));
    }

    public String getId() {
        return id;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    // Tổng tiền đơn hàng = tổng thành tiền của tất cả dòng sản phẩm.
    public long getTotalPrice() {
        long total = 0;
        for (OrderItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }
}
