package creational.builder.problem;

// Đại diện cấu hình máy tính: CPU, RAM, ổ lưu trữ và các tùy chọn phần cứng.
public class Computer {
    private String cpu; // Loại bộ xử lý của cấu hình máy, ví dụ Intel i7.
    private int ram; // Dung lượng bộ nhớ RAM của cấu hình, tính bằng GB.
    private String ssd; // Thông tin dung lượng ổ SSD, ví dụ 512GB hoặc 1TB.
    private String gpu; // Loại card đồ họa tùy chọn; null nghĩa là chưa chọn GPU.
    private boolean wifi; // Cấu hình có trang bị Wi-Fi hay không.
    private boolean bluetooth; // Cấu hình có hỗ trợ Bluetooth hay không.
    private boolean rgb; // Cấu hình có đèn RGB hay không.

    public Computer() {
    }

    public Computer(
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

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public void setSsd(String ssd) {
        this.ssd = ssd;
    }

    public void setGpu(String gpu) {
        this.gpu = gpu;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public void setBluetooth(boolean bluetooth) {
        this.bluetooth = bluetooth;
    }

    public void setRgb(boolean rgb) {
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
