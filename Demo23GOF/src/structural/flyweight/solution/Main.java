package structural.flyweight.solution;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import structural.flyweight.solution.factory.MarkerStyleFactory;
import structural.flyweight.solution.flyweight.MarkerStyle;
import structural.flyweight.solution.model.Marker;

// Điểm chạy Flyweight: dùng chung Factory để các Marker tái sử dụng object Style.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("=== WITH FLYWEIGHT ===");

        // Tạo Factory với cache (Map) rỗng; dòng new này chưa tạo MarkerStyle.
        // Dùng lại cùng Factory cho mọi Marker để tìm được các Style đã lưu.
        MarkerStyleFactory markerStyleFactory = new MarkerStyleFactory();

        // Lần lấy Style nhà hàng thứ nhất: cache chưa có bộ restaurant.png / RED / RESTAURANT.
        // getMarkerStyle() sẽ new MarkerStyle bên trong Factory, lưu vào cache rồi trả về.
        MarkerStyle phoStyle =
                markerStyleFactory.getMarkerStyle("restaurant.png", "RED", "RESTAURANT");
        Marker phoNgon = new Marker(10.01, 106.01, "Phở Ngon", phoStyle);

        // Lần lấy thứ hai: cùng bộ giá trị nên Factory trả Style đã tạo cho Phở Ngon.
        // Không new MarkerStyle; chỉ new Marker Bún Bò với tên và tọa độ riêng.
        MarkerStyle bunBoStyle =
                markerStyleFactory.getMarkerStyle("restaurant.png", "RED", "RESTAURANT");
        Marker bunBo = new Marker(10.02, 106.02, "Bún Bò", bunBoStyle);

        // Lần lấy thứ ba: tiếp tục nhận cùng Style nhà hàng đã nằm trong cache.
        // Ba biến phoStyle, bunBoStyle, comTamStyle cùng tham chiếu một object Style.
        MarkerStyle comTamStyle =
                markerStyleFactory.getMarkerStyle("restaurant.png", "RED", "RESTAURANT");
        Marker comTam = new Marker(10.03, 106.03, "Cơm Tấm", comTamStyle);

        System.out.println();
        phoNgon.display();
        bunBo.display();
        comTam.display();

        System.out.println("Number of Marker objects: " + Marker.getCreatedCount());
        System.out.println("Number of MarkerStyle objects: " + MarkerStyle.getCreatedCount());
        System.out.println(
                "Phở Ngon / Bún Bò share Style: "
                        + (phoNgon.getMarkerStyle() == bunBo.getMarkerStyle()));
        System.out.println(
                "Phở Ngon / Cơm Tấm share Style: "
                        + (phoNgon.getMarkerStyle() == comTam.getMarkerStyle()));

        System.out.println();
        System.out.println("=== ADD HOTEL AND GAS STATION ===");

        // Bộ hotel.png / BLUE / HOTEL chưa có: Factory tạo Style mới, khác Style nhà hàng.
        MarkerStyle sunriseStyle = markerStyleFactory.getMarkerStyle("hotel.png", "BLUE", "HOTEL");
        Marker sunriseHotel = new Marker(10.04, 106.04, "Khách sạn Sunrise", sunriseStyle);

        // Bộ Style khách sạn đã có từ Sunrise: trả lại object đó cho Lotus.
        MarkerStyle lotusStyle = markerStyleFactory.getMarkerStyle("hotel.png", "BLUE", "HOTEL");
        Marker lotusHotel = new Marker(10.05, 106.05, "Khách sạn Lotus", lotusStyle);

        // Bộ gas.png / GREEN / GAS_STATION chưa có: tạo thêm một Style trạm xăng.
        MarkerStyle gasStyle = markerStyleFactory.getMarkerStyle("gas.png", "GREEN", "GAS_STATION");
        Marker gasStation = new Marker(10.06, 106.06, "Trạm xăng Trung Tâm", gasStyle);

        System.out.println();
        sunriseHotel.display();
        lotusHotel.display();
        gasStation.display();

        System.out.println("Number of Marker objects: " + Marker.getCreatedCount());
        System.out.println("Number of MarkerStyle objects: " + MarkerStyle.getCreatedCount());
        System.out.println(
                "Sunrise / Lotus share Style: "
                        + (sunriseHotel.getMarkerStyle() == lotusHotel.getMarkerStyle()));
        System.out.println(
                "Restaurant / Hotel share Style: "
                        + (phoNgon.getMarkerStyle() == sunriseHotel.getMarkerStyle()));
    }
}
