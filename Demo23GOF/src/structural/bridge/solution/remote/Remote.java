package structural.bridge.solution.remote;

import structural.bridge.solution.device.Device;

// Lớp cha chung của các bộ điều khiển, giữ tham chiếu tới thiết bị được chọn.
// Lớp trừu tượng không được tạo trực tiếp; constructor khởi tạo phần kế thừa của object lớp con.
public abstract class Remote {
    // Device là kiểu của biến tham chiếu, không phải lệnh tạo object.
    // Bridge: giữ tham chiếu tới thiết bị cụ thể đã được tạo và truyền vào.
    // Tham chiếu tới object thiết bị được truyền vào, gọi các phương thức của interface Device.
    protected final Device device;

    protected Remote(Device device) {
        // Constructor cha khởi tạo phần được kế thừa trên object lớp con đang được tạo.
        // Gán tham chiếu thiết bị có sẵn vào field; không tạo thêm object.
        this.device = device;
    }

    public abstract void power();
}
