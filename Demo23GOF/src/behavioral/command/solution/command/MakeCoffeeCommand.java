package behavioral.command.solution.command;

import behavioral.command.solution.receiver.Kitchen;

// Concrete Command: đóng gói yêu cầu pha Cà phê cho một bàn cụ thể.
public class MakeCoffeeCommand implements Command {
    // Tham chiếu object bếp sẽ thực hiện yêu cầu khi execute() được gọi.
    private final Kitchen kitchen;
    // Tên bàn nhận đồ uống, ví dụ "Bàn 03"; chỉ lưu chuỗi tên bàn.
    private final String table;

    public MakeCoffeeCommand(Kitchen kitchen, String table) {
        // Chỉ lưu thông tin; bếp pha cà phê khi execute() được gọi.
        this.kitchen = kitchen;
        this.table = table;
    }

    @Override
    public void execute() {
        kitchen.makeCoffee(table);
    }

    @Override
    public String getDescription() {
        return "Cà phê - " + table;
    }
}
