package creational.abstractfactory.solution.factory;

import creational.abstractfactory.solution.product.Button;
import creational.abstractfactory.solution.product.Checkbox;
import creational.abstractfactory.solution.product.Dialog;
import creational.abstractfactory.solution.product.dark.DarkButton;
import creational.abstractfactory.solution.product.dark.DarkCheckbox;
import creational.abstractfactory.solution.product.dark.DarkDialog;

// Factory cụ thể tạo cả bộ thành phần giao diện thuộc theme tối.
public class DarkUIFactory implements UIFactory {
    @Override
    public Button createButton() {
        return new DarkButton();
    }

    @Override
    public Checkbox createCheckbox() {
        return new DarkCheckbox();
    }

    @Override
    public Dialog createDialog() {
        return new DarkDialog();
    }
}
