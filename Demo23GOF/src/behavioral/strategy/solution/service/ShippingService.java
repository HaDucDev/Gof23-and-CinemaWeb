package behavioral.strategy.solution.service;

import behavioral.strategy.solution.strategy.ShippingStrategy;
import java.util.Objects;

// Context của Strategy: giữ cách tính đang được chọn và giao việc tính phí cho nó.
public class ShippingService {
    // Tham chiếu object chứa công thức tính phí giao hàng đang được chọn.
    private ShippingStrategy shippingStrategy;

    public ShippingService(ShippingStrategy shippingStrategy) {
        // Lưu object đã được truyền vào; constructor này chưa tính phí.
        this.shippingStrategy =
                Objects.requireNonNull(shippingStrategy, "Shipping strategy must not be null.");
    }

    // Đổi cách tính cho lần gọi tiếp theo, trên cùng một ShippingService.
    public void setShippingStrategy(ShippingStrategy shippingStrategy) {
        this.shippingStrategy =
                Objects.requireNonNull(shippingStrategy, "Shipping strategy must not be null.");
    }

    // Nhận quãng đường giao từ cửa hàng đến người nhận (km), trả phí giao hàng (VND).
    public double calculateShippingFee(double distance) {
        if (distance < 0) {
            throw new IllegalArgumentException("Distance must not be negative.");
        }

        // Gọi công thức của object hiện được giữ trong field shippingStrategy.
        return shippingStrategy.calculateFee(distance);
    }
}
