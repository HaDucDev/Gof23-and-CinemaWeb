package structural.composite.solution;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import structural.composite.solution.composite.Box;
import structural.composite.solution.leaf.Product;

// Điểm chạy Composite: sản phẩm trả giá riêng, hộp tự cộng giá các phần tử con.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // 1. Tạo các sản phẩm riêng lẻ.
        Product phone = new Product("Phone", 10_000_000);
        Product headphones = new Product("Headphones", 1_000_000);
        Product charger = new Product("Charger", 500_000);
        Product cable = new Product("Cable", 200_000);

        // 2. Hộp phụ kiện chứa Charger và Cable.
        Box accessoryBox = new Box("Accessory Box");
        accessoryBox.add(charger);
        accessoryBox.add(cable);

        // 3. Hộp quà chứa Phone, Headphones và cả hộp phụ kiện.
        Box giftBox = new Box("Gift Box");
        giftBox.add(phone);
        giftBox.add(headphones);
        giftBox.add(accessoryBox);

        // 4. Product và Box đều có getPrice() theo giao diện Item.
        double phonePrice = phone.getPrice();
        double accessoryBoxPrice = accessoryBox.getPrice();
        double giftBoxPrice = giftBox.getPrice();

        // 5. In kết quả của sản phẩm và hai hộp.
        System.out.println("=== SINGLE PRODUCT ===");
        System.out.printf(Locale.US, "Phone price: %,.0f VND%n", phonePrice);

        System.out.println();
        System.out.println("=== ACCESSORY BOX ===");
        System.out.printf(Locale.US, "Accessory Box price: %,.0f VND%n", accessoryBoxPrice);

        System.out.println();
        System.out.println("=== GIFT BOX ===");
        System.out.printf(Locale.US, "Gift Box price: %,.0f VND%n", giftBoxPrice);
    }
}
