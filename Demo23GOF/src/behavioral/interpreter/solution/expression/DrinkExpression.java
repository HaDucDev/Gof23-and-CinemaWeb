package behavioral.interpreter.solution.expression;

import behavioral.interpreter.model.OrderResult;
import behavioral.interpreter.solution.context.Context;

/** Biểu thức đơn cho mã đồ uống: tra tên món trong Context và từ chối món bị lặp. */
public final class DrinkExpression implements Expression {
    private final String code; // Mã món Parser lấy ở đầu phiếu, ví dụ TS hoặc CP.

    public DrinkExpression(String code) {
        this.code = code;
    }

    @Override
    public void interpret(Context context, OrderResult result) {
        String value = context.getDrinks().get(code);
        if (value == null) {
            throw new IllegalArgumentException("Mã đồ uống không hợp lệ: " + code);
        }
        if (result.getDrinkName() != null) {
            throw new IllegalArgumentException("Trùng món trong biểu thức đơn hàng");
        }
        result.setDrinkName(value);
        System.out.println("[DrinkExpression] " + code + " -> " + value);
    }
}
