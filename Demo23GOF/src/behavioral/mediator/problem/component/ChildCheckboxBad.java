package behavioral.mediator.problem.component;

// Checkbox tự quyết định hiện/ẩn ô số con và yêu cầu nút Lưu kiểm tra.
public class ChildCheckboxBad {
    // Ô số con mà checkbox tự gọi để hiện/ẩn.
    private final ChildCountFieldBad childCountField;
    // Nút Lưu mà checkbox yêu cầu tự kiểm tra form.
    private final SaveButtonBad saveButton;
    private boolean checked; // true khi người dùng tick Có con; mặc định chưa tick.

    public ChildCheckboxBad(ChildCountFieldBad childCountField, SaveButtonBad saveButton) {
        this.childCountField = childCountField;
        this.saveButton = saveButton;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        System.out.println("  ChildCheckbox -> gọi trực tiếp ô số con và nút Lưu.");
        if (checked) {
            childCountField.show();
        } else {
            childCountField.hide();
        }
        saveButton.validate();
    }

    public boolean isChecked() {
        return checked;
    }
}
