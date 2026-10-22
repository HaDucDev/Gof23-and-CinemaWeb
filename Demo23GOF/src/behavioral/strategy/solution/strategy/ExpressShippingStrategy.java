package behavioral.strategy.solution.strategy;

// Strategy giao nhanh: phí cơ bản 35.000 VND, cộng 4.000 VND cho mỗi km.
public class ExpressShippingStrategy implements ShippingStrategy {
    // distance là quãng đường giao từ cửa hàng đến người nhận, tính bằng km.
    @Override
    public double calculateFee(double distance) {
        double baseFee = 35_000;
        double feePerKm = 4_000;
        return baseFee + feePerKm * distance;
    }
}
