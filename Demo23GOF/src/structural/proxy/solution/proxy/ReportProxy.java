package structural.proxy.solution.proxy;

import java.util.Objects;

import structural.proxy.solution.model.Role;
import structural.proxy.solution.model.User;
import structural.proxy.solution.service.RealReportService;
import structural.proxy.solution.service.ReportService;

// Lớp đại diện service báo cáo: kiểm tra quyền của người dùng trước khi gọi service thật.
public class ReportProxy implements ReportService {
    private final User user; // Người dùng gắn với Proxy này.
    // Đối tượng thực hiện việc lấy báo cáo.
    private final ReportService realReportService;

    public ReportProxy(User user) {
        this.user = Objects.requireNonNull(user, "Người dùng không được null.");
        this.realReportService = new RealReportService();
    }

    public void getReport() {
        if (user.getRole() == Role.MANAGER) {
            System.out.println("Access granted for: " + user.getName());
            realReportService.getReport();
        } else {
            // Từ chối tại Proxy; không gọi service thật.
            System.out.println("Access denied for: " + user.getName());
        }
    }
}
