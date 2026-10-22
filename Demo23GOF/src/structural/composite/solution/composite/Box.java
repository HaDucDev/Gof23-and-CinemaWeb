package structural.composite.solution.composite;

import java.util.ArrayList;
import java.util.List;
import structural.composite.solution.component.Item;

// Đại diện hộp quà có thể chứa sản phẩm và hộp con.
// Composite: vừa là Item, vừa giữ các Item con và cộng giá của chúng.
public class Box implements Item {
    private final String name; // Tên hộp quà hoặc hộp con.
    // Các phần tử trong hộp; mỗi Item có thể là Product hoặc Box con.
    private final List<Item> items = new ArrayList<>();

    public Box(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void add(Item item) {
        // Nhận được Product hoặc Box vì cả hai đều implement Item.
        items.add(item);
    }

    @Override
    public double getPrice() {
        double totalPrice = 0;

        for (Item item : items) {
            // Product trả giá riêng; Box con tự tính tổng giá bên trong nó.
            double itemPrice = item.getPrice();
            totalPrice = totalPrice + itemPrice;
        }

        return totalPrice;
    }
}
