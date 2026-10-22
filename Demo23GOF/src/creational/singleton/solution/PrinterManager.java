package creational.singleton.solution;

// Đại diện bộ quản lý việc in của ứng dụng.
// Singleton: getInstance() trả lại một object dùng chung; constructor private.
public class PrinterManager {
    // Lưu object duy nhất của PrinterManager.
    // static: thuộc về class; final: không cho gán lại biến INSTANCE.
    // Tham chiếu tới bộ quản lý in duy nhất, được tạo khi class khởi tạo.
    private static final PrinterManager INSTANCE = new PrinterManager();

    // Constructor private, không có tham số: bên ngoài không thể tự new PrinterManager.
    private PrinterManager() {
        System.out.println("PrinterManager created");
    }

    // Gọi PrinterManager.getInstance() không cần tham số để lấy object dùng chung.
    public static PrinterManager getInstance() {
        return INSTANCE;
    }
}
