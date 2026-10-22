package behavioral.command.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Cùng ba yêu cầu như solution, nhưng nhận yêu cầu nào thì gọi bếp xử lý ngay.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        Kitchen kitchen = new Kitchen();
        Waiter waiter = new Waiter(kitchen);

        System.out.println("===== CHƯA ÁP DỤNG COMMAND =====");
        System.out.println();
        System.out.println("===== NHẬN VÀ XỬ LÝ TRỰC TIẾP =====");
        waiter.orderPho("Bàn 01"); // Bếp làm Phở bò trước khi nhận yêu cầu tiếp theo.
        waiter.orderFriedRice("Bàn 02");
        waiter.orderCoffee("Bàn 03");
        System.out.println("===== HOÀN THÀNH =====");
    }
}
