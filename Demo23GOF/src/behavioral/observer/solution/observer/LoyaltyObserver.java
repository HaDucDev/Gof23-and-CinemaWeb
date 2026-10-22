package behavioral.observer.solution.observer;

import behavioral.observer.solution.model.Order;

// Concrete Observer: khi được thông báo thì cộng 10 điểm trong phần mô phỏng.
public class LoyaltyObserver implements OrderObserver {
    public void update(Order order) {
        System.out.println("[LOYALTY OBSERVER] Cộng 10 điểm cho đơn hàng #" + order.getId());
    }
}
