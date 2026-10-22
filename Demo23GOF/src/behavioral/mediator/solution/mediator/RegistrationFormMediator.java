package behavioral.mediator.solution.mediator;

import behavioral.mediator.solution.component.ChildCheckbox;
import behavioral.mediator.solution.component.ChildCountField;
import behavioral.mediator.solution.component.CountryDropdown;
import behavioral.mediator.solution.component.ProvinceDropdown;
import behavioral.mediator.solution.component.SaveButton;

// Mediator giữ quy tắc phối hợp: hiện/ẩn ô, nạp lại tỉnh/thành, kiểm tra Lưu.
public class RegistrationFormMediator implements FormMediator {
    // Tham chiếu object ô tick Có con, dùng để đọc trạng thái tick khi kiểm tra form.
    private final ChildCheckbox childCheckbox;
    // Tham chiếu object ô nhập Số con, được Mediator hiện/ẩn và đọc số đã nhập.
    private final ChildCountField childCountField;
    // Tham chiếu object ô chọn Quốc gia; đọc tên quốc gia để nạp danh sách tên tỉnh/thành.
    private final CountryDropdown countryDropdown;
    // Tham chiếu object ô chọn Tỉnh/Thành; nạp lại các tên tỉnh/thành khi đổi quốc gia.
    private final ProvinceDropdown provinceDropdown;
    // Tham chiếu object nút Lưu; nhận trạng thái bật/tắt và lý do sau khi kiểm tra form.
    private final SaveButton saveButton;

    public RegistrationFormMediator() {
        // this là cùng Mediator được bốn ô nhập giữ để báo thay đổi.
        childCheckbox = new ChildCheckbox(this);
        childCountField = new ChildCountField(this);
        countryDropdown = new CountryDropdown(this);
        provinceDropdown = new ProvinceDropdown(this);
        saveButton = new SaveButton();
    }

    // Checkbox gọi hàm này; Mediator quyết định phản ứng của ô số con và nút Lưu.
    public void onChildrenChanged(boolean checked) {
        System.out.println(
                "  Mediator -> " + (checked ? "hiện" : "ẩn") + " ô số con; kiểm tra Lưu.");
        if (checked) {
            childCountField.show();
        } else {
            childCountField.hide();
        }
        validateSaveButton();
    }

    // Dropdown quốc gia gọi hàm này; Mediator quyết định nạp lại tỉnh/thành.
    public void onCountryChanged(String country) {
        System.out.println("  Mediator -> nạp lại tỉnh/thành; kiểm tra Lưu.");
        provinceDropdown.reload(country);
        validateSaveButton();
    }

    // Nhập số con hoặc chọn tỉnh/thành đều cần kiểm tra lại form.
    public void onFormChanged() {
        System.out.println("  Mediator -> kiểm tra Lưu.");
        validateSaveButton();
    }

    // Quy tắc chung tập trung ở Mediator: đủ địa chỉ, và số con > 0 nếu tick Có con.
    private void validateSaveButton() {
        if (countryDropdown.getSelectedCountry().isEmpty()) {
            saveButton.disable("Chưa chọn quốc gia.");
            return;
        }
        if (provinceDropdown.getSelectedProvince().isEmpty()) {
            saveButton.disable("Chưa chọn tỉnh/thành.");
            return;
        }
        if (childCheckbox.isChecked() && childCountField.getValue() <= 0) {
            saveButton.disable("Đã tick Có con nhưng số con phải lớn hơn 0.");
            return;
        }
        saveButton.enable();
    }

    public ChildCheckbox getChildCheckbox() {
        return childCheckbox;
    }

    public ChildCountField getChildCountField() {
        return childCountField;
    }

    public CountryDropdown getCountryDropdown() {
        return countryDropdown;
    }

    public ProvinceDropdown getProvinceDropdown() {
        return provinceDropdown;
    }

    public SaveButton getSaveButton() {
        return saveButton;
    }
}
