package structural.adapter.problem2.printer;

// Mô phỏng API máy in mới: nhận văn bản hóa đơn và số bản in (Adaptee).
public class NewPrinter {
    public void printTicket(String text, int copies) {
        if (copies <= 0) {
            throw new IllegalArgumentException("Số bản in phải lớn hơn 0.");
        }

        for (int copy = 1; copy <= copies; copy++) {
            System.out.println("[Máy in mới] Bản " + copy + "/" + copies);
            System.out.println(text);
        }
    }
}
