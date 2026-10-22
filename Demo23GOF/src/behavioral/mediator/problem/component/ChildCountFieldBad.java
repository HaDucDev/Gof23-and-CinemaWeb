package behavioral.mediator.problem.component;

// Ô nhập giữ số con, trạng thái hiển thị và gọi thẳng nút Lưu khi nhập dữ liệu.
public class ChildCountFieldBad {
    // Nút Lưu được gọi trực tiếp sau mỗi lần nhập số con.
    private final SaveButtonBad saveButton;
    private int value; // Số con đã nhập; phải > 0 nếu checkbox Có con được tick.
    private boolean visible; // Ô số con đang hiện hay ẩn; không làm thay đổi value.

    public ChildCountFieldBad(SaveButtonBad saveButton) {
        this.saveButton = saveButton;
    }

    public void setValue(int value) {
        this.value = value;
        System.out.println("  ChildCountField -> gọi trực tiếp nút Lưu.");
        saveButton.validate();
    }

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
