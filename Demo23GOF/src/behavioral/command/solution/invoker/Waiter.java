package behavioral.command.solution.invoker;

import behavioral.command.solution.command.Command;

import java.util.ArrayDeque;
import java.util.Queue;

// Invoker: nhận, xếp hàng và yêu cầu Command thực hiện; không chọn cách làm món.
public class Waiter {
    // Hàng đợi các object Command chứa yêu cầu làm món; FIFO: yêu cầu nhận trước được xử lý trước.
    private final Queue<Command> pendingOrders = new ArrayDeque<>();

    public void addOrder(Command command) {
        pendingOrders.add(command);
        System.out.println("[PHỤC VỤ] Đã nhận yêu cầu: " + command.getDescription());
    }

    public void showPendingOrders() {
        if (pendingOrders.isEmpty()) {
            System.out.println("[PHỤC VỤ] Không có yêu cầu đang chờ.");
            return;
        }

        // Chỉ xem hàng đợi, không lấy yêu cầu ra và không gọi execute().
        int position = 1;
        for (Command command : pendingOrders) {
            System.out.println(position + ". " + command.getDescription());
            position++;
        }
    }

    public void processNextOrder() {
        Command command = pendingOrders.poll(); // Lấy và bỏ yêu cầu đầu hàng đợi.
        if (command == null) {
            System.out.println("[PHỤC VỤ] Không có yêu cầu để xử lý.");
            return;
        }

        System.out.println("[PHỤC VỤ] Gửi yêu cầu: " + command.getDescription());
        // Object Command quyết định gọi bếp thế nào; Waiter không kiểm tra loại món.
        command.execute();
        System.out.println();
    }

    public void processAllOrders() {
        while (!pendingOrders.isEmpty()) {
            processNextOrder();
        }
    }
}
