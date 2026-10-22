package behavioral.interpreter.model;

import java.util.ArrayList;
import java.util.List;

/** Chỉ chứa kết quả; giá trị null nghĩa là khách chưa chỉ định tùy chọn đó. */
public final class OrderResult {
    private String drinkName; // Tên món sau khi dịch mã, ví dụ TS -> Trà sữa.
    private String size; // Kích cỡ S, M hoặc L.
    private String sugar; // Lượng đường đã diễn giải, ví dụ D30 -> 30% đường.
    private String ice; // Lượng đá đã diễn giải, ví dụ I0 -> Không đá.
    private String temperature; // Tùy chọn nhiệt độ, ví dụ HOT -> Nóng.
    private String topping; // Topping đã diễn giải, ví dụ TC -> Trân châu.
    private String straw; // Yêu cầu ống hút, ví dụ ST0 -> Không lấy ống hút.

    public String getDrinkName() {
        return drinkName;
    }

    public void setDrinkName(String value) {
        drinkName = value;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String value) {
        size = value;
    }

    public String getSugar() {
        return sugar;
    }

    public void setSugar(String value) {
        sugar = value;
    }

    public String getIce() {
        return ice;
    }

    public void setIce(String value) {
        ice = value;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String value) {
        temperature = value;
    }

    public String getTopping() {
        return topping;
    }

    public void setTopping(String value) {
        topping = value;
    }

    public String getStraw() {
        return straw;
    }

    public void setStraw(String value) {
        straw = value;
    }

    @Override
    public String toString() {
        // Các chuỗi tên đồ uống, kích cỡ và mô tả tùy chọn để ghép thành nội dung hiển thị.
        List<String> parts = new ArrayList<>();
        if (drinkName != null) {
            parts.add(drinkName);
        }
        if (size != null) {
            parts.add("Size " + size);
        }
        if (sugar != null) {
            parts.add(sugar);
        }
        if (ice != null) {
            parts.add(ice);
        }
        if (temperature != null) {
            parts.add(temperature);
        }
        if (topping != null) {
            parts.add(topping);
        }
        if (straw != null) {
            parts.add(straw);
        }
        return String.join(" | ", parts);
    }
}
