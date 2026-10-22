package creational.builder.solution;

// Bộ ghi nhận cấu hình máy tính từng bước; build() kiểm tra và tạo object Computer.
public class ComputerBuilder {
    private String cpu; // Loại bộ xử lý của cấu hình máy, ví dụ Intel i7.
    private int ram; // Dung lượng bộ nhớ RAM của cấu hình, tính bằng GB.
    private String ssd; // Thông tin dung lượng ổ SSD, ví dụ 512GB hoặc 1TB.
    private String gpu; // Loại card đồ họa tùy chọn; null nghĩa là chưa chọn GPU.
    private boolean wifi; // Cấu hình có trang bị Wi-Fi hay không.
    private boolean bluetooth; // Cấu hình có hỗ trợ Bluetooth hay không.
    private boolean rgb; // Cấu hình có đèn RGB hay không.

    // Trả về chính builder để nối các bước cấu hình.
    public ComputerBuilder cpu(String cpu) {
        this.cpu = cpu;
        return this;
    }

    public ComputerBuilder ram(int ram) {
        this.ram = ram;
        return this;
    }

    public ComputerBuilder ssd(String ssd) {
        this.ssd = ssd;
        return this;
    }

    public ComputerBuilder gpu(String gpu) {
        this.gpu = gpu;
        return this;
    }

    public ComputerBuilder wifi(boolean wifi) {
        this.wifi = wifi;
        return this;
    }

    public ComputerBuilder bluetooth(boolean bluetooth) {
        this.bluetooth = bluetooth;
        return this;
    }

    public ComputerBuilder rgb(boolean rgb) {
        this.rgb = rgb;
        return this;
    }

    public Computer build() {
        // Kiểm tra thông tin bắt buộc trước khi tạo Computer.
        if (cpu == null || cpu.trim().isEmpty()) {
            throw new IllegalStateException("CPU is required");
        }
        if (ram <= 0) {
            throw new IllegalStateException("RAM must be greater than 0");
        }
        if (ssd == null || ssd.trim().isEmpty()) {
            throw new IllegalStateException("SSD is required");
        }

        return new Computer(cpu, ram, ssd, gpu, wifi, bluetooth, rgb);
    }
}
