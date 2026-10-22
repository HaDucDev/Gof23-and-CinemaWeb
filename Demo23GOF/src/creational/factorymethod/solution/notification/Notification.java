package creational.factorymethod.solution.notification;

// Interface khai báo send(); các kênh gửi thông báo cài đặt phương thức này theo cách riêng.
public interface Notification {
    // message là nội dung cần gửi.
    // Gửi nội dung message theo kênh của implementation; demo in ra console.
    void send(String message);
}
