package creational.factorymethod.solution.notification;

// Đại diện kênh gửi email; demo mô phỏng việc gửi bằng console.
public class EmailNotification implements Notification {
    @Override
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}
