package structural.bridge.solution.device;

// Interface khai báo các phương thức bật máy, tắt tiếng và đọc/đặt âm lượng cho các loại thiết bị.
// Interface chỉ khai báo thao tác; object được tạo từ class cụ thể implement Device.
public interface Device {
    // Yêu cầu bật thiết bị; demo chỉ in ON.
    void power();

    // Yêu cầu tắt tiếng; demo đặt âm lượng hiện tại về 0.
    void mute();

    // Đọc âm lượng hiện tại để Remote tăng/giảm hoặc lưu mức yêu thích.
    int getVolume();

    // Đặt âm lượng Remote yêu cầu; thiết bị giới hạn giá trị từ 0 đến 100.
    void setVolume(int volume);
}
