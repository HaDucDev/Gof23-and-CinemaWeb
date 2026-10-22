package behavioral.mediator.solution.component;

import behavioral.mediator.solution.mediator.FormMediator;

// Checkbox chỉ giữ trạng thái tick và báo Mediator; không biết ô số con hay nút Lưu.
public class ChildCheckbox {
    // Nơi nhận sự kiện tick/bỏ tick và quyết định phản ứng của form.
    private final FormMediator mediator;
    private boolean checked; // Trạng thái Có con của chính checkbox này.

    public ChildCheckbox(FormMediator mediator) {
        this.mediator = mediator;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        System.out.println("  ChildCheckbox -> mediator.onChildrenChanged(" + checked + ")");
        mediator.onChildrenChanged(checked);
    }

    public boolean isChecked() {
        return checked;
    }
}
