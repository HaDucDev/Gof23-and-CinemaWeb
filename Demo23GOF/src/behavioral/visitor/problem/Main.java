package behavioral.visitor.problem;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Điểm chạy Problem: gọi các hàm xuất trực tiếp trên từng địa điểm; chỉ in mô phỏng ra console. */
public final class Main {
    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        run();
    }

    public static void run() {
        System.out.println("===== PROBLEM: ĐỊA ĐIỂM TỰ XUẤT DỮ LIỆU (MÔ PHỎNG) =====");
        HouseBad house = new HouseBad("Nhà A", "Hà Nội", 4);
        ParkBad park = new ParkBad("Công viên Hòa Bình", 20000, 500);
        CompanyBad company = new CompanyBad("Công ty ABC", "Hà Nội", 100);

        System.out.println("\n===== XUẤT XML =====");
        house.exportXml();
        park.exportXml();
        company.exportXml();

        System.out.println("\n===== XUẤT PDF =====");
        house.exportPdf();
        park.exportPdf();
        company.exportPdf();

        // exportExcel() đã được viết sẵn trong ba class địa điểm; chạy demo chỉ gọi các phương thức
        // đó.
        System.out.println("\n===== XUẤT EXCEL: GỌI exportExcel() TRÊN TỪNG ĐỊA ĐIỂM =====");
        house.exportExcel();
        park.exportExcel();
        company.exportExcel();
    }
}
