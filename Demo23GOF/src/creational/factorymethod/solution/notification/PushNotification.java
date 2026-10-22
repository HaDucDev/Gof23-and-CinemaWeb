package creational.factorymethod.solution.notification;

// Đại diện kênh gửi thông báo đẩy; demo mô phỏng việc gửi bằng console.
public class PushNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("Push: " + message);
    }
}
