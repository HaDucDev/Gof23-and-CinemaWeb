package behavioral.interpreter.solution.expression;

import behavioral.interpreter.model.OrderResult;
import behavioral.interpreter.solution.context.Context;

/** Interface khai báo interpret(); mọi biểu thức đều đọc Context và ghi kết quả vào cùng OrderResult. */
public interface Expression {
    // Kiểm tra mã/quy tắc của biểu thức rồi ghi giá trị đã diễn giải vào result.
    void interpret(Context context, OrderResult result);
}
