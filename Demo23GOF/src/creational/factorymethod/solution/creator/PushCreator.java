package creational.factorymethod.solution.creator;

import creational.factorymethod.solution.notification.PushNotification;
import creational.factorymethod.solution.notification.Notification;

// Creator chọn và tạo object thông báo qua thông báo đẩy khi factory method được gọi.
public class PushCreator extends NotificationCreator {
    // Ghi đè factory method để tạo thông báo đẩy.
    @Override
    public Notification createNotification() {
        return new PushNotification();
    }
}
