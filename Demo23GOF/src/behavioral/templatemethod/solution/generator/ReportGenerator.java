package behavioral.templatemethod.solution.generator;

// Lớp cha chứa khung quy trình xuất báo cáo và cài đặt các bước dùng chung.
public abstract class ReportGenerator {
    // Template Method: final ngăn class con override để đổi thứ tự quy trình này.
    public final void generateReport() {
        loadData();
        processData();
        createFormat(); // Java gọi cách tạo định dạng do class con override.
        saveFile();
    }

    // Bước chung: PDF và Excel cùng dùng phần đọc dữ liệu của lớp cha.
    private void loadData() {
        System.out.println("1. [COMMON] Đọc dữ liệu");
    }

    // Bước chung: xử lý dữ liệu trước khi tạo định dạng.
    private void processData() {
        System.out.println("2. [COMMON] Xử lý dữ liệu");
    }

    // Bước thay đổi: mỗi class con phải cung cấp cách tạo định dạng riêng.
    protected abstract void createFormat();

    // Bước chung: demo mô phỏng lưu file bằng thông báo console.
    private void saveFile() {
        System.out.println("4. [COMMON] Mô phỏng lưu báo cáo");
    }
}
