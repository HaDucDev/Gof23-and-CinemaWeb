package behavioral.visitor.solution.element;

import behavioral.visitor.solution.visitor.MapVisitor;

/** Interface chung của các địa điểm trên bản đồ, khai báo accept() để nhận một chức năng từ bên ngoài. */
public interface MapElement {
    // Địa điểm cụ thể gọi ngược visitor.visit(this) để chọn đúng hàm xử lý cho loại của nó.
    void accept(MapVisitor visitor);
}
