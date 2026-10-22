package com.td.CinemaMini.service.showtime;

import com.td.CinemaMini.model.*;
import java.util.*;

/**
 * Mẫu suất chiếu đọc từ config/showtime-templates.json, gồm tên mẫu, giá và danh sách ghế.
 *
 * <p>GoF: Creational / Prototype. copy() tạo bản sao để thay giá và ghế cho một suất mới.
 */
public class ShowtimeTemplate {
    /** Tên mẫu hiển thị trên form admin, ví dụ "Suất tiêu chuẩn"; tên phim được chọn riêng. */
    private String name;

    /** Giá cơ bản và phụ thu VIP mặc định, dùng điền sẵn hai ô giá trên form tạo suất. */
    private PricingConfiguration pricing;

    /** Danh sách mã ghế và cờ mở bán; service đặt lại cờ theo các ghế admin chọn không bán. */
    private List<SeatConfiguration> seats = new ArrayList<SeatConfiguration>();

    public String getName() {
        return name;
    }

    public void setName(String v) {
        name = v;
    }

    public PricingConfiguration getPricing() {
        return pricing;
    }

    public void setPricing(PricingConfiguration v) {
        pricing = v;
    }

    public List<SeatConfiguration> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatConfiguration> v) {
        seats = v;
    }

    /** Sao chép giá, danh sách và từng ghế để các thay đổi chỉ áp dụng cho bản sao. */
    public ShowtimeTemplate copy() {
        ShowtimeTemplate copy = new ShowtimeTemplate();
        // Tên mẫu là String nên có thể dùng lại; cấu hình giá và từng ghế cần bản sao riêng.
        copy.name = name;
        copy.pricing = pricing.copy();
        // Sao chép từng ghế để việc đóng/mở bán trong lần tạo suất này không sửa mẫu gốc.
        for (SeatConfiguration seat : seats) {
            copy.seats.add(seat.copy());
        }
        return copy;
    }
}
