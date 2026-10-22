package behavioral.mediator.solution.component;

import behavioral.mediator.solution.mediator.FormMediator;

// Dropdown chỉ giữ quốc gia đã chọn và báo Mediator; không giữ dropdown tỉnh/thành.
public class CountryDropdown {
    // Nhận quốc gia mới để phối hợp nạp tỉnh/thành và kiểm tra Lưu.
    private final FormMediator mediator;
    // Tên quốc gia đang chọn, ví dụ Việt Nam; chuỗi rỗng nghĩa là chưa chọn.
    private String selectedCountry = "";

    public CountryDropdown(FormMediator mediator) {
        this.mediator = mediator;
    }

    public void select(String country) {
        selectedCountry = country;
        System.out.println("  CountryDropdown -> mediator.onCountryChanged(\"" + country + "\")");
        mediator.onCountryChanged(country);
    }

    public String getSelectedCountry() {
        return selectedCountry;
    }
}
