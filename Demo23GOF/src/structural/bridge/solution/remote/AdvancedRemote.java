package structural.bridge.solution.remote;

import structural.bridge.solution.device.Device;

// Bộ điều khiển Advanced: các nút Basic và thêm tắt tiếng.
// Kế thừa các nút BasicRemote; phương thức mới ở đây là mute().
public class AdvancedRemote extends BasicRemote {
    public AdvancedRemote(Device device) {
        super(device);
    }

    public void mute() {
        device.mute();
    }
}
