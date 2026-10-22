package behavioral.state.solution.state;

// Lớp cha trừu tượng: mặc định từ chối các thao tác, dùng tên do State con cung cấp.
// State con chỉ ghi đè thao tác được phép; không tạo trực tiếp AbstractOrderState.
public abstract class AbstractOrderState implements OrderState {
    @Override
    public void pay() {
        System.out.println("Không thể thanh toán ở trạng thái " + getName() + ".");
    }

    @Override
    public void ship() {
        System.out.println("Không thể giao hàng ở trạng thái " + getName() + ".");
    }

    @Override
    public void complete() {
        System.out.println("Không thể hoàn tất ở trạng thái " + getName() + ".");
    }

    @Override
    public void cancel() {
        System.out.println("Không thể hủy đơn ở trạng thái " + getName() + ".");
    }
}
