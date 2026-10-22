package structural.composite.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

// Điểm chạy hộp quà: Client tự phân biệt sản phẩm và hộp con để tính tổng giá.
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

        // 4. Product có getPrice(); Box phải được Client tính bằng calculatePrice().
        double phonePrice = phone.getPrice();
        double accessoryBoxPrice = calculatePrice(accessoryBox);
        double giftBoxPrice = calculatePrice(giftBox);

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

    // Client tự duyệt hộp vì Product và Box chưa có giao diện tính giá chung.
    private static double calculatePrice(Box box) {
        double total = 0;

        for (Object item : box.getItems()) {
            if (item instanceof Product product) {
                total += product.getPrice();
            } else if (item instanceof Box childBox) {
                // Gặp hộp con: Client lại tự tính giá các phần tử bên trong.
                total += calculatePrice(childBox);
            }
        }

        return total;
    }
}
