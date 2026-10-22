package behavioral.memento;

import behavioral.memento.model.RestaurantTable;
import java.util.Arrays;
import java.util.List;

/** Tạo sơ đồ A/B/C và bàn D mẫu để hai bản demo thực hiện cùng một chuỗi chỉnh sửa. */
public final class RestaurantTableLayoutDemoData {
    private RestaurantTableLayoutDemoData() {
    }

    public static List<RestaurantTable> initialRestaurantTables() {
        return Arrays.asList(
                new RestaurantTable("A", 1, 1, 4, "Vuông"),
                new RestaurantTable("B", 3, 1, 2, "Tròn"),
                new RestaurantTable("C", 2, 3, 6, "Chữ nhật"));
    }

    public static RestaurantTable restaurantTableD() {
        return new RestaurantTable("D", 4, 3, 4, "Vuông");
    }
}
