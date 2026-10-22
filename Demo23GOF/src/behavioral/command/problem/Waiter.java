package behavioral.command.problem;

// Nhận món rồi gọi bếp ngay; mỗi loại món có một phương thức xử lý riêng.
public class Waiter {
    private final Kitchen kitchen; // Phục vụ biết và gọi trực tiếp các thao tác của bếp.

    public Waiter(Kitchen kitchen) {
        this.kitchen = kitchen;
    }

    public void orderPho(String table) {
        System.out.println("[PHỤC VỤ] Đã nhận yêu cầu: Phở bò - " + table);
        System.out.println("[PHỤC VỤ] Gửi yêu cầu: Phở bò - " + table);
        kitchen.makePho(table); // Bếp xử lý ngay trong lần nhận yêu cầu này.
        System.out.println();
    }

    public void orderFriedRice(String table) {
        System.out.println("[PHỤC VỤ] Đã nhận yêu cầu: Cơm rang - " + table);
        System.out.println("[PHỤC VỤ] Gửi yêu cầu: Cơm rang - " + table);
        kitchen.makeFriedRice(table);
        System.out.println();
    }

    public void orderCoffee(String table) {
        System.out.println("[PHỤC VỤ] Đã nhận yêu cầu: Cà phê - " + table);
        System.out.println("[PHỤC VỤ] Gửi yêu cầu: Cà phê - " + table);
        kitchen.makeCoffee(table);
        System.out.println();
    }
}
