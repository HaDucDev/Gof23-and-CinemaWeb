package behavioral.visitor.solution.element;

import behavioral.visitor.solution.visitor.MapVisitor;

/** Element nhà ở: giữ dữ liệu, còn chức năng xuất được nhận từ MapVisitor qua accept(). */
public final class House implements MapElement {
    private final String name; // Tên nhà ở mà Visitor đọc qua getter.
    private final String address; // Địa chỉ của nhà.
    private final int residents; // Số người cư trú trong nhà.

    public House(String name, String address, int residents) {
        this.name = name;
        this.address = address;
        this.residents = residents;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getResidents() {
        return residents;
    }

    @Override
    public void accept(MapVisitor visitor) {
        // Log để quan sát luồng gọi khi demo; không chứa logic xuất dữ liệu.
        System.out.println("[DISPATCH] House.accept -> visitor.visit(House)");
        // this có kiểu House tại đây nên chọn overload visit(House) trên Visitor được truyền vào.
        visitor.visit(this);
    }
}
