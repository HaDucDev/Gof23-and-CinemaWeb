package behavioral.templatemethod.solution;

import behavioral.templatemethod.solution.generator.ExcelReportGenerator;
import behavioral.templatemethod.solution.generator.PdfReportGenerator;
import behavioral.templatemethod.solution.generator.ReportGenerator;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Client: chọn loại báo cáo rồi gọi cùng Template Method generateReport().
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("===== XUẤT PDF (MÔ PHỎNG) =====");
        System.out.println();
        // ReportGenerator là kiểu chung; object được tạo từ class cụ thể PdfReportGenerator.
        ReportGenerator pdf = new PdfReportGenerator();
        pdf.generateReport(); // Chạy phương thức được kế thừa từ ReportGenerator.

        System.out.println();
        System.out.println("===== XUẤT EXCEL (MÔ PHỎNG) =====");
        System.out.println();
        ReportGenerator excel = new ExcelReportGenerator();
        excel.generateReport(); // Cùng khung; bước createFormat() chạy bản Excel.
    }
}
