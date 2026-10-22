package behavioral.observer.solution.observer;

import behavioral.observer.solution.model.Order;

// Concrete Observer: khi được thông báo thì thực hiện phần gửi SMS.
public class SmsObserver implements OrderObserver {
    public void update(Order order) {
        System.out.println("[SMS OBSERVER] Gửi SMS: Đơn hàng #" + order.getId() + " đã giao");
    }
}
