package creational.factorymethod.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Điểm chạy gửi thông báo: Service tự chọn và tạo kênh email, SMS hoặc push.
public class Main {
    // args là mảng tham số dòng lệnh; demo này không sử dụng.
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        String message = "Don hang da duoc xac nhan.";

        NotificationService service = new NotificationService();
        service.sendNotification("email", message);
        service.sendNotification("sms", message);
        service.sendNotification("push", message);
    }
}
