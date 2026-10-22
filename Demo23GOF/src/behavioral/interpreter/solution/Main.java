package behavioral.interpreter.solution;

import behavioral.interpreter.DemoData;
import behavioral.interpreter.model.OrderResult;
import behavioral.interpreter.solution.context.Context;
import behavioral.interpreter.solution.expression.OrderExpression;
import behavioral.interpreter.solution.parser.OrderParser;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Điểm chạy Solution: Parser tạo các Expression, sau đó diễn giải chúng thành OrderResult. */
public final class Main {
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        run();
    }

    // Parser chỉ xây cấu trúc; các Expression tra Context và ghi ý nghĩa vào kết quả chung.
    public static OrderResult interpret(String ticket) {
        OrderExpression order = new OrderParser().parse(ticket);
        OrderResult result = new OrderResult();
        order.interpret(new Context(), result);
        return result;
    }

    public static void run() {
        System.out.println("===== SOLUTION: INTERPRETER PATTERN =====");
        for (String ticket : DemoData.ORDERS) {
            System.out.println("\n[INPUT] " + ticket);
            System.out.println("[RESULT] " + interpret(ticket));
        }

        // StrawExpression đã có trong source; Main chỉ đưa phiếu mẫu qua luồng diễn giải hiện có.
        System.out.println("\n===== DIỄN GIẢI PHIẾU CÓ TÙY CHỌN ỐNG HÚT =====");
        System.out.println("[MÃ ỐNG HÚT] ST0: không lấy ống hút; ST1: có ống hút.");
        System.out.println("[INPUT] " + DemoData.STRAW_ORDER);
        System.out.println("[RESULT] " + interpret(DemoData.STRAW_ORDER));
    }
}
