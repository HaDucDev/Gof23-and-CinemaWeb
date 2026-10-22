package creational.abstractfactory.problem.product;

// Interface khai báo render() để hiển thị hộp thoại, dùng chung cho theme sáng và tối.
public interface Dialog {
    // Mô phỏng hiển thị hộp thoại theo theme của implementation.
    void render();
}
