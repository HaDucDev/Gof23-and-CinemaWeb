package behavioral.command.solution.receiver;

// Receiver: bếp thực sự làm món; table là bàn sẽ nhận món, ví dụ "Bàn 01".
public class Kitchen {
    public void makePho(String table) {
        System.out.println("[BẾP] Đang nấu Phở bò cho " + table);
    }

    public void makeFriedRice(String table) {
        System.out.println("[BẾP] Đang làm Cơm rang cho " + table);
    }

    public void makeCoffee(String table) {
        System.out.println("[BẾP] Đang pha Cà phê cho " + table);
    }
}
