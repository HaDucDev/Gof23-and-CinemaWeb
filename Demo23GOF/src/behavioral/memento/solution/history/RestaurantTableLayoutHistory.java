package behavioral.memento.solution.history;

import behavioral.memento.solution.layout.RestaurantTableLayout;
import behavioral.memento.solution.snapshot.RestaurantTableLayoutSnapshot;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * LỊCH SỬ (Caretaker): quản lý NHIỀU bản lưu (Memento) theo số phiên bản.
 * Khi lưu: yêu cầu sơ đồ (Originator) tạo bản lưu, rồi cất bản lưu đó.
 * Khi khôi phục: chọn bản lưu (Memento) và gọi restore() trên bản lưu.
 * Bản lưu chuyển dữ liệu về sơ đồ (Originator) để sơ đồ tự sao chép và cập nhật trạng thái.
 * So sánh save()/restore() ở đây với hai phương thức trong class cùng tên của bản Problem.
 */
public final class RestaurantTableLayoutHistory {
    // Sơ đồ hiện tại (Originator): nơi tạo bản lưu và thực sự áp dụng dữ liệu khôi phục.
    private final RestaurantTableLayout currentRestaurantTableLayout;

    // Khóa: số phiên bản, ví dụ 1, 2, 3, 4.
    // Giá trị: MỘT bản lưu (Memento), tức một đối tượng Snapshot do sơ đồ tạo ra.
    // Map giữ được NHIỀU Memento cùng lúc; không phải chỉ có một biến backup.
    private final Map<Integer, RestaurantTableLayoutSnapshot> savedVersions =
            new LinkedHashMap<Integer, RestaurantTableLayoutSnapshot>();

    private int nextVersionNumber = 1;

    public RestaurantTableLayoutHistory(RestaurantTableLayout currentRestaurantTableLayout) {
        this.currentRestaurantTableLayout = currentRestaurantTableLayout;
    }

    public int save() {
        // BƯỚC 1: lịch sử (Caretaker) yêu cầu sơ đồ (Originator) tạo bản lưu (Memento).
        RestaurantTableLayoutSnapshot restaurantTableLayoutSnapshot =
                currentRestaurantTableLayout.createSnapshot();

        // BƯỚC 2: lịch sử (Caretaker) cất bản lưu (Memento) theo số phiên bản.
        int versionNumber = nextVersionNumber;
        savedVersions.put(versionNumber, restaurantTableLayoutSnapshot);
        nextVersionNumber++;

        // Lịch sử (Caretaker) giữ/chọn bản lưu, không tự đọc thuộc tính hay sao chép bàn.
        System.out.println("[HISTORY] Đã nhận Memento từ sơ đồ và lưu V" + versionNumber);
        return versionNumber;
    }

    public boolean restore(int versionNumber) {
        // BƯỚC 1: lịch sử (Caretaker) chọn bản lưu (Memento) theo số phiên bản.
        RestaurantTableLayoutSnapshot selectedRestaurantTableLayoutSnapshot =
                savedVersions.get(versionNumber);
        if (selectedRestaurantTableLayoutSnapshot == null) {
            System.out.println("[HISTORY] Không có V" + versionNumber + "; giữ nguyên sơ đồ");
            return false;
        }

        // BƯỚC 2: lịch sử (Caretaker) gọi restore() trên bản lưu (Memento).
        // Bản lưu (Memento) chuyển dữ liệu về sơ đồ (Originator) qua restoreTables().
        // Sơ đồ (Originator) dùng copyRestaurantTables() rồi thay danh sách bàn hiện tại.
        selectedRestaurantTableLayoutSnapshot.restore();

        System.out.println("[HISTORY] Đã yêu cầu sơ đồ tự khôi phục V" + versionNumber);
        return true;
    }

    public List<Integer> getVersions() {
        return new ArrayList<Integer>(savedVersions.keySet());
    }

    public void showHistory() {
        System.out.println("\n[CÁC PHIÊN BẢN ĐANG ĐƯỢC GIỮ]");
        if (savedVersions.isEmpty()) {
            System.out.println("(Chưa có bản lưu)");
        }
        for (Integer versionNumber : savedVersions.keySet()) {
            System.out.println("V" + versionNumber);
        }
    }
}
