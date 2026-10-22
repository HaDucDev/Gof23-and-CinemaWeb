package behavioral.strategy.solution.strategy;

// Interface khai báo calculateFee() để các cách tính phí giao hàng có cùng cách gọi; object được
// tạo từ class cụ thể.
public interface ShippingStrategy {
    // distance: quãng đường giao từ cửa hàng đến người nhận (km); kết quả: phí giao hàng (VND).
    double calculateFee(double distance);
}
