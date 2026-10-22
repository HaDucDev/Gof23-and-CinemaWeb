package creational.factorymethod.problem.notification;

// Đại diện kênh gửi thông báo đẩy; demo mô phỏng việc gửi bằng console.
public class PushNotification {
    // message là nội dung cần gửi qua thông báo đẩy.
    public void send(String message) {
        System.out.println("Push: " + message);
    }
}
