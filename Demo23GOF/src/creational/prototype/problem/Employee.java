package creational.prototype.problem;

// Đại diện hồ sơ nhân viên với tên, phòng ban, chức vụ, công ty và văn phòng.
public class Employee {
    private String name; // Tên nhân viên trong hồ sơ.
    private String department; // Phòng ban nhân viên làm việc.
    private String position; // Chức vụ hoặc vị trí công việc của nhân viên.
    private String company; // Tên công ty của nhân viên.
    private String office; // Văn phòng làm việc của nhân viên.

    // Getter đọc giá trị của thuộc tính.
    public String getName() {
        return name;
    }

    // name là tham số; this.name là thuộc tính của object hiện tại.
    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }
}
