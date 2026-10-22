package behavioral.observer.solution.subject;

import behavioral.observer.solution.model.Order;
import behavioral.observer.solution.observer.OrderObserver;

import java.util.ArrayList;
import java.util.List;

// Subject: giữ các bên đã đăng ký và gửi cùng thông báo cho từng bên.
public class OrderSubject {
    // Danh sách tham chiếu tới các object nhận sự kiện đơn hàng (EmailObserver, SmsObserver,
    // LoyaltyObserver), theo thứ tự đăng ký.
    private final List<OrderObserver> observers = new ArrayList<>();

    public void subscribe(OrderObserver observer) {
        if (observers.contains(observer)) {
            return; // Cùng một object chỉ được đăng ký một lần.
        }
        observers.add(observer);
        System.out.println("[SUBSCRIBE] " + observer.getClass().getSimpleName());
    }

    public void unsubscribe(OrderObserver observer) {
        if (observers.remove(observer)) {
            System.out.println("[UNSUBSCRIBE] " + observer.getClass().getSimpleName());
        }
    }

    public void notifyObservers(Order order) {
        System.out.println("[NOTIFY] Thông báo cho " + observers.size() + " Observer...");
        for (OrderObserver observer : observers) {
            // Gọi update của object thực tế: EmailObserver, SmsObserver hoặc LoyaltyObserver.
            observer.update(order);
        }
    }
}
