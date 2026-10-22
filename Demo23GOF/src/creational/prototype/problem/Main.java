package creational.prototype.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Điểm chạy Prototype bản Problem: Client tự sao chép từng thuộc tính nhân viên.
public class Main {
    // args là mảng tham số dòng lệnh; demo này không sử dụng.
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // Tạo nhân viên A với các thông tin ban đầu.
        Employee employeeA = new Employee();
        employeeA.setName("Nguyen Van A");
        employeeA.setDepartment("Development");
        employeeA.setPosition("Developer");
        employeeA.setCompany("ABC Company");
        employeeA.setOffice("Ha Noi");

        // Tạo B: Main phải tự copy từng field từ A, dễ dài dòng và bỏ sót.
        Employee employeeB = new Employee();
        employeeB.setName("Nguyen Van B");
        employeeB.setDepartment(employeeA.getDepartment());
        employeeB.setPosition(employeeA.getPosition());
        employeeB.setCompany(employeeA.getCompany());
        employeeB.setOffice(employeeA.getOffice());

        System.out.println(
                "Nhân viên A: tên="
                        + employeeA.getName()
                        + " | phòng ban="
                        + employeeA.getDepartment()
                        + " | chức vụ="
                        + employeeA.getPosition()
                        + " | công ty="
                        + employeeA.getCompany()
                        + " | văn phòng="
                        + employeeA.getOffice());

        System.out.println(
                "Nhân viên B: tên="
                        + employeeB.getName()
                        + " | phòng ban="
                        + employeeB.getDepartment()
                        + " | chức vụ="
                        + employeeB.getPosition()
                        + " | công ty="
                        + employeeB.getCompany()
                        + " | văn phòng="
                        + employeeB.getOffice());

        // == kiểm tra cùng object; A và B là hai object riêng nên trả về false.
        System.out.println("A và B cùng object Employee (==): " + (employeeA == employeeB));
    }
}
