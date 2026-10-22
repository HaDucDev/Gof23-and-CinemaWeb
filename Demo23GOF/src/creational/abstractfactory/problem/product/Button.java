package creational.abstractfactory.problem.product;

// Interface khai báo render() để hiển thị nút bấm, dùng chung cho theme sáng và tối.
public interface Button {
    // Mô phỏng hiển thị nút bấm theo theme của implementation.
    void render();
}
