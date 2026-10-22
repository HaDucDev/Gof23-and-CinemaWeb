package creational.builder.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Điểm chạy cấu hình máy tính bằng constructor nhiều tham số hoặc các setter.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // Nhiều tham số: cần xem constructor để hiểu true, true, false là gì.
        // Thêm tùy chọn sẽ làm lời gọi constructor dài hơn.
        Computer gamingComputer =
                new Computer("Intel i7", 32, "1TB", "RTX 5070", true, true, false);

        System.out.println("Gaming Computer (constructor):");
        System.out.println(gamingComputer);
        System.out.println();

        Computer officeComputer = new Computer();
        officeComputer.setCpu("Intel i5");
        officeComputer.setRam(16);

        // Computer đã tồn tại, nhưng còn thiếu SSD bắt buộc.
        officeComputer.setSsd("512GB");
        officeComputer.setWifi(true);

        System.out.println("Office Computer (setters):");
        System.out.println(officeComputer);
    }
}
