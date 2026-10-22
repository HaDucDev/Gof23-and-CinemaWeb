package structural.proxy.solution.service;

// Service thực hiện lấy báo cáo doanh thu; demo in dữ liệu giả lập và không tự kiểm tra quyền.
public class RealReportService implements ReportService {
    public void getReport() {
        System.out.println("Loading revenue report...");
        System.out.println("Revenue report: 500,000,000 VND");
    }
}
