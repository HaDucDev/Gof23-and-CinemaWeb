package structural.facade.solution.model;

// Đại diện đơn đặt món giao đến phòng: món ăn, số lượng, giá và trạng thái xử lý.
public class Order {
    private final String id; // Mã định danh của đơn đặt món.
    private final String dishName; // Tên món ăn được đặt.
    private final int quantity; // Số phần món ăn khách đặt.
    private final int roomNumber; // Số phòng nhận món.
    private final long unitPrice; // Giá một phần món ăn, tính bằng đồng.
    // Trạng thái xử lý hiện tại của đơn, ban đầu là CREATED.
    private OrderStatus status = OrderStatus.CREATED;

    public Order(String id, String dishName, int quantity, int roomNumber, long unitPrice) {
        if (quantity <= 0 || roomNumber <= 0) {
            throw new IllegalArgumentException("Số lượng và số phòng phải lớn hơn 0.");
        }

        this.id = id;
        this.dishName = dishName;
        this.quantity = quantity;
        this.roomNumber = roomNumber;
        this.unitPrice = unitPrice;
    }

    public String getId() {
        return id;
    }

    public String getDishName() {
        return dishName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public long getTotalPrice() {
        return unitPrice * quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Kết quả: "
                + id
                + " | "
                + quantity
                + " x "
                + dishName
                + " | Phòng "
                + roomNumber
                + " | Tổng tiền: "
                + getTotalPrice()
                + " đồng"
                + " | Trạng thái: "
                + status;
    }
}
