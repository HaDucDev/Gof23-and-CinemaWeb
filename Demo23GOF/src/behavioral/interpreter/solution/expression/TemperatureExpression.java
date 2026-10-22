package behavioral.interpreter.solution.expression;

import behavioral.interpreter.model.OrderResult;
import behavioral.interpreter.solution.context.Context;

/** Biểu thức đơn cho nhiệt độ; quy tắc xung đột với đá được kiểm tra ở OrderExpression. */
public final class TemperatureExpression implements Expression {
    private final String code; // Mã nhiệt độ, hiện hỗ trợ HOT -> Nóng.

    public TemperatureExpression(String code) {
        this.code = code;
    }

    @Override
    public void interpret(Context context, OrderResult result) {
        String value = context.getTemperatures().get(code);
        if (value == null) {
            throw new IllegalArgumentException("Mã nhiệt độ không hợp lệ: " + code);
        }
        if (result.getTemperature() != null) {
            throw new IllegalArgumentException(
                    "Trùng tùy chọn nhiệt độ: chỉ được chọn một giá trị trong nhóm");
        }
        result.setTemperature(value);
        System.out.println("[TemperatureExpression] " + code + " -> " + value);
    }
}
