package behavioral.templatemethod.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Hai class độc lập tự thực hiện toàn bộ quy trình xuất báo cáo.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("===== XUẤT PDF (MÔ PHỎNG) =====");
        System.out.println();
        PdfReportGenerator pdf = new PdfReportGenerator();
        pdf.generateReport();

        System.out.println();
        System.out.println("===== XUẤT EXCEL (MÔ PHỎNG) =====");
        System.out.println();
        ExcelReportGenerator excel = new ExcelReportGenerator();
        excel.generateReport();
    }
}
