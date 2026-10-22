package structural.composite.problem;

import java.util.ArrayList;
import java.util.List;

// Đại diện hộp quà có thể chứa sản phẩm và hộp con.
// Giữ List<Object>; Client tự duyệt và phân biệt sản phẩm với hộp con.
public class Box {
    private final String name; // Tên hộp quà hoặc hộp con.

    // Cố tình dùng Object: Product và Box chưa có giao diện tính giá chung.
    // Danh sách object sản phẩm (Product) hoặc hộp con (Box); chưa có giao diện tính giá chung.
    private final List<Object> items = new ArrayList<>();

    public Box(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(Object item) {
        items.add(item);
    }

    public List<Object> getItems() {
        return items;
    }
}
