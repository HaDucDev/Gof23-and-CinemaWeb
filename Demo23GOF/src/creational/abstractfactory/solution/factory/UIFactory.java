package creational.abstractfactory.solution.factory;

import creational.abstractfactory.solution.product.Button;
import creational.abstractfactory.solution.product.Checkbox;
import creational.abstractfactory.solution.product.Dialog;

// Interface khai báo các phương thức tạo nút bấm, ô chọn và hộp thoại cùng theme (Abstract
// Factory).
public interface UIFactory {
    // Yêu cầu Factory cụ thể tạo nút bấm thuộc cùng theme.
    Button createButton();

    // Yêu cầu Factory cụ thể tạo ô chọn thuộc cùng theme.
    Checkbox createCheckbox();

    // Yêu cầu Factory cụ thể tạo hộp thoại thuộc cùng theme.
    Dialog createDialog();
}
