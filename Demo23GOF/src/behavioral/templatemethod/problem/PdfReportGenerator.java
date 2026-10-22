package behavioral.templatemethod.problem;

// Chưa dùng Template Method: PDF tự giữ cả quy trình và các bước chung.
public class PdfReportGenerator {
    public void generateReport() {
        loadData();
        processData();
        createFormat();
        saveFile();
    }

    private void loadData() {
        System.out.println("1. [COMMON] Đọc dữ liệu");
    }

    private void processData() {
        System.out.println("2. [COMMON] Xử lý dữ liệu");
    }

    private void createFormat() {
        System.out.println("3. [PDF] Đang tạo định dạng PDF");
    }

    private void saveFile() {
        System.out.println("4. [COMMON] Mô phỏng lưu báo cáo");
    }
}
