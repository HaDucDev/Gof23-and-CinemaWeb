package behavioral.visitor.solution.visitor;

import behavioral.visitor.solution.element.Company;
import behavioral.visitor.solution.element.House;
import behavioral.visitor.solution.element.Park;

/** Chức năng xuất Excel mới nằm trong Visitor riêng; demo chỉ in các cột mô phỏng ra console. */
public final class ExcelExportVisitor implements MapVisitor {
    @Override
    public void visit(House house) {
        System.out.println(
                "[EXCEL] Nhà ở | Tên="
                        + house.getName()
                        + " | Địa chỉ="
                        + house.getAddress()
                        + " | Số người="
                        + house.getResidents());
    }

    @Override
    public void visit(Park park) {
        System.out.println(
                "[EXCEL] Công viên | Tên="
                        + park.getName()
                        + " | Diện tích(m²)="
                        + park.getAreaSquareMeters()
                        + " | Số cây="
                        + park.getTreeCount());
    }

    @Override
    public void visit(Company company) {
        System.out.println(
                "[EXCEL] Công ty | Tên="
                        + company.getName()
                        + " | Địa chỉ="
                        + company.getAddress()
                        + " | Nhân viên="
                        + company.getEmployees());
    }
}
