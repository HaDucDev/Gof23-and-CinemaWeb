package structural.composite.problem;

// Đại diện sản phẩm riêng lẻ có tên và giá bán.
// Chưa có giao diện tính giá chung với Box; Client phải phân biệt loại.
public class Product {
    private final String name; // Tên sản phẩm riêng lẻ.
    private final double price; // Giá riêng của sản phẩm, tính bằng VND.

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }
}
