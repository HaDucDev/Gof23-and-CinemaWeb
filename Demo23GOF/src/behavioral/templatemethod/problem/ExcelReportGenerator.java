package behavioral.templatemethod.problem;

// Excel lặp lại cùng quy trình và ba bước chung đã có trong bản PDF.
public class ExcelReportGenerator {
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
        System.out.println("3. [EXCEL] Đang tạo định dạng Excel");
    }

    private void saveFile() {
        System.out.println("4. [COMMON] Mô phỏng lưu báo cáo");
    }
}
