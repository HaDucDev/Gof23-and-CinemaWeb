package behavioral.strategy.solution.strategy;

// Strategy giao thường: phí cơ bản 20.000 VND, cộng 2.000 VND cho mỗi km.
public class StandardShippingStrategy implements ShippingStrategy {
    // distance là quãng đường giao từ cửa hàng đến người nhận, tính bằng km.
    @Override
    public double calculateFee(double distance) {
        double baseFee = 20_000;
        double feePerKm = 2_000;
        return baseFee + feePerKm * distance;
    }
}
