package behavioral.mediator.problem.component;

// Dropdown quốc gia tự quyết định cập nhật tỉnh/thành rồi kiểm tra nút Lưu.
public class CountryDropdownBad {
    // Dropdown bị quốc gia gọi trực tiếp để nạp lại tỉnh/thành.
    private final ProvinceDropdownBad provinceDropdown;
    // Nút Lưu phải kiểm tra lại sau khi đổi quốc gia.
    private final SaveButtonBad saveButton;
    // Tên quốc gia đang chọn, ví dụ Việt Nam; chuỗi rỗng nghĩa là chưa chọn.
    private String selectedCountry = "";

    public CountryDropdownBad(ProvinceDropdownBad provinceDropdown, SaveButtonBad saveButton) {
        this.provinceDropdown = provinceDropdown;
        this.saveButton = saveButton;
    }

    public void select(String country) {
        selectedCountry = country;
        System.out.println("  CountryDropdown -> gọi trực tiếp tỉnh/thành và nút Lưu.");
        provinceDropdown.reload(country);
        saveButton.validate();
    }

    public String getSelectedCountry() {
        return selectedCountry;
    }
}
