package behavioral.interpreter.solution.expression;

import behavioral.interpreter.model.OrderResult;
import behavioral.interpreter.solution.context.Context;

/** Biểu thức đơn cho yêu cầu ống hút; minh họa thêm quy tắc mà giữ nguyên các Expression cũ. */
public final class StrawExpression implements Expression {
    private final String code; // ST0: không lấy ống hút; ST1: có ống hút.

    public StrawExpression(String code) {
        this.code = code;
    }

    @Override
    public void interpret(Context context, OrderResult result) {
        String value = context.getStraws().get(code);
        if (value == null) {
            throw new IllegalArgumentException("Mã ống hút không hợp lệ: " + code);
        }
        if (result.getStraw() != null) {
            throw new IllegalArgumentException(
                    "Trùng tùy chọn ống hút: chỉ được chọn một giá trị trong nhóm");
        }
        result.setStraw(value);
        System.out.println("[StrawExpression] " + code + " -> " + value);
    }
}
