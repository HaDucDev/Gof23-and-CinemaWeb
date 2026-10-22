package behavioral.observer.solution.observer;

import behavioral.observer.solution.model.Order;

// Interface khai báo update() cho các bên nhận thông báo; tham số là đơn hàng đã được giao.
public interface OrderObserver {
    void update(Order order);
}
