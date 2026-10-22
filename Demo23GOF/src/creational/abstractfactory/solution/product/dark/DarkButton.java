package creational.abstractfactory.solution.product.dark;

import creational.abstractfactory.solution.product.Button;

// Đại diện nút bấm thuộc theme tối; render() mô phỏng hiển thị bằng console.
public class DarkButton implements Button {
    @Override
    public void render() {
        System.out.println("Rendering Dark Button");
    }
}
