package creational.singleton.problem;

// Đại diện bộ quản lý việc in của ứng dụng.
// Constructor public cho phép mỗi lần new tạo một bộ quản lý in riêng.
public class PrinterManager {
    // Constructor không có tham số, được gọi khi tạo object bằng new PrinterManager().
    public PrinterManager() {
        System.out.println("PrinterManager created");
    }
}
