package behavioral.interpreter.solution.expression;

import behavioral.interpreter.model.OrderResult;
import behavioral.interpreter.solution.context.Context;

/** Biểu thức đơn cho topping: dịch mã topping và từ chối chỉ định lặp trong đơn. */
public final class ToppingExpression implements Expression {
    private final String code; // Mã topping, hiện hỗ trợ TC -> Trân châu.

    public ToppingExpression(String code) {
        this.code = code;
    }

    @Override
    public void interpret(Context context, OrderResult result) {
        String value = context.getToppings().get(code);
        if (value == null) {
            throw new IllegalArgumentException("Mã topping không hợp lệ: " + code);
        }
        if (result.getTopping() != null) {
            throw new IllegalArgumentException(
                    "Trùng tùy chọn topping: chỉ được chọn một giá trị trong nhóm");
        }
        result.setTopping(value);
        System.out.println("[ToppingExpression] " + code + " -> " + value);
    }
}
