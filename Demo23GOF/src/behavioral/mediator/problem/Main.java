package behavioral.mediator.problem;

import behavioral.mediator.problem.component.ChildCheckboxBad;
import behavioral.mediator.problem.component.ChildCountFieldBad;
import behavioral.mediator.problem.component.CountryDropdownBad;
import behavioral.mediator.problem.component.ProvinceDropdownBad;
import behavioral.mediator.problem.component.SaveButtonBad;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Main chỉ tạo form, mô phỏng người dùng và in kết quả sau mỗi thao tác.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // Các ô nhập giữ tham chiếu trực tiếp tới ô khác hoặc nút Lưu.
        // save mô phỏng nút "Lưu": giữ trạng thái bật/tắt và lý do dựa trên dữ liệu form.
        SaveButtonBad save = new SaveButtonBad();
        // countField mô phỏng ô "Số con"; nhận save để kiểm tra lại form khi số con thay đổi.
        ChildCountFieldBad countField = new ChildCountFieldBad(save);
        // checkbox mô phỏng ô tick "Có con"; nhận countField để hiện/ẩn ô số con và save để kiểm
        // tra lại form.
        ChildCheckboxBad checkbox = new ChildCheckboxBad(countField, save);
        // province mô phỏng danh sách chọn "Tỉnh/Thành"; nhận save để kiểm tra lại form khi chọn
        // địa chỉ.
        ProvinceDropdownBad province = new ProvinceDropdownBad(save);
        // country mô phỏng danh sách chọn "Quốc gia"; nhận province để nạp lại tỉnh/thành và save
        // để kiểm tra lại form.
        CountryDropdownBad country = new CountryDropdownBad(province, save);
        // Cho nút Lưu giữ bốn ô nhập để tự đọc dữ liệu và quyết định có được bật hay không.
        save.bindForm(checkbox, countField, country, province);

        System.out.println("===== PROBLEM: CÁC Ô NHẬP TỰ PHỐI HỢP =====");
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
            ChildCheckboxBad checkbox,
            ChildCountFieldBad countField,
            CountryDropdownBad country,
            ProvinceDropdownBad province,
            SaveButtonBad save) {
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
