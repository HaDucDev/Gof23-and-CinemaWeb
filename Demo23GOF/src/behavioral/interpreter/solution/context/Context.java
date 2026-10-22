package behavioral.interpreter.solution.context;

import java.util.Map;
import java.util.Set;

/** Chỉ cung cấp bảng mã bất biến, không phân tích phiếu hoặc điều phối nghiệp vụ. */
public final class Context {
    // Khóa là mã đồ uống (TS, CP...); giá trị là tên đồ uống (Trà sữa, Cà phê...); Expression dùng
    // bảng này để tra tên.
    private final Map<String, String> drinks =
            Map.of(
                    "TS", "Trà sữa",
                    "TX", "Trà xanh",
                    "CP", "Cà phê",
                    "NC", "Nước cam");
    // Tập hợp các mã kích cỡ đồ uống hợp lệ.
    private final Set<String> sizes = Set.of("S", "M", "L");
    // Khóa là mã lượng đường (D0, D30...); giá trị là mô tả lượng đường (Không đường, 30%
    // đường...).
    private final Map<String, String> sugars =
            Map.of(
                    "D0", "Không đường",
                    "D30", "30% đường",
                    "D50", "50% đường",
                    "D70", "70% đường",
                    "D100", "100% đường");
    // Khóa là mã lượng đá (I0, I50, I100); giá trị là mô tả lượng đá (Không đá, Ít đá, Nhiều đá).
    private final Map<String, String> ice =
            Map.of(
                    "I0", "Không đá",
                    "I50", "Ít đá",
                    "I100", "Nhiều đá");
    // Mã nhiệt độ -> mô tả nhiệt độ đồ uống.
    private final Map<String, String> temperatures = Map.of("HOT", "Nóng");
    // Mã topping -> tên topping thêm vào đồ uống.
    private final Map<String, String> toppings = Map.of("TC", "Trân châu");
    // Khóa là mã yêu cầu ống hút; giá trị là mô tả có lấy ống hút hay không.
    private final Map<String, String> straws =
            Map.of(
                    "ST0", "Không lấy ống hút",
                    "ST1", "Có ống hút");

    public Map<String, String> getDrinks() {
        return drinks;
    }

    public Set<String> getSizes() {
        return sizes;
    }

    public Map<String, String> getSugars() {
        return sugars;
    }

    public Map<String, String> getIce() {
        return ice;
    }

    public Map<String, String> getTemperatures() {
        return temperatures;
    }

    public Map<String, String> getToppings() {
        return toppings;
    }

    public Map<String, String> getStraws() {
        return straws;
    }
}
