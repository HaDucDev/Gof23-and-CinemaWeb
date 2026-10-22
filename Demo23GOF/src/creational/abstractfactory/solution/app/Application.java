package creational.abstractfactory.solution.app;

import creational.abstractfactory.solution.factory.UIFactory;
import creational.abstractfactory.solution.product.Button;
import creational.abstractfactory.solution.product.Checkbox;
import creational.abstractfactory.solution.product.Dialog;

// Đại diện ứng dụng có nút bấm, ô chọn và hộp thoại được tạo từ cùng một Factory.
public class Application {
    private final Button button; // Tham chiếu nút bấm do Factory tạo theo theme đã chọn.
    // Tham chiếu ô chọn do cùng Factory tạo để đồng bộ theme.
    private final Checkbox checkbox;
    // Tham chiếu hộp thoại do cùng Factory tạo để đồng bộ theme.
    private final Dialog dialog;

    // factory cung cấp cả bộ component cùng theme.
    public Application(UIFactory factory) {
        this.button = factory.createButton();
        this.checkbox = factory.createCheckbox();
        this.dialog = factory.createDialog();
    }

    public void render() {
        button.render();
        checkbox.render();
        dialog.render();
    }
}
