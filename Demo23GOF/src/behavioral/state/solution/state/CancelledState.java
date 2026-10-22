package behavioral.state.solution.state;

// Đơn đã hủy: chỉ cung cấp tên, kế thừa tất cả thao tác từ chối từ lớp cha.
public class CancelledState extends AbstractOrderState {
    @Override
    public String getName() {
        return "CANCELLED - Đã hủy";
    }
}
