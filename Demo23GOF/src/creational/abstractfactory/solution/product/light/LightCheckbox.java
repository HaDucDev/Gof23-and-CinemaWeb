package creational.abstractfactory.solution.product.light;

import creational.abstractfactory.solution.product.Checkbox;

// Đại diện ô chọn thuộc theme sáng; render() mô phỏng hiển thị bằng console.
public class LightCheckbox implements Checkbox {
    @Override
    public void render() {
        System.out.println("Rendering Light Checkbox");
    }
}
