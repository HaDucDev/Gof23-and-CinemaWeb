package creational.abstractfactory.problem.product.dark;

import creational.abstractfactory.problem.product.Checkbox;

// Đại diện ô chọn thuộc theme tối; render() mô phỏng hiển thị bằng console.
public class DarkCheckbox implements Checkbox {
    @Override
    public void render() {
        System.out.println("Rendering Dark Checkbox");
    }
}
