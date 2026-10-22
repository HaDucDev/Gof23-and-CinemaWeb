package behavioral.command.solution;

import behavioral.command.solution.command.Command;
import behavioral.command.solution.command.MakeCoffeeCommand;
import behavioral.command.solution.command.MakeFriedRiceCommand;
import behavioral.command.solution.command.MakePhoCommand;
import behavioral.command.solution.invoker.Waiter;
import behavioral.command.solution.receiver.Kitchen;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// Client: tạo các yêu cầu cụ thể rồi giao cho người phục vụ xếp hàng và xử lý.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("===== ÁP DỤNG COMMAND =====");
        System.out.println();

        Kitchen kitchen = new Kitchen();
        Waiter waiter = new Waiter();

        // Yêu cầu làm Phở bò cho Bàn 01 đã trở thành một object.
        // Command là kiểu chung; object thực tế được tạo từ class MakePhoCommand.
        Command order = new MakePhoCommand(kitchen, "Bàn 01");
        Command friedRiceOrder = new MakeFriedRiceCommand(kitchen, "Bàn 02");
        Command coffeeOrder = new MakeCoffeeCommand(kitchen, "Bàn 03");

        System.out.println("===== NHẬN YÊU CẦU =====");
        // Nhận đủ ba yêu cầu nhưng chưa yêu cầu bếp làm món.
        waiter.addOrder(order);
        waiter.addOrder(friedRiceOrder);
        waiter.addOrder(coffeeOrder);

        System.out.println();
        System.out.println("===== YÊU CẦU ĐANG CHỜ =====");
        waiter.showPendingOrders();

        System.out.println();
        System.out.println("===== BẮT ĐẦU XỬ LÝ =====");
        waiter.processNextOrder(); // Xử lý yêu cầu đầu tiên: Phở bò của Bàn 01.
        waiter.processAllOrders(); // Xử lý hai yêu cầu còn lại theo thứ tự đã nhận.
        System.out.println("===== HOÀN THÀNH =====");

        System.out.println();
        System.out.println("===== VÍ DỤ THÊM: LƯU COMMAND TRONG LIST =====");
        Command extraOrder = new MakeCoffeeCommand(kitchen, "Bàn 04");
        // Danh sách object yêu cầu làm món (Command), có thể lấy ra thực hiện sau.
        List<Command> savedOrders = new ArrayList<>();
        savedOrders.add(extraOrder); // List giữ yêu cầu như một object bình thường.
        System.out.println("[MAIN] Đã lưu yêu cầu vào savedOrders: " + extraOrder.getDescription());
        System.out.println(
                "[MAIN] Yêu cầu Bàn 04 đang được lưu; execute() của yêu cầu này chưa được gọi.");

        System.out.println();
        System.out.println("[MAIN] Lấy yêu cầu từ savedOrders rồi gọi execute():");
        Command savedOrder = savedOrders.get(0);
        savedOrder.execute();
    }
}
