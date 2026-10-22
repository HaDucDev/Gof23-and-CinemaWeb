package structural.flyweight.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Điểm chạy Marker bản đồ: mỗi Marker nhận một Style mới dù dữ liệu Style giống nhau.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        System.out.println("=== WITHOUT FLYWEIGHT ===");

        // Cùng ba thuộc tính Style, nhưng mỗi lần new vẫn tạo object riêng.
        MarkerStyle phoStyle = new MarkerStyle("restaurant.png", "RED", "RESTAURANT");
        Marker phoNgon = new Marker(10.01, 106.01, "Phở Ngon", phoStyle);

        MarkerStyle bunBoStyle = new MarkerStyle("restaurant.png", "RED", "RESTAURANT");
        Marker bunBo = new Marker(10.02, 106.02, "Bún Bò", bunBoStyle);

        MarkerStyle comTamStyle = new MarkerStyle("restaurant.png", "RED", "RESTAURANT");
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

        MarkerStyle sunriseStyle = new MarkerStyle("hotel.png", "BLUE", "HOTEL");
        Marker sunriseHotel = new Marker(10.04, 106.04, "Khách sạn Sunrise", sunriseStyle);

        MarkerStyle lotusStyle = new MarkerStyle("hotel.png", "BLUE", "HOTEL");
        Marker lotusHotel = new Marker(10.05, 106.05, "Khách sạn Lotus", lotusStyle);

        MarkerStyle gasStyle = new MarkerStyle("gas.png", "GREEN", "GAS_STATION");
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
