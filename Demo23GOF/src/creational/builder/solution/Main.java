package creational.builder.solution;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Điểm chạy Builder: ghi nhận cấu hình từng bước, rồi build() tạo Computer hợp lệ.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // Cấu hình trên builder; build() mới tạo Computer.
        Computer gamingComputer =
                new ComputerBuilder()
                        .cpu("Intel i7")
                        .ram(32)
                        .ssd("1TB")
                        .gpu("RTX 5070")
                        .wifi(true)
                        .bluetooth(true)
                        .rgb(true)
                        .build();

        System.out.println("Gaming Computer:");
        System.out.println(gamingComputer);
        System.out.println();

        // Bỏ qua các tùy chọn không cần dùng.
        Computer officeComputer =
                new ComputerBuilder().cpu("Intel i5").ram(16).ssd("512GB").wifi(true).build();

        System.out.println("Office Computer:");
        System.out.println(officeComputer);

        // Bỏ comment để thấy build() từ chối cấu hình thiếu SSD.
        // Computer invalidComputer = new ComputerBuilder()
        //         .cpu("Intel i5")
        //         .ram(16)
        //         .build();
    }
}
