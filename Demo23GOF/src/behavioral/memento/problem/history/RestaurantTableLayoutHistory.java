package behavioral.memento.problem.history;

import behavioral.memento.problem.layout.RestaurantTableLayout;
import behavioral.memento.model.RestaurantTable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CÁCH THÔNG THƯỜNG: quản lý được NHIỀU phiên bản và khôi phục đúng.
 * Lịch sử vừa giữ/chọn phiên bản, vừa tự sao chép và thay dữ liệu của sơ đồ.
 * Ở Solution, lịch sử (Caretaker) giữ/chọn bản lưu (Memento),
 * còn sơ đồ (Originator) tự sao chép và áp dụng dữ liệu được bản lưu chuyển về.
 * Đọc save() rồi restore(); hai vòng lặp cho thấy công việc đang nằm BÊN NGOÀI sơ đồ.
 */
public final class RestaurantTableLayoutHistory {
    // Giữ tham chiếu tới chính sơ đồ đang được người dùng chỉnh sửa.
    private final RestaurantTableLayout currentRestaurantTableLayout;

    // Khóa: số phiên bản, ví dụ 1, 2, 3, 4.
    // Giá trị: danh sách CÁC BÀN ĐÃ SAO CHÉP của phiên bản đó.
    // Map này giữ nhiều bản lưu cùng lúc; mỗi lần save() thêm một phần tử mới.
    private final Map<Integer, List<RestaurantTable>> savedVersions =
            new LinkedHashMap<Integer, List<RestaurantTable>>();

    private int nextVersionNumber = 1;

    public RestaurantTableLayoutHistory(
            RestaurantTableLayout currentRestaurantTableLayout) {
        this.currentRestaurantTableLayout = currentRestaurantTableLayout;
    }

    public int save() {
        // BƯỚC 1: chuẩn bị danh sách bản sao của tất cả bàn trong một phiên bản sơ đồ.
        List<RestaurantTable> savedRestaurantTables = new ArrayList<RestaurantTable>();

        // BƯỚC 2: lịch sử tự đọc TỪNG THUỘC TÍNH của TỪNG BÀN để sao chép.
        // Ở Solution, sơ đồ (Originator) thực hiện phần sao chép này.
        // Chỉ chép danh sách sẽ vẫn dùng chung RestaurantTable có thể chỉnh sửa, làm hỏng bản lưu
        // cũ.
        for (RestaurantTable currentRestaurantTable : currentRestaurantTableLayout.getTables()) {
            String savedCode = currentRestaurantTable.getCode();
            int savedX = currentRestaurantTable.getX();
            int savedY = currentRestaurantTable.getY();
            int savedSeats = currentRestaurantTable.getSeats();
            String savedType = currentRestaurantTable.getType();

            // Đây là object bàn MỚI, giữ giá trị tại thời điểm lưu.
            RestaurantTable savedRestaurantTable =
                    new RestaurantTable(savedCode, savedX, savedY, savedSeats, savedType);
            savedRestaurantTables.add(savedRestaurantTable);
        }

        // BƯỚC 3: gắn danh sách vừa chép với một số phiên bản, rồi chuẩn bị số tiếp theo.
        int versionNumber = nextVersionNumber;
        savedVersions.put(versionNumber, savedRestaurantTables);
        nextVersionNumber++;

        System.out.println("[HISTORY] Đã tự sao chép dữ liệu và lưu V" + versionNumber);
        return versionNumber;
    }

    public boolean restore(int versionNumber) {
        // BƯỚC 1: tìm đúng danh sách bàn đã lưu của phiên bản được chọn.
        List<RestaurantTable> savedRestaurantTables = savedVersions.get(versionNumber);
        if (savedRestaurantTables == null) {
            System.out.println("[HISTORY] Không có V" + versionNumber + "; giữ nguyên sơ đồ");
            return false;
        }

        // BƯỚC 2: lịch sử lại tự đọc đủ thuộc tính để dựng các bàn hiện tại.
        // Ví dụ V2 đã lưu bàn A ở (1,0). Khi khôi phục, tạo một bàn A MỚI cũng ở (1,0).
        // Bàn A mới được đưa vào sơ đồ hiện tại; bàn A gốc vẫn nằm riêng trong bản lưu V2.
        // Sau đó kéo A hiện tại tới (9,9) thì A trong bản lưu V2 vẫn phải ở (1,0).
        // Nếu dùng chung object A của bản lưu, kéo A hiện tại sẽ sửa luôn dữ liệu đã lưu của V2.
        List<RestaurantTable> restoredRestaurantTables = new ArrayList<RestaurantTable>();
        for (RestaurantTable savedRestaurantTable : savedRestaurantTables) {
            String restoredCode = savedRestaurantTable.getCode();
            int restoredX = savedRestaurantTable.getX();
            int restoredY = savedRestaurantTable.getY();
            int restoredSeats = savedRestaurantTable.getSeats();
            String restoredType = savedRestaurantTable.getType();

            RestaurantTable restoredRestaurantTable =
                    new RestaurantTable(
                            restoredCode, restoredX, restoredY, restoredSeats, restoredType);
            restoredRestaurantTables.add(restoredRestaurantTable);
        }

        // BƯỚC 3: lịch sử trực tiếp thay toàn bộ dữ liệu bên trong sơ đồ.
        // Ở Solution, sơ đồ (Originator) tự thay dữ liệu qua restoreTables().
        // Xóa trước để D của V4 không còn; thêm lại toàn bộ để B của V2 xuất hiện trở lại.
        List<RestaurantTable> restaurantTablesBeingEdited =
                currentRestaurantTableLayout.getTables();
        restaurantTablesBeingEdited.clear();
        restaurantTablesBeingEdited.addAll(restoredRestaurantTables);

        System.out.println("[HISTORY] Đã tự ghi dữ liệu của V" + versionNumber + " vào sơ đồ");
        return true;
    }

    /*
     * Điểm cần chỉ ra khi trình bày:
     * Nếu RestaurantTable có thêm dữ liệu cần khôi phục, phải xem lại cả hai vòng lặp ở trên.
     * Hoàn toàn có thể gom hai vòng lặp vào một hàm copy dùng chung trong History.
     * Khi đó chỉ sửa cách sao chép ở một chỗ, nhưng History vẫn phải hiểu dữ liệu bàn.
     * History đang phụ thuộc vào getter, constructor và cách thay danh sách của sơ đồ.
     * Bản này vẫn chạy đúng; vấn đề là phạm vi trách nhiệm và sự phụ thuộc.
     */

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
