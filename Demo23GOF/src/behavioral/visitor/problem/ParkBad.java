package behavioral.visitor.problem;

/** Công viên bản Problem: dữ liệu công viên và các cách xuất XML/PDF/Excel nằm chung một class. */
public final class ParkBad {
    private final String name; // Tên công viên.
    private final int areaSquareMeters; // Diện tích công viên, đơn vị m².
    private final int treeCount; // Số cây trong công viên.

    public ParkBad(String name, int areaSquareMeters, int treeCount) {
        this.name = name;
        this.areaSquareMeters = areaSquareMeters;
        this.treeCount = treeCount;
    }

    public void exportXml() {
        System.out.println(
                "[XML] <park name=\""
                        + name
                        + "\" areaM2=\""
                        + areaSquareMeters
                        + "\" trees=\""
                        + treeCount
                        + "\"/>");
    }

    public void exportPdf() {
        System.out.println(
                "[PDF] Công viên: "
                        + name
                        + " | Diện tích: "
                        + areaSquareMeters
                        + " m² | Số cây: "
                        + treeCount);
    }

    // Cùng yêu cầu Excel, class ParkBad cũng phải được sửa.
    public void exportExcel() {
        System.out.println(
                "[EXCEL] Công viên | Tên="
                        + name
                        + " | Diện tích(m²)="
                        + areaSquareMeters
                        + " | Số cây="
                        + treeCount);
    }
}
