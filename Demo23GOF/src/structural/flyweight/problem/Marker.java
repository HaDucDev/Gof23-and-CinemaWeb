package structural.flyweight.problem;

// Đại diện một điểm đánh dấu trên bản đồ, có tên và tọa độ riêng.
// Giữ tham chiếu tới MarkerStyle để hiển thị biểu tượng, màu và nhóm địa điểm.
public class Marker {
    // Tổng số object Marker đã tạo trong lần chạy; static dùng chung cho class.
    private static int createdCount;

    // Dữ liệu riêng: mỗi Marker có vị trí và tên của nó.
    private final double latitude; // Vĩ độ riêng của điểm đánh dấu trên bản đồ.
    private final double longitude; // Kinh độ riêng của điểm đánh dấu trên bản đồ.
    private final String locationName; // Tên địa điểm riêng của Marker, ví dụ Phở Ngon.

    // Giữ tham chiếu tới Style đã được truyền vào, không tự tạo thêm Style.
    // Tham chiếu bộ hiển thị đã được truyền vào; field này không tự tạo Style.
    private final MarkerStyle markerStyle;

    public Marker(double latitude, double longitude, String locationName, MarkerStyle markerStyle) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationName = locationName;
        this.markerStyle = markerStyle;
        createdCount++;
    }

    public MarkerStyle getMarkerStyle() {
        return markerStyle;
    }

    public static int getCreatedCount() {
        return createdCount;
    }

    public void display() {
        System.out.println("Marker: " + locationName);
        System.out.println("Position: (" + latitude + ", " + longitude + ")");
        System.out.println(
                "Style: icon="
                        + markerStyle.getIcon()
                        + ", color="
                        + markerStyle.getColor()
                        + ", locationType="
                        + markerStyle.getLocationType());
        // Mã hash hỗ trợ quan sát; các phép == trong Main trực tiếp kiểm tra việc dùng chung
        // object.
        System.out.println(
                "Mã nhận diện Style (identityHashCode): " + System.identityHashCode(markerStyle));
        System.out.println();
    }
}
