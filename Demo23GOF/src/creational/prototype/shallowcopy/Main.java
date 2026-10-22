package creational.prototype.shallowcopy;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Điểm chạy sao chép nông: hai Employee riêng vẫn tham chiếu cùng Address.
public class Main {
    // args là mảng tham số dòng lệnh; demo này không sử dụng.
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        Address address = new Address("Ha Noi");

        Employee employeeA = new Employee();
        employeeA.setName("Nguyen Van A");
        employeeA.setAddress(address);

        // B là Employee mới, nhưng vẫn giữ cùng Address với A.
        Employee employeeB = employeeA.copy();
        employeeB.setName("Nguyen Van B");

        // So sánh Employee và Address riêng để thấy rõ phần nào được dùng chung.
        System.out.println("A và B cùng object Employee (==): " + (employeeA == employeeB));
        System.out.println(
                "A và B cùng object Address (==): "
                        + (employeeA.getAddress() == employeeB.getAddress()));
        System.out.println();

        System.out.println("Trước khi sửa thành phố qua địa chỉ của B:");
        System.out.println(
                "Nhân viên A: tên="
                        + employeeA.getName()
                        + " | thành phố="
                        + employeeA.getAddress().getCity());
        System.out.println(
                "Nhân viên B: tên="
                        + employeeB.getName()
                        + " | thành phố="
                        + employeeB.getAddress().getCity());
        System.out.println();

        // Lấy Address thông qua B rồi sửa nội dung object Address đang dùng chung.
        Address addressOfB = employeeB.getAddress();
        addressOfB.setCity("Da Nang");

        System.out.println("Sau khi sửa thành phố qua địa chỉ của B:");
        System.out.println(
                "Nhân viên A: tên="
                        + employeeA.getName()
                        + " | thành phố="
                        + employeeA.getAddress().getCity());
        System.out.println(
                "Nhân viên B: tên="
                        + employeeB.getName()
                        + " | thành phố="
                        + employeeB.getAddress().getCity());
    }
}
