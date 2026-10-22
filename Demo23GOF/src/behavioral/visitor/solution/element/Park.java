package behavioral.visitor.solution.element;

import behavioral.visitor.solution.visitor.MapVisitor;

/** Element công viên: giữ dữ liệu và chuyển chức năng xử lý sang visit(Park) của Visitor. */
public final class Park implements MapElement {
    private final String name; // Tên công viên.
    private final int areaSquareMeters; // Diện tích, đơn vị m².
    private final int treeCount; // Số cây trong công viên.

    public Park(String name, int areaSquareMeters, int treeCount) {
        this.name = name;
        this.areaSquareMeters = areaSquareMeters;
        this.treeCount = treeCount;
    }

    public String getName() {
        return name;
    }

    public int getAreaSquareMeters() {
        return areaSquareMeters;
    }

    public int getTreeCount() {
        return treeCount;
    }

    @Override
    public void accept(MapVisitor visitor) {
        System.out.println("[DISPATCH] Park.accept -> visitor.visit(Park)");
        // this có kiểu Park tại đây, giúp Visitor nhận đúng loại địa điểm.
        visitor.visit(this);
    }
}
