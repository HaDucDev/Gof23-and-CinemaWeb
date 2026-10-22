package structural.facade.problem.service;

import structural.facade.problem.model.Order;
import structural.facade.problem.model.OrderStatus;

// Mô phỏng thanh toán đơn đặt món từ ví khách; giữ số dư qua các lần thanh toán.
public class PaymentService {
    // Một ví giả lập của khách trong demo, giữ số dư qua các lần đặt món.
    // Số dư ví khách giả lập, tính bằng đồng; giảm sau thanh toán thành công.
    private long availableBalance = 200000;

    public boolean pay(Order order) {
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Chỉ thanh toán đơn đã được xác nhận.");
        }

        long amount = order.getTotalPrice();
        if (availableBalance < amount) {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            System.out.println(
                    "[Thanh toán] Thất bại cho "
                            + order.getId()
                            + ": cần "
                            + amount
                            + " đồng, ví còn "
                            + availableBalance
                            + " đồng.");
            return false;
        }

        availableBalance -= amount;
        order.setStatus(OrderStatus.PAID);
        System.out.println(
                "[Thanh toán] Đã trả "
                        + amount
                        + " đồng cho "
                        + order.getId()
                        + ". Ví còn "
                        + availableBalance
                        + " đồng.");
        return true;
    }
}
