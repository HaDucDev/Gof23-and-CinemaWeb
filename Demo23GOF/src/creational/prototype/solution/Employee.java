package creational.prototype.solution;

// Đại diện hồ sơ nhân viên; copy() tạo Employee mới mang các giá trị của hồ sơ gốc.
public class Employee {
    private String name; // Tên nhân viên trong hồ sơ.
    private String department; // Phòng ban nhân viên làm việc.
    private String position; // Chức vụ hoặc vị trí công việc của nhân viên.
    private String company; // Tên công ty của nhân viên.
    private String office; // Văn phòng làm việc của nhân viên.

    // Prototype: object tự cung cấp cách sao chép chính nó.
    // copy() không nhận tham số và trả về một Employee mới.
    public Employee copy() {
        // this là object gốc; employeeCopy là object mới nhận dữ liệu từ object gốc.
        Employee employeeCopy = new Employee();
        employeeCopy.name = this.name;
        employeeCopy.department = this.department;
        employeeCopy.position = this.position;
        employeeCopy.company = this.company;
        employeeCopy.office = this.office;
        return employeeCopy;
    }

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
