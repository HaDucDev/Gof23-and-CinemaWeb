package structural.composite.solution.leaf;

import structural.composite.solution.component.Item;

// Đại diện sản phẩm riêng lẻ có tên và giá bán.
// Leaf của Composite: trả giá riêng qua getPrice(), không chứa Item con.
public class Product implements Item {
    private final String name; // Tên sản phẩm riêng lẻ.
    private final double price; // Giá riêng của sản phẩm, tính bằng VND.

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    @Override
    public double getPrice() {
        return price;
    }
}
