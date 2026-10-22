package behavioral.memento.problem;

import behavioral.memento.problem.layout.RestaurantTableLayout;
import behavioral.memento.problem.history.RestaurantTableLayoutHistory;
import behavioral.memento.RestaurantTableLayoutDemoData;
import behavioral.memento.model.RestaurantTable;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Cùng câu chuyện với slide: lưu V1 -> V2 -> V3 -> V4, rồi chọn khôi phục V2.
 * Đọc Main để hiểu thao tác người dùng; mở
 * RestaurantTableLayoutHistory.save()/restore() để xem cách thực hiện.
 */
public final class Main {
    public static void main(String[] args) throws Exception {
        // Thiết lập UTF-8 để terminal hiển thị đúng tiếng Việt; sau đó chạy kịch bản.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        run();
    }

    public static RestaurantTableLayout run() {
        System.out.println("===== PROBLEM: HISTORY TỰ ĐỌC/GHI DỮ LIỆU =====");

        // currentRestaurantTableLayout là MỘT sơ đồ bàn nhà hàng, chứa danh sách nhiều bàn.
        // Lịch sử ở Problem vừa giữ/chọn phiên bản vừa tự sao chép và thay dữ liệu sơ đồ.
        RestaurantTableLayout currentRestaurantTableLayout =
                new RestaurantTableLayout();
        RestaurantTableLayoutHistory restaurantTableLayoutHistory =
                new RestaurantTableLayoutHistory(currentRestaurantTableLayout);

        // V1: tạo ba bàn A, B, C rồi lưu TOÀN BỘ sơ đồ.
        // initialRestaurantTables là danh sách các bàn mẫu, không phải danh sách các bản lưu.
        System.out.println("\n[V1] Sơ đồ ban đầu có A, B, C");
        List<RestaurantTable> initialRestaurantTables =
                RestaurantTableLayoutDemoData.initialRestaurantTables();
        // Mỗi lượt thêm MỘT bàn; vòng lặp gọi addTable() lần lượt cho A, B, C.
        for (RestaurantTable restaurantTable : initialRestaurantTables) {
            currentRestaurantTableLayout.addTable(restaurantTable);
        }
        // Lưu toàn bộ sơ đồ hiện tại gồm A, B, C thành V1.
        restaurantTableLayoutHistory.save();
        currentRestaurantTableLayout.show();

        // V2: chỉ di chuyển A. Lần save() này vẫn lưu đủ A, B, C và mọi thuộc tính của chúng.
        System.out.println("\n[V2] Di chuyển A từ (1,1) đến (1,0), rồi lưu");
        currentRestaurantTableLayout.moveTable("A", 1, 0);
        int version2 = restaurantTableLayoutHistory.save();
        currentRestaurantTableLayout.show();

        // V3: thêm D rồi lưu thêm một phiên bản; V1 và V2 vẫn được giữ.
        System.out.println("\n[V3] Thêm D tại (4,3), rồi lưu");
        // Thêm MỘT bàn D vào sơ đồ hiện tại; sơ đồ lúc này có A, B, C, D.
        currentRestaurantTableLayout.addTable(RestaurantTableLayoutDemoData.restaurantTableD());
        // Lưu TOÀN BỘ sơ đồ A, B, C, D thành phiên bản V3.
        restaurantTableLayoutHistory.save();
        currentRestaurantTableLayout.show();

        // V4: xóa B rồi lưu. Sơ đồ hiện tại có A, C, D; bản V2 vẫn phải có A, B, C.
        System.out.println("\n[V4] Xóa B, rồi lưu");
        currentRestaurantTableLayout.removeTable("B");
        restaurantTableLayoutHistory.save();
        currentRestaurantTableLayout.show();

        // Hiển thị các khóa đang có trong Map để thấy chương trình giữ CẢ BỐN bản lưu.
        restaurantTableLayoutHistory.showHistory();

        // Người dùng chọn V2. version2 là số mà lần save() thứ hai đã trả về.
        System.out.println("\n[NGƯỜI DÙNG] Chọn khôi phục V" + version2);
        boolean restored = restaurantTableLayoutHistory.restore(version2);
        if (!restored) {
            throw new IllegalStateException("Kịch bản đã lưu V2 nhưng không tìm thấy để khôi phục");
        }

        System.out.println("\n[KẾT QUẢ SAU KHÔI PHỤC V2]");
        currentRestaurantTableLayout.show();
        System.out.println("Có lại bàn B: " + currentRestaurantTableLayout.hasTable("B"));
        System.out.println("Còn bàn D: " + currentRestaurantTableLayout.hasTable("D"));
        return currentRestaurantTableLayout;
    }
}
