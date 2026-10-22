package behavioral.interpreter.solution.expression;

import behavioral.interpreter.model.OrderResult;
import behavioral.interpreter.solution.context.Context;
import java.util.List;

/** Nonterminal: kết hợp các biểu thức con và kiểm tra quy tắc toàn đơn. */
public final class OrderExpression implements Expression {
    // Danh sách object biểu thức cho đồ uống, kích cỡ và tùy chọn, theo thứ tự trên phiếu.
    private final List<Expression> children;

    public OrderExpression(List<Expression> children) {
        this.children = List.copyOf(children);
    }

    @Override
    public void interpret(Context context, OrderResult result) {
        System.out.println("[INTERPRETER] Bắt đầu diễn giải đơn hàng");
        // Mỗi con diễn giải phần của mình, cùng ghi vào một kết quả đơn hàng.
        for (Expression child : children) {
            child.interpret(context, result);
        }
        // Sau khi diễn giải các con mới kiểm tra điều kiện bắt buộc và xung đột giữa nhóm.
        if (result.getDrinkName() == null || result.getSize() == null) {
            throw new IllegalArgumentException("Đơn hàng phải có món và size");
        }
        if (result.getTemperature() != null && result.getIce() != null) {
            throw new IllegalArgumentException(
                    "Không thể chọn đồ uống nóng (HOT) cùng tùy chọn đá");
        }
        System.out.println(
                "[OrderExpression] Đã diễn giải các thành phần và kiểm tra quy tắc toàn phiếu.");
    }
}
