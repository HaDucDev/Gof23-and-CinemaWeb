package behavioral.mediator.solution.mediator;

// Các thông báo có tên cụ thể để nhìn vào biết component vừa thay đổi điều gì.
public interface FormMediator {
    void onChildrenChanged(boolean checked); // Checkbox báo thay đổi trạng thái Có con.

    // country là tên quốc gia vừa chọn, dùng để nạp lại danh sách tên tỉnh/thành.
    void onCountryChanged(String country);

    void onFormChanged(); // Số con hoặc tỉnh/thành đổi, cần kiểm tra lại điều kiện Lưu.
}
