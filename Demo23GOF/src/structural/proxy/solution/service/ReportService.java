package structural.proxy.solution.service;

// Interface khai báo getReport(), được cả Proxy và service báo cáo thật triển khai (Subject).
public interface ReportService {
    // Yêu cầu lấy báo cáo doanh thu; Proxy có thể kiểm tra quyền trước khi chuyển tiếp.
    void getReport();
}
