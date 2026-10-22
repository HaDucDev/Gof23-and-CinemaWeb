package structural.bridge.solution.device;

// Đại diện thiết bị TV giả lập: giữ âm lượng hiện tại và thực hiện yêu cầu từ Remote.
public class TvDevice implements Device {
    // Âm lượng hiện tại của thiết bị, từ 0 đến 100; 0 nghĩa là tắt tiếng trong demo.
    private int volume = 30;

    @Override
    public void power() {
        System.out.println("TV ON");
    }

    @Override
    public void mute() {
        volume = 0;
        System.out.println("TV MUTE (volume = 0)");
    }

    @Override
    public int getVolume() {
        return volume;
    }

    @Override
    public void setVolume(int volume) {
        // Giữ âm lượng trong khoảng 0 đến 100.
        this.volume = Math.max(0, Math.min(100, volume));
        System.out.println("TV VOLUME: " + this.volume);
    }
}
