package creational.abstractfactory.problem.product.dark;

import creational.abstractfactory.problem.product.Dialog;

// Đại diện hộp thoại thuộc theme tối; render() mô phỏng hiển thị bằng console.
public class DarkDialog implements Dialog {
    @Override
    public void render() {
        System.out.println("Rendering Dark Dialog");
    }
}
