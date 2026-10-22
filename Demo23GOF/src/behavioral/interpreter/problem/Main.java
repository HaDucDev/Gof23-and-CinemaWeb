package behavioral.interpreter.problem;

import behavioral.interpreter.DemoData;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Điểm chạy Problem: đưa từng phiếu vào DrinkOrderProcessorBad để xử lý tập trung. */
public final class Main {
    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        run();
    }

    public static void run() {
        System.out.println("===== PROBLEM: XỬ LÝ PHIẾU BẰNG IF-ELSE =====");
        DrinkOrderProcessorBad processor = new DrinkOrderProcessorBad();
        for (String ticket : DemoData.ORDERS) {
            System.out.println("\n[INPUT] " + ticket);
            System.out.println("[RESULT] " + processor.process(ticket));
        }

        // Quy tắc ống hút đã có trong processor; đây là lần chạy phiếu mẫu có tùy chọn đó.
        System.out.println("\n===== DIỄN GIẢI PHIẾU CÓ TÙY CHỌN ỐNG HÚT =====");
        System.out.println("[MÃ ỐNG HÚT] ST0: không lấy ống hút; ST1: có ống hút.");
        System.out.println("[INPUT] " + DemoData.STRAW_ORDER);
        System.out.println("[RESULT] " + processor.process(DemoData.STRAW_ORDER));
    }
}
