package behavioral.strategy.problem;

// Dịch vụ tính phí giao hàng: chọn đơn giá theo loại và áp dụng công thức chung.
public class ShippingService {
    // distance: quãng đường giao từ cửa hàng đến người nhận (km); kết quả: phí giao hàng (VND).
    public double calculateShippingFee(String shippingType, double distance) {
        if (distance < 0) {
            throw new IllegalArgumentException("Distance must not be negative.");
        }

        double baseFee; // Phí cơ bản của một đơn giao hàng, tính bằng VND.
        double feePerKm; // Phí cộng thêm cho mỗi km giao hàng, tính bằng VND/km.

        // Thêm loại giao hàng mới thì vẫn phải sửa nhánh chọn đơn giá tại đây.
        if ("STANDARD".equals(shippingType)) {
            baseFee = 20_000;
            feePerKm = 2_000;
        } else if ("EXPRESS".equals(shippingType)) {
            baseFee = 35_000;
            feePerKm = 4_000;
        } else if ("SAME_DAY".equals(shippingType)) {
            baseFee = 50_000;
            feePerKm = 6_000;
        } else {
            throw new IllegalArgumentException("Unknown shipping type: " + shippingType);
        }

        return baseFee + feePerKm * distance;
    }
}
