package creational.abstractfactory.solution.product.light;

import creational.abstractfactory.solution.product.Dialog;

// Đại diện hộp thoại thuộc theme sáng; render() mô phỏng hiển thị bằng console.
public class LightDialog implements Dialog {
    @Override
    public void render() {
        System.out.println("Rendering Light Dialog");
    }
}
