package behavioral.visitor.problem;

/** Nhà ở bản Problem: mỗi định dạng xuất mới lại cần thêm phương thức vào class này. */
public final class HouseBad {
    private final String name; // Tên nhà ở.
    private final String address; // Địa chỉ nhà trên bản đồ.
    private final int residents; // Số người cư trú đưa vào nội dung xuất.

    public HouseBad(String name, String address, int residents) {
        this.name = name;
        this.address = address;
        this.residents = residents;
    }

    public void exportXml() {
        System.out.println(
                "[XML] <house name=\""
                        + name
                        + "\" address=\""
                        + address
                        + "\" residents=\""
                        + residents
                        + "\"/>");
    }

    public void exportPdf() {
        System.out.println(
                "[PDF] Nhà ở: " + name + " | Địa chỉ: " + address + " | Số người: " + residents);
    }

    // Yêu cầu Excel khiến phải bổ sung phương thức vào chính class địa điểm.
    public void exportExcel() {
        System.out.println(
                "[EXCEL] Nhà ở | Tên="
                        + name
                        + " | Địa chỉ="
                        + address
                        + " | Số người="
                        + residents);
    }
}
