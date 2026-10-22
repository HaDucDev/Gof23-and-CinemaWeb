package behavioral.mediator.solution.component;

import behavioral.mediator.solution.mediator.FormMediator;

// Ô nhập giữ số con và trạng thái hiển thị; nhập số thì chỉ báo Mediator.
public class ChildCountField {
    private final FormMediator mediator; // Nhận thông báo khi người dùng thay đổi số con.
    private int value; // Giá trị số con đang giữ, vẫn còn khi ô bị ẩn.
    private boolean visible; // Trạng thái hiện/ẩn do Mediator quyết định.

    public ChildCountField(FormMediator mediator) {
        this.mediator = mediator;
    }

    public void setValue(int value) {
        this.value = value;
        System.out.println("  ChildCountField -> mediator.onFormChanged()");
        mediator.onFormChanged();
    }

    // Chỉ cập nhật hiển thị; Mediator sẽ tự kiểm tra nút Lưu trong cùng lần xử lý.
    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public int getValue() {
        return value;
    }

    public boolean isVisible() {
        return visible;
    }
}
