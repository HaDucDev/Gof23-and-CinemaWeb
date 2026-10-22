package behavioral.mediator.solution.component;

import behavioral.mediator.solution.mediator.FormMediator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Dropdown giữ danh sách và lựa chọn tỉnh/thành; chọn xong chỉ báo Mediator.
public class ProvinceDropdown {
    // Được báo sau khi chọn tỉnh/thành để kiểm tra lại form.
    private final FormMediator mediator;
    // Danh sách tên tỉnh/thành có thể chọn, thuộc quốc gia hiện tại.
    private List<String> options = Collections.emptyList();
    // Tên tỉnh/thành đã chọn; xóa về chuỗi rỗng khi danh sách tên tỉnh/thành được nạp lại.
    private String selectedProvince = "";

    public ProvinceDropdown(FormMediator mediator) {
        this.mediator = mediator;
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
        System.out.println("  ProvinceDropdown -> mediator.onFormChanged()");
        mediator.onFormChanged();
    }

    public String getSelectedProvince() {
        return selectedProvince;
    }

    public List<String> getOptions() {
        return Collections.unmodifiableList(options);
    }
}
