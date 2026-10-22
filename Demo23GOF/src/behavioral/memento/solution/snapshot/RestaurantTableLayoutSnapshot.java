package behavioral.memento.solution.snapshot;

import behavioral.memento.model.RestaurantTable;
import behavioral.memento.solution.layout.RestaurantTableLayout;
import java.util.List;
import java.util.Objects;

/**
 * BẢN LƯU (Memento): trạng thái của toàn bộ sơ đồ bàn nhà hàng tại một thời điểm.
 * Giữ dữ liệu private, không có getter để nơi quản lý lịch sử đọc các bàn đã lưu.
 * Trong demo này, restore() nhận yêu cầu từ lịch sử (Caretaker),
 * rồi chuyển dữ liệu về sơ đồ (Originator) để sơ đồ thực hiện khôi phục.
 */
public final class RestaurantTableLayoutSnapshot {
    // Sơ đồ hiện tại (Originator) sẽ nhận dữ liệu khi khôi phục bản lưu này.
    private final RestaurantTableLayout currentRestaurantTableLayout;

    // Dữ liệu TẤT CẢ CÁC BÀN của MỘT bản lưu (Memento).
    // Ví dụ bản lưu V2 giữ các bản sao bàn A, B, C với thông tin tại lúc lưu V2.
    // Mỗi phần tử là một bàn; lịch sử (Caretaker) giữ nhiều bản lưu V1, V2, V3, ...
    private final List<RestaurantTable> savedRestaurantTables;

    public RestaurantTableLayoutSnapshot(RestaurantTableLayout currentRestaurantTableLayout) {
        this.currentRestaurantTableLayout = Objects.requireNonNull(currentRestaurantTableLayout);

        // Sơ đồ (Originator) trả danh sách chỉ đọc chứa các bàn đã sao chép qua getTables().
        // Bản lưu (Memento) giữ kết quả này để dữ liệu đã lưu độc lập với sơ đồ hiện tại.
        this.savedRestaurantTables = currentRestaurantTableLayout.getTables();
    }

    public void restore() {
        // Bản lưu (Memento) nhận yêu cầu khôi phục từ lịch sử (Caretaker).
        // Chuyển dữ liệu đã lưu về sơ đồ (Originator) qua lời gọi bên dưới.
        // Sơ đồ (Originator) thực sự sao chép và thay dữ liệu hiện tại trong restoreTables().
        currentRestaurantTableLayout.restoreTables(savedRestaurantTables);
    }
}
