package behavioral.state.solution.state;

// Interface khai báo các thao tác trên đơn hàng; mỗi class trạng thái cài đặt cách xử lý tương ứng.
public interface OrderState {
    void pay();

    void ship();

    void complete();

    void cancel();

    // Trả tên trạng thái đơn hàng để hiển thị, ví dụ NEW - Mới tạo; phương thức này không đổi trạng
    // thái.
    String getName();
}
