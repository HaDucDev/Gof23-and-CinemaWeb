package structural.bridge.solution.remote;

import structural.bridge.solution.device.Device;

// Bộ điều khiển Premium: các nút Advanced và thêm lưu/khôi phục âm lượng yêu thích.
// Kế thừa các nút AdvancedRemote; phương thức mới là saveVolume() và restoreVolume().
public class PremiumRemote extends AdvancedRemote {
    // Mức âm lượng yêu thích đã lưu, độc lập với âm lượng hiện tại.
    private int savedVolume;

    public PremiumRemote(Device device) {
        super(device);
        // Mức lưu ban đầu là âm lượng thiết bị khi kết nối Remote.
        savedVolume = device.getVolume();
    }

    public void saveVolume() {
        savedVolume = device.getVolume();
        System.out.println("Saved favourite volume: " + savedVolume);
    }

    public void restoreVolume() {
        System.out.println("Restoring favourite volume: " + savedVolume);
        device.setVolume(savedVolume);
    }
}
