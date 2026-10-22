package behavioral.chainofresponsibility.solution;

import behavioral.chainofresponsibility.solution.handler.CustomerServiceHandler;
import behavioral.chainofresponsibility.solution.handler.ManagerHandler;
import behavioral.chainofresponsibility.solution.handler.RefundHandler;
import behavioral.chainofresponsibility.solution.handler.TeamLeaderHandler;
import behavioral.chainofresponsibility.solution.model.RefundRequest;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Client: tạo các cấp, nối chuỗi rồi đưa mọi yêu cầu vào cùng một đầu chuỗi.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        CustomerServiceHandler staff = new CustomerServiceHandler();
        TeamLeaderHandler leader = new TeamLeaderHandler();
        ManagerHandler manager = new ManagerHandler();

        // Nối từng bước để thấy rõ: CS -> Trưởng nhóm -> Manager.
        staff.setNext(leader);
        leader.setNext(manager);
        // Vẫn là object CS vừa tạo, không tạo thêm object.
        RefundHandler firstHandler = staff;

        RefundRequest[] requests = {
            new RefundRequest(500_000), new RefundRequest(5_000_000), new RefundRequest(20_000_000)
        };

        System.out.println("===== SOLUTION: CHAIN OF RESPONSIBILITY =====");
        for (RefundRequest request : requests) {
            System.out.println();
            System.out.println("--- Yêu cầu: " + request.getFormattedAmount() + " ---");
            // Luôn bắt đầu ở CS; các cấp tự xử lý hoặc chuyển tiếp.
            firstHandler.handle(request);
        }
    }
}
