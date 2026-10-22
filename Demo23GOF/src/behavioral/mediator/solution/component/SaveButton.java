package behavioral.mediator.solution.component;

// Nút Lưu chỉ nhận kết quả kiểm tra; không giữ ô nhập và không phát sự kiện.
public class SaveButton {
    // Kết quả bật/tắt do Mediator quyết định sau khi kiểm tra form.
    private boolean enabled;
    // Lý do hiển thị cùng trạng thái nút Lưu.
    private String reason = "Chưa chọn quốc gia.";

    public void enable() {
        enabled = true;
        reason = "Đã đủ thông tin.";
    }

    public void disable(String reason) {
        enabled = false;
        this.reason = reason;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getReason() {
        return reason;
    }
}
