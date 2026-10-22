package structural.proxy.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import structural.proxy.problem.model.Role;
import structural.proxy.problem.model.User;
import structural.proxy.problem.service.RealReportService;

// Điểm chạy báo cáo: Client tự kiểm tra quyền và minh họa một lần quên kiểm tra.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        User manager = new User("Alice", Role.MANAGER);
        User staff = new User("Bob", Role.STAFF);
        RealReportService reportService = new RealReportService();

        System.out.println("=== Manager ===");
        if (manager.getRole() == Role.MANAGER) {
            reportService.getReport();
        } else {
            System.out.println("Access denied");
        }

        System.out.println();
        System.out.println("=== Staff ===");
        // Một nơi gọi khác phải lặp lại cùng điều kiện kiểm tra quyền.
        if (staff.getRole() == Role.MANAGER) {
            reportService.getReport();
        } else {
            System.out.println("Access denied");
        }

        System.out.println();
        System.out.println("=== Forgot permission check ===");
        // Giả lập màn hình của Bob quên kiểm tra quyền: service vẫn trả báo cáo.
        reportService.getReport();
    }
}
