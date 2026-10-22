package behavioral.visitor.solution.element;

import behavioral.visitor.solution.visitor.MapVisitor;

/** Element công ty: cung cấp dữ liệu cho Visitor và dẫn lời gọi tới visit(Company). */
public final class Company implements MapElement {
    private final String name; // Tên công ty.
    private final String address; // Địa chỉ công ty.
    private final int employees; // Số nhân viên mà các Visitor đưa vào nội dung xuất.

    public Company(String name, String address, int employees) {
        this.name = name;
        this.address = address;
        this.employees = employees;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getEmployees() {
        return employees;
    }

    @Override
    public void accept(MapVisitor visitor) {
        System.out.println("[DISPATCH] Company.accept -> visitor.visit(Company)");
        // Kiểu Company của this chọn đúng overload; class Visitor quyết định cách xuất.
        visitor.visit(this);
    }
}
