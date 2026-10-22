package structural.bridge.problem;

// Bộ điều khiển Advanced cho TV: các nút Basic và thêm tắt tiếng.
// Problem gộp chức năng Remote và trạng thái thiết bị vào cùng class cho từng cặp.
public class AdvancedTvRemote {
    // Âm lượng hiện tại của thiết bị, từ 0 đến 100; 0 nghĩa là tắt tiếng trong demo.
    private int volume = 30;

    public void power() {
        System.out.println("TV ON");
    }

    public void volumeUp() {
        volume = Math.min(100, volume + 10);
        System.out.println("TV VOLUME: " + volume);
    }

    public void volumeDown() {
        volume = Math.max(0, volume - 10);
        System.out.println("TV VOLUME: " + volume);
    }

    public void mute() {
        volume = 0;
        System.out.println("TV MUTE (volume = 0)");
    }
}
