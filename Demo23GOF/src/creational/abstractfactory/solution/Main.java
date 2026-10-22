package creational.abstractfactory.solution;

import creational.abstractfactory.solution.app.Application;
import creational.abstractfactory.solution.factory.DarkUIFactory;
import creational.abstractfactory.solution.factory.LightUIFactory;
import creational.abstractfactory.solution.factory.UIFactory;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Điểm chạy Abstract Factory: chọn một Factory để tạo cả bộ giao diện cùng theme.
public class Main {
    // args là tham số dòng lệnh; demo này không sử dụng.
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // Đổi thành "DARK" để chạy bộ component còn lại.
        String theme = "LIGHT";

        // Chọn theme một lần qua factory.
        UIFactory factory;
        if (theme.equals("LIGHT")) {
            factory = new LightUIFactory();
        } else {
            factory = new DarkUIFactory();
        }

        // Application không cần biết class component cụ thể.
        Application application = new Application(factory);
        application.render();
    }
}
