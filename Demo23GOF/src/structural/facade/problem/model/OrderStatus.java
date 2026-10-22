package structural.facade.problem.model;

// Các trạng thái của đơn đặt món trong luồng tạo, xác nhận, thanh toán và giao món.
public enum OrderStatus {
    CREATED("Đã tạo"),
    CONFIRMED("Đã xác nhận"),
    REJECTED("Bị từ chối"),
    PAID("Đã thanh toán"),
    PAYMENT_FAILED("Thanh toán thất bại"),
    DELIVERY_SCHEDULED("Đã sắp xếp giao món");

    private final String description; // Nội dung tiếng Việt để hiển thị trạng thái đơn.

    OrderStatus(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
