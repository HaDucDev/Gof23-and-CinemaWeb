package behavioral.visitor.solution.visitor;

import behavioral.visitor.solution.element.Company;
import behavioral.visitor.solution.element.House;
import behavioral.visitor.solution.element.Park;

/** Gom cách xuất XML cho mọi loại địa điểm; demo in chuỗi XML mẫu ra console. */
public final class XmlExportVisitor implements MapVisitor {
    @Override
    public void visit(House house) {
        System.out.println(
                "[XML] <house name=\""
                        + house.getName()
                        + "\" address=\""
                        + house.getAddress()
                        + "\" residents=\""
                        + house.getResidents()
                        + "\"/>");
    }

    @Override
    public void visit(Park park) {
        System.out.println(
                "[XML] <park name=\""
                        + park.getName()
                        + "\" areaM2=\""
                        + park.getAreaSquareMeters()
                        + "\" trees=\""
                        + park.getTreeCount()
                        + "\"/>");
    }

    @Override
    public void visit(Company company) {
        System.out.println(
                "[XML] <company name=\""
                        + company.getName()
                        + "\" address=\""
                        + company.getAddress()
                        + "\" employees=\""
                        + company.getEmployees()
                        + "\"/>");
    }
}
