package creational.abstractfactory.problem.product.light;

import creational.abstractfactory.problem.product.Button;

// Đại diện nút bấm thuộc theme sáng; render() mô phỏng hiển thị bằng console.
public class LightButton implements Button {
    @Override
    public void render() {
        System.out.println("Rendering Light Button");
    }
}
