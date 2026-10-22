package creational.factorymethod.problem.notification;

// Đại diện kênh gửi email; demo mô phỏng việc gửi bằng console.
public class EmailNotification {
    // message là nội dung cần gửi qua email.
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}
