package creational.factorymethod.solution.creator;

import creational.factorymethod.solution.notification.EmailNotification;
import creational.factorymethod.solution.notification.Notification;

// Creator chọn và tạo object thông báo qua email khi factory method được gọi.
public class EmailCreator extends NotificationCreator {
    // Ghi đè factory method để tạo thông báo email.
    @Override
    public Notification createNotification() {
        return new EmailNotification();
    }
}
