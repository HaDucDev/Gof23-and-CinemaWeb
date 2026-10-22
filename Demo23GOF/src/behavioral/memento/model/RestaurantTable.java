package behavioral.memento.model;

import java.util.Objects;

/**
 * Dữ liệu của MỘT bàn trong sơ đồ nhà hàng.
 * Mã bàn giữ nguyên; tọa độ, số ghế và loại bàn có thể được chỉnh sửa.
 * Vì object RestaurantTable có thể thay đổi, bản lưu phải chứa các RestaurantTable độc lập với bàn
 * đang chỉnh sửa.
 */
public final class RestaurantTable {
    private final String code;
    private int x;
    private int y;
    private int seats;
    private String type;

    public RestaurantTable(String code, int x, int y, int seats, String type) {
        this.code = Objects.requireNonNull(code);
        this.x = x;
        this.y = y;
        changeDetails(seats, type);
    }

    // Kéo thả bàn: thay tọa độ trên chính object này.
    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Đổi thông tin bàn; kiểm tra đủ dữ liệu trước khi cập nhật.
    public void changeDetails(int seats, String type) {
        if (seats <= 0) {
            throw new IllegalArgumentException("Số ghế phải lớn hơn 0");
        }
        Objects.requireNonNull(type);
        this.seats = seats;
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getSeats() {
        return seats;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Bàn " + code + ": (" + x + "," + y + "), " + seats + " ghế, loại: " + type;
    }

    // So sánh ĐẦY ĐỦ dữ liệu để đối chiếu kết quả khôi phục của hai bản.
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RestaurantTable)) {
            return false;
        }
        RestaurantTable restaurantTable = (RestaurantTable) other;
        return code.equals(restaurantTable.code)
                && x == restaurantTable.x
                && y == restaurantTable.y
                && seats == restaurantTable.seats
                && type.equals(restaurantTable.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, x, y, seats, type);
    }
}
