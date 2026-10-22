package structural.adapter.problem2.model;

// Đại diện một dòng hàng được mua: tên, số lượng, đơn vị tính và đơn giá.
public class OrderItem {
    private final String name; // Tên sản phẩm, ví dụ: "Sữa tươi".
    private final int quantity; // Số lượng khách mua, ví dụ: 2.
    private final String unit; // Đơn vị tính, ví dụ: "hộp" hoặc "ổ".
    // Giá của một đơn vị sản phẩm, tính bằng VNĐ, ví dụ: 25000.
    private final long unitPrice;

    public OrderItem(String name, int quantity, String unit, long unitPrice) {
        if (quantity <= 0 || unitPrice < 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0 và đơn giá không được âm.");
        }

        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    // Thành tiền của dòng sản phẩm = đơn giá × số lượng.
    public long getTotalPrice() {
        return unitPrice * quantity;
    }
}
