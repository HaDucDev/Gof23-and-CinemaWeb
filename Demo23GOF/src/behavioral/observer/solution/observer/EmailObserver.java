package behavioral.observer.solution.observer;

import behavioral.observer.solution.model.Order;

// Concrete Observer: khi được thông báo thì thực hiện phần gửi email.
public class EmailObserver implements OrderObserver {
    public void update(Order order) {
        System.out.println(
                "[EMAIL OBSERVER] Gửi email: Đơn hàng #" + order.getId() + " đã giao thành công");
    }
}
