package behavioral.state.solution.state;

// Đơn đã hoàn tất: chỉ cung cấp tên, kế thừa tất cả thao tác từ chối từ lớp cha.
public class CompletedState extends AbstractOrderState {
    @Override
    public String getName() {
        return "COMPLETED - Đã hoàn tất";
    }
}
