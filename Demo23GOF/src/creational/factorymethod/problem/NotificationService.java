package creational.factorymethod.problem;

import creational.factorymethod.problem.notification.EmailNotification;
import creational.factorymethod.problem.notification.SmsNotification;
import creational.factorymethod.problem.notification.PushNotification;

// Dịch vụ gửi thông báo, tự chọn và tạo object cho từng kênh bằng if/else.
public class NotificationService {
    // type là kênh gửi; message là nội dung thông báo.
    public void sendNotification(String type, String message) {
        // Service trực tiếp chọn và tạo từng loại thông báo.
        // Khi thêm kênh mới, phải sửa chuỗi if/else này.
        if ("email".equals(type)) {
            EmailNotification notification = new EmailNotification();
            notification.send(message);
        } else if ("sms".equals(type)) {
            SmsNotification notification = new SmsNotification();
            notification.send(message);
        } else if ("push".equals(type)) {
            PushNotification notification = new PushNotification();
            notification.send(message);
        } else {
            System.out.println("Unknown notification type: " + type);
        }
    }
}
