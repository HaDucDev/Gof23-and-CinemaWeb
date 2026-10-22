package creational.factorymethod.solution.notification;

// Đại diện kênh gửi tin nhắn SMS; demo mô phỏng việc gửi bằng console.
public class SmsNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}
