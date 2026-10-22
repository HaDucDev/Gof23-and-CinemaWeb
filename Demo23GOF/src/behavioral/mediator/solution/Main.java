package behavioral.mediator.solution;

import behavioral.mediator.solution.component.ChildCheckbox;
import behavioral.mediator.solution.component.ChildCountField;
import behavioral.mediator.solution.component.CountryDropdown;
import behavioral.mediator.solution.component.ProvinceDropdown;
import behavioral.mediator.solution.component.SaveButton;
import behavioral.mediator.solution.mediator.RegistrationFormMediator;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Cùng thao tác và cách in trạng thái như Problem; khác ở nơi chứa logic phối hợp.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // Mediator tạo năm component; các getter dưới đây trả lại object đã được tạo.
        // mediator là bộ điều phối form: xử lý quan hệ giữa Có con, Số con, địa chỉ và nút Lưu.
        RegistrationFormMediator mediator = new RegistrationFormMediator();
        // checkbox mô phỏng ô tick "Có con"; báo Mediator khi người dùng tick hoặc bỏ tick.
        ChildCheckbox checkbox = mediator.getChildCheckbox();
        // countField mô phỏng ô "Số con": giữ số đã nhập và trạng thái hiện/ẩn.
        ChildCountField countField = mediator.getChildCountField();
        // country mô phỏng danh sách chọn "Quốc gia"; báo Mediator khi lựa chọn thay đổi.
        CountryDropdown country = mediator.getCountryDropdown();
        // province mô phỏng danh sách chọn "Tỉnh/Thành", được nạp theo quốc gia đã chọn.
        ProvinceDropdown province = mediator.getProvinceDropdown();
        // save mô phỏng nút "Lưu": giữ trạng thái bật/tắt và lý do do Mediator quyết định.
        SaveButton save = mediator.getSaveButton();

        System.out.println("===== SOLUTION: MEDIATOR ĐỨNG RA PHỐI HỢP =====");
        System.out.println("Quy tắc: chọn đủ địa chỉ; nếu có con thì nhập số con lớn hơn 0.");
        System.out.println();
        System.out.println("TRẠNG THÁI BAN ĐẦU");
        showForm(checkbox, countField, country, province, save);

        // Tình huống 1: chưa khai báo có con, chỉ cần đủ thông tin địa chỉ.
        System.out.println("\n1. CHỌN ĐỊA CHỈ");
        System.out.println("Thao tác: chọn quốc gia Việt Nam.");
        country.select("Việt Nam");
        showForm(checkbox, countField, country, province, save);

        System.out.println("\nThao tác: chọn tỉnh/thành Hà Nội.");
        province.select("Hà Nội");
        showForm(checkbox, countField, country, province, save);

        // Tình huống 2: cùng form đang hợp lệ, tick Có con sẽ yêu cầu thêm số con.
        System.out.println("\n2. KHAI BÁO CÓ CON");
        System.out.println("Thao tác: tick Có con.");
        checkbox.setChecked(true);
        showForm(checkbox, countField, country, province, save);

        System.out.println("\nThao tác: nhập số con bằng 2.");
        countField.setValue(2);
        showForm(checkbox, countField, country, province, save);

        System.out.println("\nThao tác: bỏ tick Có con.");
        checkbox.setChecked(false);
        showForm(checkbox, countField, country, province, save);

        // Tình huống 3: đổi quốc gia làm lựa chọn tỉnh/thành trước đó không còn hợp lệ.
        System.out.println("\n3. ĐỔI QUỐC GIA");
        System.out.println("Thao tác: đổi quốc gia sang Nhật Bản.");
        country.select("Nhật Bản");
        showForm(checkbox, countField, country, province, save);

        System.out.println("\nThao tác: chọn tỉnh/thành Tokyo.");
        province.select("Tokyo");
        showForm(checkbox, countField, country, province, save);
    }

    // Chỉ in dữ liệu đang có để người xem thấy kết quả; hàm này không điều phối form.
    private static void showForm(
            ChildCheckbox checkbox,
            ChildCountField countField,
            CountryDropdown country,
            ProvinceDropdown province,
            SaveButton save) {
        String countryName =
                country.getSelectedCountry().isEmpty()
                        ? "(chưa chọn)"
                        : country.getSelectedCountry();
        String provinceName =
                province.getSelectedProvince().isEmpty()
                        ? "(chưa chọn)"
                        : province.getSelectedProvince();

        System.out.println("  Địa chỉ: " + countryName + " / " + provinceName);
        System.out.println(
                "  Có con: "
                        + (checkbox.isChecked() ? "CÓ" : "KHÔNG")
                        + " | Ô số con: "
                        + (countField.isVisible() ? "HIỆN" : "ẨN")
                        + " | Số con đã nhập: "
                        + countField.getValue());
        System.out.println(
                "  Nút Lưu: " + (save.isEnabled() ? "BẬT" : "TẮT") + " - " + save.getReason());
    }
}
