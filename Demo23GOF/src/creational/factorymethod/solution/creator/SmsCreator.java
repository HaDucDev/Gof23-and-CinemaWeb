package creational.factorymethod.solution.creator;

import creational.factorymethod.solution.notification.SmsNotification;
import creational.factorymethod.solution.notification.Notification;

// Creator chọn và tạo object thông báo qua tin nhắn SMS khi factory method được gọi.
public class SmsCreator extends NotificationCreator {
    // Ghi đè factory method để tạo thông báo SMS.
    @Override
    public Notification createNotification() {
        return new SmsNotification();
    }
}
