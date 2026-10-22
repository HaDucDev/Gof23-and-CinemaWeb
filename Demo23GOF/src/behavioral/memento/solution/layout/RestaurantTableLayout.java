package behavioral.memento.solution.layout;

import behavioral.memento.model.RestaurantTable;
import behavioral.memento.solution.snapshot.RestaurantTableLayoutSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * SƠ ĐỒ (Originator): giữ trạng thái hiện tại, gồm nhiều bàn A, B, C, ...
 * createSnapshot() tạo bản lưu (Memento) cho trạng thái của sơ đồ.
 * copyRestaurantTables() sao chép dữ liệu; restoreTables() thực sự cập nhật sơ đồ hiện tại.
 * Lịch sử (Caretaker) chọn bản lưu; bản lưu (Memento) chuyển dữ liệu về sơ đồ để khôi phục.
 */
public final class RestaurantTableLayout {
    // Các bàn đang được chỉnh sửa. Danh sách này KHÁC dữ liệu bên trong các bản lưu.
    private final List<RestaurantTable> currentRestaurantTables = new ArrayList<RestaurantTable>();

    /**
     * Sơ đồ (Originator) tạo bản lưu (Memento) cho trạng thái hiện tại.
     * Bản lưu nhớ sơ đồ này và lấy các bàn đã sao chép qua getTables() của sơ đồ.
     */
    public RestaurantTableLayoutSnapshot createSnapshot() {
        return new RestaurantTableLayoutSnapshot(this);
    }

    /**
     * Sơ đồ (Originator) nhận dữ liệu do bản lưu (Memento) chuyển về.
     * Đây là nơi thực sự sao chép và cập nhật trạng thái; bản lưu chỉ gọi vào hàm này.
     * Ví dụ V2 giữ A ở (1,0): tạo một bàn A mới ở (1,0) cho sơ đồ hiện tại.
     * Nếu kéo A hiện tại tới (9,9), A trong bản lưu V2 vẫn ở (1,0) vì là object khác.
     */
    public void restoreTables(List<RestaurantTable> savedRestaurantTables) {
        Objects.requireNonNull(savedRestaurantTables);
        // Sơ đồ (Originator) tự tạo các bàn mới từ dữ liệu bản lưu (Memento).
        List<RestaurantTable> restoredRestaurantTables =
                copyRestaurantTables(savedRestaurantTables);
        // Sơ đồ (Originator) tự thay danh sách bàn hiện tại.
        currentRestaurantTables.clear();
        currentRestaurantTables.addAll(restoredRestaurantTables);
    }

    /**
     * Sơ đồ (Originator) hiểu dữ liệu từng bàn và biết cách sao chép đầy đủ.
     * Lịch sử (Caretaker) làm việc với bản lưu, không gọi hàm sao chép này.
     * Mỗi kết quả là một danh sách mới chứa các RestaurantTable mới; không chia sẻ bàn có thể chỉnh sửa.
     * Khi dữ liệu cần lưu thay đổi, xem lại cách sao chép tại đây.
     */
    private List<RestaurantTable> copyRestaurantTables(
            List<RestaurantTable> sourceRestaurantTables) {
        List<RestaurantTable> copiedRestaurantTables = new ArrayList<RestaurantTable>();

        for (RestaurantTable sourceRestaurantTable : sourceRestaurantTables) {
            String copiedCode = sourceRestaurantTable.getCode();
            int copiedX = sourceRestaurantTable.getX();
            int copiedY = sourceRestaurantTable.getY();
            int copiedSeats = sourceRestaurantTable.getSeats();
            String copiedType = sourceRestaurantTable.getType();

            RestaurantTable copiedRestaurantTable =
                    new RestaurantTable(copiedCode, copiedX, copiedY, copiedSeats, copiedType);
            copiedRestaurantTables.add(copiedRestaurantTable);
        }

        return copiedRestaurantTables;
    }

    // Các thao tác dưới đây là nghiệp vụ chỉnh sửa sơ đồ, giống bản Problem.
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

    /**
     * Sơ đồ (Originator) cung cấp các bàn đã sao chép để tạo bản lưu hoặc hiển thị/đối chiếu.
     * Sửa các bàn trong kết quả không sửa sơ đồ hiện tại hoặc dữ liệu của bản lưu đã tạo.
     * Lịch sử (Caretaker) không dùng phương thức này khi lưu hay khôi phục.
     */
    public List<RestaurantTable> getTables() {
        List<RestaurantTable> restaurantTableCopies = copyRestaurantTables(currentRestaurantTables);
        return Collections.unmodifiableList(restaurantTableCopies);
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
