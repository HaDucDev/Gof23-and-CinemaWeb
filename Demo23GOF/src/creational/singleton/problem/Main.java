package creational.singleton.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Điểm chạy Singleton bản Problem: tạo nhiều object quản lý in riêng.
public class Main {
    // args là mảng tham số dòng lệnh; demo này không sử dụng.
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // Mỗi lần new tạo một đối tượng quản lý việc in riêng.
        PrinterManager managerA = new PrinterManager();
        PrinterManager managerB = new PrinterManager();
        PrinterManager managerC = new PrinterManager();
        System.out.println();

        // == kiểm tra chính xác hai biến có trỏ tới cùng object không.
        // Bản này có các object khác nhau nên kết quả là false.
        System.out.println("managerA và managerB cùng object (==): " + (managerA == managerB));
        System.out.println("managerB và managerC cùng object (==): " + (managerB == managerC));
    }
}
