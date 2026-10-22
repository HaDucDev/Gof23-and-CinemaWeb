package structural.composite.solution.component;

// Interface khai báo getPrice() để lấy giá sản phẩm hoặc tổng giá hộp chứa các phần tử (Component).
public interface Item {
    // Lấy giá sản phẩm hoặc tổng giá hộp qua cùng một cách gọi.
    double getPrice();
}
