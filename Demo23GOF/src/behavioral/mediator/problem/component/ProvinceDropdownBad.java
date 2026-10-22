package behavioral.mediator.problem.component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Dropdown giữ danh sách, tỉnh/thành đã chọn và gọi thẳng nút Lưu khi chọn.
public class ProvinceDropdownBad {
    // Nút Lưu được gọi trực tiếp sau khi chọn tỉnh/thành.
    private final SaveButtonBad saveButton;
    // Danh sách tên tỉnh/thành có thể chọn, thuộc quốc gia hiện tại.
    private List<String> options = Collections.emptyList();
    // Tên tỉnh/thành đã chọn; xóa về chuỗi rỗng khi đổi quốc gia.
    private String selectedProvince = "";

    public ProvinceDropdownBad(SaveButtonBad saveButton) {
        this.saveButton = saveButton;
    }

    public void reload(String country) {
        if ("Việt Nam".equals(country)) {
            options = Arrays.asList("Hà Nội", "Đà Nẵng");
        } else if ("Nhật Bản".equals(country)) {
            options = Arrays.asList("Tokyo", "Osaka");
        } else {
            options = Collections.emptyList();
        }
        selectedProvince = ""; // Xóa lựa chọn cũ sau khi đổi quốc gia.
        System.out.println("  Danh sách tên tỉnh/thành có thể chọn: " + options);
    }

    public void select(String province) {
        if (!options.contains(province)) {
            throw new IllegalArgumentException("Tỉnh/Thành không có trong danh sách: " + province);
        }
        selectedProvince = province;
        System.out.println("  ProvinceDropdown -> gọi trực tiếp nút Lưu.");
        saveButton.validate();
    }

    public String getSelectedProvince() {
        return selectedProvince;
    }

    public List<String> getOptions() {
        return Collections.unmodifiableList(options);
    }
}
