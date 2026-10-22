package behavioral.interpreter.solution.parser;

import behavioral.interpreter.solution.expression.DrinkExpression;
import behavioral.interpreter.solution.expression.Expression;
import behavioral.interpreter.solution.expression.IceExpression;
import behavioral.interpreter.solution.expression.OrderExpression;
import behavioral.interpreter.solution.expression.SizeExpression;
import behavioral.interpreter.solution.expression.StrawExpression;
import behavioral.interpreter.solution.expression.SugarExpression;
import behavioral.interpreter.solution.expression.TemperatureExpression;
import behavioral.interpreter.solution.expression.ToppingExpression;
import java.util.ArrayList;
import java.util.List;

/** Xây cấu trúc biểu thức; không dịch mã thành tên đồ uống hoặc tùy chọn. */
public final class OrderParser {
    // Chuyển chuỗi Món-Size[-Tùy chọn...] thành biểu thức gốc chứa các biểu thức đơn.
    public OrderExpression parse(String ticket) {
        System.out.println("[PARSER] Phân tích cấu trúc phiếu gọi món");
        if (ticket == null || ticket.isBlank()) {
            throw new IllegalArgumentException("Phiếu không được để trống");
        }
        // Mảng mã trên phiếu: mã đồ uống, mã kích cỡ, rồi các mã tùy chọn.
        String[] tokens = ticket.split("-", -1);
        if (tokens.length < 2) {
            throw new IllegalArgumentException(
                    "Phiếu phải có món và size theo cấu trúc Món-Size[-Tùy chọn...]");
        }
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].isEmpty()) {
                throw new IllegalArgumentException(
                        "Phiếu chứa thành phần rỗng ở vị trí " + (i + 1));
            }
        }

        // Vị trí quyết định món/size; tiền tố tùy chọn quyết định loại Expression còn lại.
        // Danh sách object biểu thức cho đồ uống, kích cỡ và từng tùy chọn.
        List<Expression> children = new ArrayList<>();
        children.add(created(new DrinkExpression(tokens[0]), "DrinkExpression"));
        children.add(created(new SizeExpression(tokens[1]), "SizeExpression"));
        for (int i = 2; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.startsWith("D")) {
                children.add(created(new SugarExpression(token), "SugarExpression"));
            } else if (token.startsWith("I")) {
                children.add(created(new IceExpression(token), "IceExpression"));
            } else if (token.equals("HOT")) {
                children.add(created(new TemperatureExpression(token), "TemperatureExpression"));
            } else if (token.equals("TC")) {
                children.add(created(new ToppingExpression(token), "ToppingExpression"));
            } else if (token.startsWith("ST")) {
                // Tích hợp nhóm ống hút mới; ý nghĩa ST0/ST1 thuộc StrawExpression.
                children.add(created(new StrawExpression(token), "StrawExpression"));
            } else {
                throw new IllegalArgumentException("Mã tùy chọn không hợp lệ: " + token);
            }
        }
        return new OrderExpression(children);
    }

    // expression là object vừa tạo; name là tên class biểu thức để in log, ví dụ DrinkExpression.
    private Expression created(Expression expression, String name) {
        System.out.println("[PARSER] Tạo " + name);
        return expression;
    }
}
