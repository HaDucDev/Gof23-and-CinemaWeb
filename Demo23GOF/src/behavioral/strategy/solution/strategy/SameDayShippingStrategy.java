package behavioral.strategy.solution.strategy;

// Strategy giao trong ngày: phí cơ bản 50.000 VND, cộng 6.000 VND cho mỗi km.
public class SameDayShippingStrategy implements ShippingStrategy {
    // distance là quãng đường giao từ cửa hàng đến người nhận, tính bằng km.
    @Override
    public double calculateFee(double distance) {
        double baseFee = 50_000;
        double feePerKm = 6_000;
        return baseFee + feePerKm * distance;
    }
}
