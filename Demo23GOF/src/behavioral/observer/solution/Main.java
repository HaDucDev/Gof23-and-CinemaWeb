package behavioral.observer.solution;

import behavioral.observer.solution.model.Order;
import behavioral.observer.solution.observer.EmailObserver;
import behavioral.observer.solution.observer.LoyaltyObserver;
import behavioral.observer.solution.observer.SmsObserver;
import behavioral.observer.solution.service.OrderService;
import behavioral.observer.solution.subject.OrderSubject;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Client: tạo các bên nhận, đăng ký rồi thay đổi danh sách nhận trong lúc chạy.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("===== SOLUTION: OBSERVER PATTERN =====");
        System.out.println();

        OrderSubject subject = new OrderSubject();
        EmailObserver emailObserver = new EmailObserver();
        SmsObserver smsObserver = new SmsObserver();
        LoyaltyObserver loyaltyObserver = new LoyaltyObserver();

        // Đăng ký chỉ lưu các object; chưa gửi email, SMS hoặc cộng điểm.
        subject.subscribe(emailObserver);
        subject.subscribe(smsObserver);
        subject.subscribe(loyaltyObserver);

        // Service phải dùng cùng Subject vừa được đăng ký ở trên.
        OrderService orderService = new OrderService(subject);
        Order order = new Order("ORD-001");
        System.out.println();
        orderService.deliverOrder(order); // Cả ba Observer nhận đơn ORD-001.

        System.out.println();
        System.out.println("===== HỦY ĐĂNG KÝ SMS =====");
        subject.unsubscribe(smsObserver); // Truyền lại chính object đã đăng ký.

        Order order2 = new Order("ORD-002");
        System.out.println();
        orderService.deliverOrder(order2); // Cùng service, giờ chỉ còn Email và Loyalty.
    }
}
