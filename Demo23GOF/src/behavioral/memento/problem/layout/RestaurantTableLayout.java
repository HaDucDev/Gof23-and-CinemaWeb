package behavioral.memento.problem.layout;

import behavioral.memento.model.RestaurantTable;
import java.util.ArrayList;
import java.util.List;

/**
 * MỘT SƠ ĐỒ BỐ TRÍ BÀN NHÀ HÀNG: chứa danh sách nhiều bàn (A, B, C, ...).
 * Các thao tác thêm, xóa và chỉnh sửa áp dụng cho từng bàn trong sơ đồ.
 * Chưa biết tạo bản lưu hoặc khôi phục; những việc đó do RestaurantTableLayoutHistory
 * thực hiện bên ngoài.
 * Ở Solution, sơ đồ (Originator) có thêm cách tạo bản lưu và tự áp dụng dữ liệu khôi phục.
 */
public final class RestaurantTableLayout {
    // Danh sách các bàn ĐANG ĐƯỢC CHỈNH SỬA, không phải danh sách phiên bản.
    private final List<RestaurantTable> currentRestaurantTables = new ArrayList<RestaurantTable>();

    /**
     * Trả chính danh sách nội bộ để History đọc dữ liệu và ghi dữ liệu khôi phục trở lại.
     * Đây là chỗ hai class bị phụ thuộc: History có thể can thiệp trực tiếp vào sơ đồ.
     */
    public List<RestaurantTable> getTables() {
        return currentRestaurantTables;
    }

    /**
     * Thêm MỘT bàn vào sơ đồ hiện tại. Thao tác này chưa lưu phiên bản vào lịch sử.
     */
    public void addTable(RestaurantTable restaurantTable) {
        if (hasTable(restaurantTable.getCode())) {
            throw new IllegalArgumentException("Đã có bàn " + restaurantTable.getCode());
        }
        currentRestaurantTables.add(restaurantTable);
    }

    public void removeTable(String code) {
        int tableIndex = indexOf(code);
        currentRestaurantTables.remove(tableIndex);
    }

    public void moveTable(String code, int x, int y) {
        int tableIndex = indexOf(code);
        RestaurantTable restaurantTableBeingEdited = currentRestaurantTables.get(tableIndex);
        restaurantTableBeingEdited.moveTo(x, y);
    }

    public void changeTableDetails(String code, int seats, String type) {
        int tableIndex = indexOf(code);
        RestaurantTable restaurantTableBeingEdited = currentRestaurantTables.get(tableIndex);
        restaurantTableBeingEdited.changeDetails(seats, type);
    }

    public boolean hasTable(String code) {
        for (RestaurantTable restaurantTable : currentRestaurantTables) {
            if (restaurantTable.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }

    private int indexOf(String code) {
        for (int index = 0; index < currentRestaurantTables.size(); index++) {
            RestaurantTable restaurantTable = currentRestaurantTables.get(index);
            if (restaurantTable.getCode().equals(code)) {
                return index;
            }
        }
        throw new IllegalArgumentException("Không tìm thấy bàn " + code);
    }

    public void show() {
        if (currentRestaurantTables.isEmpty()) {
            System.out.println("(Sơ đồ rỗng)");
        }
        for (RestaurantTable restaurantTable : currentRestaurantTables) {
            System.out.println(restaurantTable);
        }
    }
}
