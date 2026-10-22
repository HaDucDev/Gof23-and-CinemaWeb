package creational.builder.solution;

// Đại diện cấu hình máy tính hoàn chỉnh, được ComputerBuilder tạo sau khi kiểm tra dữ liệu.
public class Computer {
    private final String cpu; // Loại bộ xử lý của cấu hình máy, ví dụ Intel i7.
    private final int ram; // Dung lượng bộ nhớ RAM của cấu hình, tính bằng GB.
    private final String ssd; // Thông tin dung lượng ổ SSD, ví dụ 512GB hoặc 1TB.
    private final String gpu; // Loại card đồ họa tùy chọn; null nghĩa là chưa chọn GPU.
    private final boolean wifi; // Cấu hình có trang bị Wi-Fi hay không.
    private final boolean bluetooth; // Cấu hình có hỗ trợ Bluetooth hay không.
    private final boolean rgb; // Cấu hình có đèn RGB hay không.

    // Constructor không public; trong demo, ComputerBuilder gọi constructor này.
    Computer(
            String cpu,
            int ram,
            String ssd,
            String gpu,
            boolean wifi,
            boolean bluetooth,
            boolean rgb) {
        this.cpu = cpu;
        this.ram = ram;
        this.ssd = ssd;
        this.gpu = gpu;
        this.wifi = wifi;
        this.bluetooth = bluetooth;
        this.rgb = rgb;
    }

    public String toString() {
        return "CPU: "
                + cpu
                + "\nRAM: "
                + ram
                + " GB"
                + "\nSSD: "
                + ssd
                + "\nGPU: "
                + (gpu == null ? "None" : gpu)
                + "\nWi-Fi: "
                + wifi
                + "\nBluetooth: "
                + bluetooth
                + "\nRGB: "
                + rgb;
    }
}
