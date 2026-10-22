package behavioral.command.problem;

// Bếp thực sự làm món; giữ cùng nghiệp vụ với Kitchen trong bản solution.
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
