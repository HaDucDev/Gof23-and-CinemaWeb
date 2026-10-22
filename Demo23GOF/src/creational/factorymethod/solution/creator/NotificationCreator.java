package creational.factorymethod.solution.creator;

import creational.factorymethod.solution.notification.Notification;

// Lớp cha của các bên tạo thông báo: giữ luồng tạo rồi gửi, để lớp con chọn loại object.
// Lớp trừu tượng không được tạo trực tiếp; object được tạo từ Creator cụ thể.
public abstract class NotificationCreator {
    // Factory Method: subclass quyết định loại Notification được tạo.
    public abstract Notification createNotification();

    // Luồng xử lý chung: tạo thông báo qua factory method, sau đó gửi.
    public void sendNotification(String message) {
        Notification notification = createNotification();
        notification.send(message);
    }
}

//    Từ Java 8, đoạn này có thể thay bằng interface. Dùng default để giữ phần xử lý chung
// public interface NotificationCreator {
//
//    // Class thực thi quyết định loại thông báo được tạo.
//    Notification createNotification();
//
//    // default cho phép interface có phương thức với phần xử lý.
//    default void sendNotification(String message) {
//        Notification notification = createNotification();
//        notification.send(message);
//    }
// }
