package structural.flyweight.solution.factory;

import java.util.HashMap;
import java.util.Map;
import structural.flyweight.solution.flyweight.MarkerStyle;

// Bộ quản lý Style dùng chung: tìm trong cache, tạo mới hoặc trả lại object đã có.
// Flyweight Factory chỉ chia sẻ khi icon, color và locationType cùng giá trị.
public class MarkerStyleFactory {
    // Factory mới có Map rỗng: new HashMap chỉ tạo cache, chưa tạo MarkerStyle.
    // Khóa là chuỗi ghép tên file biểu tượng, màu và loại địa điểm; giá trị là object MarkerStyle
    // dùng chung cho bộ đó.
    private final Map<String, MarkerStyle> styles = new HashMap<>();

    // Yêu cầu lấy Style: có trong cache thì dùng lại, chưa có thì tạo rồi lưu vào cache.
    public MarkerStyle getMarkerStyle(String icon, String color, String locationType) {
        // Cùng cả ba thuộc tính mới dùng chung Style; không chỉ so locationType.
        // Ví dụ khóa: restaurant.png|RED|RESTAURANT.
        String key = icon + "|" + color + "|" + locationType;
        // Chỉ tìm tham chiếu đã lưu; get() của Map không tạo object MarkerStyle.
        MarkerStyle markerStyle = styles.get(key);

        if (markerStyle != null) {
            // Đã có: trả đúng tham chiếu cũ, không tạo thêm object Style.
            System.out.println("Reusing MarkerStyle: " + locationType);
            return markerStyle;
        }

        // Chưa có bộ giá trị này: chính dòng new MarkerStyle bên dưới mới tạo object Style.
        // Lần đầu yêu cầu mỗi bộ icon/color/locationType chạy nhánh này; lần sau dùng nhánh trên.
        markerStyle = new MarkerStyle(icon, color, locationType);
        styles.put(key, markerStyle);
        System.out.println("Creating new MarkerStyle: " + locationType);
        return markerStyle;
    }
}
