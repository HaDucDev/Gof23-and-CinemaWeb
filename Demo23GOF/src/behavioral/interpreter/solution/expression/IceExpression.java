package behavioral.interpreter.solution.expression;

import behavioral.interpreter.model.OrderResult;
import behavioral.interpreter.solution.context.Context;

/** Biểu thức đơn cho lượng đá: tra bảng mã đá và từ chối tùy chọn đá bị lặp. */
public final class IceExpression implements Expression {
    private final String code; // Mã lượng đá cần diễn giải, ví dụ I0 hoặc I100.

    public IceExpression(String code) {
        this.code = code;
    }

    @Override
    public void interpret(Context context, OrderResult result) {
        String value = context.getIce().get(code);
        if (value == null) {
            throw new IllegalArgumentException("Lượng đá không hợp lệ: " + code);
        }
        if (result.getIce() != null) {
            throw new IllegalArgumentException(
                    "Trùng tùy chọn đá: chỉ được chọn một giá trị trong nhóm");
        }
        result.setIce(value);
        System.out.println("[IceExpression] " + code + " -> " + value);
    }
}
