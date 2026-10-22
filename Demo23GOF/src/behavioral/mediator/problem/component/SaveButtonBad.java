package behavioral.mediator.problem.component;

// Nút Lưu phải giữ cả bốn ô nhập để tự đọc và kiểm tra toàn bộ dữ liệu.
public class SaveButtonBad {
    // Đọc người dùng có khai báo Có con hay không.
    private ChildCheckboxBad childCheckbox;
    // Đọc số con để tự kiểm tra điều kiện > 0.
    private ChildCountFieldBad childCountField;
    // Đọc quốc gia hiện tại để kiểm tra địa chỉ.
    private CountryDropdownBad countryDropdown;
    // Đọc tỉnh/thành hiện tại để kiểm tra địa chỉ.
    private ProvinceDropdownBad provinceDropdown;
    private boolean enabled; // Nút Lưu có được bật hay không.
    // Giải thích trạng thái bật/tắt khi in form.
    private String reason = "Chưa chọn quốc gia.";

    // Main nối các object sau khi tạo; validate() chỉ được gọi sau khi đã bind đủ form.
    public void bindForm(
            ChildCheckboxBad childCheckbox,
            ChildCountFieldBad childCountField,
            CountryDropdownBad countryDropdown,
            ProvinceDropdownBad provinceDropdown) {
        this.childCheckbox = childCheckbox;
        this.childCountField = childCountField;
        this.countryDropdown = countryDropdown;
        this.provinceDropdown = provinceDropdown;
    }

    // Bản Problem đặt cả quy tắc toàn form trong nút Lưu, khiến nút phụ thuộc các ô nhập.
    public void validate() {
        if (countryDropdown.getSelectedCountry().isEmpty()) {
            disable("Chưa chọn quốc gia.");
            return;
        }
        if (provinceDropdown.getSelectedProvince().isEmpty()) {
            disable("Chưa chọn tỉnh/thành.");
            return;
        }
        if (childCheckbox.isChecked() && childCountField.getValue() <= 0) {
            disable("Đã tick Có con nhưng số con phải lớn hơn 0.");
            return;
        }
        enable();
    }

    private void enable() {
        enabled = true;
        reason = "Đã đủ thông tin.";
    }

    private void disable(String reason) {
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
