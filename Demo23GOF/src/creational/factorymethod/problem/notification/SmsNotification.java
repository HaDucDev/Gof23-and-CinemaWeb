package creational.factorymethod.problem.notification;

// Đại diện kênh gửi tin nhắn SMS; demo mô phỏng việc gửi bằng console.
public class SmsNotification {
    // message là nội dung cần gửi qua SMS.
    public void send(String message) {
        System.out.println("SMS: " + message);
    }
}
