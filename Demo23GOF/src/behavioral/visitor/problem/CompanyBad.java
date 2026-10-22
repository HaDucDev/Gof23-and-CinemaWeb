package behavioral.visitor.problem;

/** Công ty bản Problem: vừa giữ dữ liệu địa điểm, vừa tự chứa từng chức năng xuất. */
public final class CompanyBad {
    private final String name; // Tên công ty.
    private final String address; // Địa chỉ công ty trên bản đồ.
    private final int employees; // Số nhân viên đưa vào nội dung xuất.

    public CompanyBad(String name, String address, int employees) {
        this.name = name;
        this.address = address;
        this.employees = employees;
    }

    public void exportXml() {
        System.out.println(
                "[XML] <company name=\""
                        + name
                        + "\" address=\""
                        + address
                        + "\" employees=\""
                        + employees
                        + "\"/>");
    }

    public void exportPdf() {
        System.out.println(
                "[PDF] Công ty: " + name + " | Địa chỉ: " + address + " | Nhân viên: " + employees);
    }

    // Cùng yêu cầu Excel, class CompanyBad cũng phải được sửa.
    public void exportExcel() {
        System.out.println(
                "[EXCEL] Công ty | Tên="
                        + name
                        + " | Địa chỉ="
                        + address
                        + " | Nhân viên="
                        + employees);
    }
}
