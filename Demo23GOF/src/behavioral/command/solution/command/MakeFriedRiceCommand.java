package behavioral.command.solution.command;

import behavioral.command.solution.receiver.Kitchen;

// Concrete Command: đóng gói yêu cầu làm Cơm rang cho một bàn cụ thể.
public class MakeFriedRiceCommand implements Command {
    // Tham chiếu object bếp sẽ thực hiện yêu cầu khi execute() được gọi.
    private final Kitchen kitchen;
    // Tên bàn nhận món, ví dụ "Bàn 02"; chỉ lưu chuỗi tên bàn.
    private final String table;

    public MakeFriedRiceCommand(Kitchen kitchen, String table) {
        // Chỉ lưu thông tin; bếp làm món khi execute() được gọi.
        this.kitchen = kitchen;
        this.table = table;
    }

    @Override
    public void execute() {
        kitchen.makeFriedRice(table);
    }

    @Override
    public String getDescription() {
        return "Cơm rang - " + table;
    }
}
