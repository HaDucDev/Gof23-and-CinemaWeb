package structural.bridge.problem;

// Bộ điều khiển Basic cho máy chiếu: bật máy và tăng/giảm âm lượng.
// Problem gộp chức năng Remote và trạng thái thiết bị vào cùng class cho từng cặp.
public class BasicProjectorRemote {
    // Âm lượng hiện tại của thiết bị, từ 0 đến 100; 0 nghĩa là tắt tiếng trong demo.
    private int volume = 30;

    public void power() {
        System.out.println("Projector ON");
    }

    public void volumeUp() {
        volume = Math.min(100, volume + 10);
        System.out.println("Projector VOLUME: " + volume);
    }

    public void volumeDown() {
        volume = Math.max(0, volume - 10);
        System.out.println("Projector VOLUME: " + volume);
    }
}
