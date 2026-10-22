package behavioral.command.solution.command;

import behavioral.command.solution.receiver.Kitchen;

// Concrete Command: đóng gói yêu cầu làm Phở bò cho một bàn cụ thể.
public class MakePhoCommand implements Command {
    // Tham chiếu object bếp sẽ thực hiện yêu cầu khi execute() được gọi.
    private final Kitchen kitchen;
    // Tên bàn nhận món, ví dụ "Bàn 01"; chỉ lưu chuỗi tên bàn.
    private final String table;

    public MakePhoCommand(Kitchen kitchen, String table) {
        // Chỉ ghi nhớ bếp và bàn; tạo Command chưa làm bếp nấu.
        this.kitchen = kitchen;
        this.table = table;
    }

    @Override
    public void execute() {
        kitchen.makePho(table);
    }

    @Override
    public String getDescription() {
        return "Phở bò - " + table;
    }
}
