package creational.abstractfactory.solution.factory;

import creational.abstractfactory.solution.product.Button;
import creational.abstractfactory.solution.product.Checkbox;
import creational.abstractfactory.solution.product.Dialog;
import creational.abstractfactory.solution.product.light.LightButton;
import creational.abstractfactory.solution.product.light.LightCheckbox;
import creational.abstractfactory.solution.product.light.LightDialog;

// Factory cụ thể tạo cả bộ thành phần giao diện thuộc theme sáng.
public class LightUIFactory implements UIFactory {
    @Override
    public Button createButton() {
        return new LightButton();
    }

    @Override
    public Checkbox createCheckbox() {
        return new LightCheckbox();
    }

    @Override
    public Dialog createDialog() {
        return new LightDialog();
    }
}
