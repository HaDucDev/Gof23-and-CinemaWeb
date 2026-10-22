package structural.flyweight.solution.flyweight;

// Đại diện bộ hiển thị của Marker: biểu tượng, màu và loại địa điểm.
// Flyweight bất biến: nhiều Marker có cùng bộ giá trị dùng chung một object Style.
public final class MarkerStyle {
    // Tổng số object MarkerStyle đã tạo trong lần chạy; static dùng chung cho class.
    private static int createdCount;

    // Immutable: chỉ đọc, không sửa Style dùng chung của các Marker khác.
    // Tên file biểu tượng của Marker, ví dụ restaurant.png; chưa phải dữ liệu ảnh.
    private final String icon;
    private final String color; // Màu hiển thị của Marker, ví dụ RED hoặc BLUE.
    // Nhóm địa điểm, ví dụ RESTAURANT, HOTEL hoặc GAS_STATION.
    private final String locationType;

    public MarkerStyle(String icon, String color, String locationType) {
        this.icon = icon;
        this.color = color;
        this.locationType = locationType;
        createdCount++;
    }

    public String getIcon() {
        return icon;
    }

    public String getColor() {
        return color;
    }

    public String getLocationType() {
        return locationType;
    }

    public static int getCreatedCount() {
        return createdCount;
    }
}
