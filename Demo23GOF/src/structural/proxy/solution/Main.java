package structural.proxy.solution;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import structural.proxy.solution.model.Role;
import structural.proxy.solution.model.User;
import structural.proxy.solution.proxy.ReportProxy;
import structural.proxy.solution.service.ReportService;

// Điểm chạy Proxy: chọn người dùng rồi xem báo cáo qua lớp kiểm tra quyền.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        User manager = new User("Alice", Role.MANAGER);
        User staff = new User("Bob", Role.STAFF);

        // Khởi tạo Proxy cho từng người dùng; phía sử dụng giữ kiểu ReportService.
        ReportService managerReport = new ReportProxy(manager);
        ReportService staffReport = new ReportProxy(staff);

        System.out.println("=== Manager ===");
        managerReport.getReport();

        System.out.println();
        System.out.println("=== Staff ===");
        staffReport.getReport();
    }
}
