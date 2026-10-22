package behavioral.visitor.solution;

import behavioral.visitor.solution.element.Company;
import behavioral.visitor.solution.element.House;
import behavioral.visitor.solution.element.MapElement;
import behavioral.visitor.solution.element.Park;
import behavioral.visitor.solution.visitor.ExcelExportVisitor;
import behavioral.visitor.solution.visitor.MapVisitor;
import behavioral.visitor.solution.visitor.PdfExportVisitor;
import behavioral.visitor.solution.visitor.XmlExportVisitor;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** Điểm chạy Solution: tạo các Element rồi dùng từng Visitor để xuất cùng bộ địa điểm. */
public final class Main {
    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        run();
    }

    // Tạo danh sách object địa điểm trên bản đồ: một House, một Park và một Company.
    public static List<MapElement> createElements() {
        return Arrays.<MapElement>asList(
                new House("Nhà A", "Hà Nội", 4),
                new Park("Công viên Hòa Bình", 20000, 500),
                new Company("Công ty ABC", "Hà Nội", 100));
    }

    public static void run() {
        System.out.println("===== SOLUTION: XUẤT DỮ LIỆU QUA VISITOR (MÔ PHỎNG) =====");
        // Danh sách object nhà ở, công viên và công ty được dùng chung cho các lần xuất.
        List<MapElement> elements = createElements();

        System.out.println("\n===== XUẤT XML =====");
        MapVisitor xmlVisitor = new XmlExportVisitor();
        visitAll(elements, xmlVisitor);

        System.out.println("\n===== XUẤT PDF =====");
        MapVisitor pdfVisitor = new PdfExportVisitor();
        visitAll(elements, pdfVisitor);

        // ExcelExportVisitor đã có sẵn trong source; Main tạo object rồi gọi qua cùng visitAll()
        // như XML/PDF.
        System.out.println("\n===== XUẤT EXCEL: DÙNG ExcelExportVisitor =====");
        MapVisitor excelVisitor = new ExcelExportVisitor();
        visitAll(elements, excelVisitor);
    }

    // elements là danh sách object địa điểm; visitor là object thực hiện chức năng xuất XML, PDF
    // hoặc Excel.
    // Lần gọi thứ nhất chọn accept() theo loại Element; từ đó visit() chọn hành vi của Visitor.
    public static void visitAll(List<MapElement> elements, MapVisitor visitor) {
        for (MapElement element : elements) {
            element.accept(visitor);
        }
    }
}
