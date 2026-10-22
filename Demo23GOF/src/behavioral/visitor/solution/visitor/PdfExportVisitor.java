package behavioral.visitor.solution.visitor;

import behavioral.visitor.solution.element.Company;
import behavioral.visitor.solution.element.House;
import behavioral.visitor.solution.element.Park;

/** Gom cách xuất PDF cho mọi loại địa điểm; demo chỉ in nội dung mô phỏng, không tạo file PDF. */
public final class PdfExportVisitor implements MapVisitor {
    @Override
    public void visit(House house) {
        System.out.println(
                "[PDF] Nhà ở: "
                        + house.getName()
                        + " | Địa chỉ: "
                        + house.getAddress()
                        + " | Số người: "
                        + house.getResidents());
    }

    @Override
    public void visit(Park park) {
        System.out.println(
                "[PDF] Công viên: "
                        + park.getName()
                        + " | Diện tích: "
                        + park.getAreaSquareMeters()
                        + " m² | Số cây: "
                        + park.getTreeCount());
    }

    @Override
    public void visit(Company company) {
        System.out.println(
                "[PDF] Công ty: "
                        + company.getName()
                        + " | Địa chỉ: "
                        + company.getAddress()
                        + " | Nhân viên: "
                        + company.getEmployees());
    }
}
