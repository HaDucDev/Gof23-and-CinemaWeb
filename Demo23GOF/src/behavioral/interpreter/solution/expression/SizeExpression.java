package behavioral.interpreter.solution.expression;

import behavioral.interpreter.model.OrderResult;
import behavioral.interpreter.solution.context.Context;

/** Biểu thức đơn cho kích cỡ: chấp nhận S/M/L rồi ghi size vào kết quả đơn hàng. */
public final class SizeExpression implements Expression {
    private final String code; // Mã kích cỡ ở vị trí thứ hai của phiếu.

    public SizeExpression(String code) {
        this.code = code;
    }

    @Override
    public void interpret(Context context, OrderResult result) {
        if (!context.getSizes().contains(code)) {
            throw new IllegalArgumentException("Size không hợp lệ hoặc sai vị trí: " + code);
        }
        if (result.getSize() != null) {
            throw new IllegalArgumentException("Trùng size trong biểu thức đơn hàng");
        }
        result.setSize(code);
        System.out.println("[SizeExpression] " + code + " -> Size " + code);
    }
}
