package structural.bridge.problem;

// Bộ điều khiển Premium cho TV: các nút Advanced và thêm lưu/khôi phục âm lượng yêu thích.
// Problem gộp chức năng Remote và trạng thái thiết bị vào cùng class cho từng cặp.
public class PremiumTvRemote {
    // Âm lượng hiện tại của thiết bị, từ 0 đến 100; 0 nghĩa là tắt tiếng trong demo.
    private int volume = 30;
    // Mức âm lượng yêu thích đã lưu, độc lập với âm lượng hiện tại.
    private int savedVolume = volume;

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

    public void saveVolume() {
        // Ghi nhớ một giá trị riêng, không bị đổi theo volume hiện tại.
        savedVolume = volume;
        System.out.println("Saved favourite volume: " + savedVolume);
    }

    public void restoreVolume() {
        System.out.println("Restoring favourite volume: " + savedVolume);
        volume = savedVolume;
        System.out.println("TV VOLUME: " + volume);
    }
}
