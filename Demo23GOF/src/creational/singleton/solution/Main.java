package creational.singleton.solution;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Điểm chạy Singleton bản Solution: lấy cùng object quản lý in qua getInstance().
public class Main {
    // args là mảng tham số dòng lệnh; demo này không sử dụng.
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // Ba lần gọi getInstance() trả về cùng object; A, B, C cùng tham chiếu tới nó.
        PrinterManager managerA = PrinterManager.getInstance();
        PrinterManager managerB = PrinterManager.getInstance();
        PrinterManager managerC = PrinterManager.getInstance();
        System.out.println();

        // == kiểm tra chính xác hai biến có trỏ tới cùng object không.
        // Bản này dùng chung một object nên kết quả là true.
        System.out.println("managerA và managerB cùng object (==): " + (managerA == managerB));
        System.out.println("managerB và managerC cùng object (==): " + (managerB == managerC));
    }
}
