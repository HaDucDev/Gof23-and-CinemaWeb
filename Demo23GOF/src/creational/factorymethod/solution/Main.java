package creational.factorymethod.solution;

import creational.factorymethod.solution.creator.EmailCreator;
import creational.factorymethod.solution.creator.NotificationCreator;
import creational.factorymethod.solution.creator.PushCreator;
import creational.factorymethod.solution.creator.SmsCreator;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Điểm chạy Factory Method: chọn Creator để tạo và gửi thông báo đúng kênh.
public class Main {
    // args là mảng tham số dòng lệnh; demo này không sử dụng.
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        String message = "Don hang da duoc xac nhan.";

        // Chọn creator; creator quyết định loại Notification được tạo.
        NotificationCreator emailCreator = new EmailCreator();
        emailCreator.sendNotification(message);

        NotificationCreator smsCreator = new SmsCreator();
        smsCreator.sendNotification(message);

        NotificationCreator pushCreator = new PushCreator();
        pushCreator.sendNotification(message);
    }
}
