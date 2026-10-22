package creational.abstractfactory.solution.product;

// Interface khai báo render() để hiển thị ô chọn, dùng chung cho theme sáng và tối.
public interface Checkbox {
    // Mô phỏng hiển thị ô chọn theo theme của implementation.
    void render();
}
