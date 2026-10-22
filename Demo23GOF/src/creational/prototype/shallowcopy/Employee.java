package creational.prototype.shallowcopy;

// Đại diện nhân viên có địa chỉ dạng object; copy() tạo nhân viên mới nhưng dùng chung Address.
public class Employee {
    private String name; // Tên nhân viên trong hồ sơ.
    // Tham chiếu tới địa chỉ; bản sao nông vẫn dùng chung object Address.
    private Address address;

    public Employee copy() {
        // Tạo Employee mới.
        Employee employeeCopy = new Employee();
        employeeCopy.name = this.name;

        // Chỉ sao chép tham chiếu, nên hai Employee dùng chung một Address.
        employeeCopy.address = this.address;
        return employeeCopy;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
