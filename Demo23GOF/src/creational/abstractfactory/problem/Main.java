package creational.abstractfactory.problem;

import creational.abstractfactory.problem.product.Button;
import creational.abstractfactory.problem.product.Checkbox;
import creational.abstractfactory.problem.product.Dialog;
import creational.abstractfactory.problem.product.dark.DarkButton;
import creational.abstractfactory.problem.product.dark.DarkCheckbox;
import creational.abstractfactory.problem.product.dark.DarkDialog;
import creational.abstractfactory.problem.product.light.LightButton;
import creational.abstractfactory.problem.product.light.LightCheckbox;
import creational.abstractfactory.problem.product.light.LightDialog;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Điểm chạy giao diện: tự chọn Light/Dark cho từng nút bấm, ô chọn và hộp thoại.
public class Main {
    // args là tham số dòng lệnh; demo này không sử dụng.
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // Đổi thành "DARK" để chạy bộ component còn lại.
        String theme = "LIGHT";

        // Mỗi component lặp lại cùng một quyết định chọn theme.
        Button button;
        if (theme.equals("LIGHT")) {
            button = new LightButton();
        } else {
            button = new DarkButton();
        }

        Checkbox checkbox;
        if (theme.equals("LIGHT")) {
            checkbox = new LightCheckbox();
        } else {
            checkbox = new DarkCheckbox();
        }

        Dialog dialog;
        if (theme.equals("LIGHT")) {
            dialog = new LightDialog();
        } else {
            dialog = new DarkDialog();
        }

        button.render();
        checkbox.render();
        dialog.render();
    }
}
