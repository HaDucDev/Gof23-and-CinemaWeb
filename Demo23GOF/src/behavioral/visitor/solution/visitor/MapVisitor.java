package behavioral.visitor.solution.visitor;

import behavioral.visitor.solution.element.Company;
import behavioral.visitor.solution.element.House;
import behavioral.visitor.solution.element.Park;

/** Interface Visitor khai báo visit() cho từng loại địa điểm; mỗi Visitor cụ thể cài đặt cách xử lý cả ba loại. */
public interface MapVisitor {
    // Các overload khác kiểu tham số; thêm loại địa điểm sẽ cần bổ sung phương thức visit() vào
    // interface này.
    void visit(House house);

    void visit(Park park);

    void visit(Company company);
}
