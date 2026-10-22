package behavioral.interpreter.solution.expression;

import behavioral.interpreter.model.OrderResult;
import behavioral.interpreter.solution.context.Context;

/** Biểu thức đơn cho lượng đường: dịch mã và kiểm tra không chọn đường nhiều lần. */
public final class SugarExpression implements Expression {
    private final String code; // Mã lượng đường cần diễn giải, ví dụ D0 hoặc D30.

    public SugarExpression(String code) {
        this.code = code;
    }

    @Override
    public void interpret(Context context, OrderResult result) {
        String value = context.getSugars().get(code);
        if (value == null) {
            throw new IllegalArgumentException("Lượng đường không hợp lệ: " + code);
        }
        if (result.getSugar() != null) {
            throw new IllegalArgumentException(
                    "Trùng tùy chọn đường: chỉ được chọn một giá trị trong nhóm");
        }
        result.setSugar(value);
        System.out.println("[SugarExpression] " + code + " -> " + value);
    }
}
