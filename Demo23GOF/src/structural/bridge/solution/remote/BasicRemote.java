package structural.bridge.solution.remote;

import structural.bridge.solution.device.Device;

// Bộ điều khiển Basic: bật máy và tăng/giảm âm lượng.
// Gửi yêu cầu qua Device để dùng cùng chức năng với các loại thiết bị.
public class BasicRemote extends Remote {
    public BasicRemote(Device device) {
        super(device);
    }

    @Override
    public void power() {
        device.power();
    }

    public void volumeUp() {
        int currentVolume = device.getVolume();
        int newVolume = currentVolume + 10;
        device.setVolume(newVolume);
    }

    public void volumeDown() {
        int currentVolume = device.getVolume();
        int newVolume = currentVolume - 10;
        device.setVolume(newVolume);
    }
}
