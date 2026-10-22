package creational.prototype.shallowcopy;

// Đại diện địa chỉ của nhân viên; thành phố có thể thay đổi để minh họa tham chiếu dùng chung.
public class Address {
    private String city; // Tên thành phố của địa chỉ; có thể đổi qua setCity().

    // city là tên thành phố được truyền vào khi tạo Address.
    public Address(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    // Address có thể thay đổi vì phương thức này gán lại thuộc tính city.
    public void setCity(String city) {
        this.city = city;
    }
}
