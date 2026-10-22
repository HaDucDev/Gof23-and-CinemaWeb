package com.td.CinemaMini.service.notification;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Thực hiện việc gửi cập nhật sau khi transaction lưu thành công để tránh phát dữ liệu bị hủy. */
public final class AfterCommit {
    private static final Logger log = LoggerFactory.getLogger(AfterCommit.class);

    private AfterCommit() {}

    public static void run(final Runnable action) {
        // Nếu không có transaction để chờ hoàn tất, thực hiện ngay thao tác đã truyền vào.
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            safely(action);
            return;
        }
        // Đăng ký tác vụ sau commit; transaction rollback sẽ không chạy nhánh afterCommit này.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        safely(action);
                    }
                });
    }

    private static void safely(Runnable action) {
        try {
            // Thực hiện tác vụ gửi; lỗi được ghi log và không làm mất kết quả nghiệp vụ đã lưu.
            action.run();
        } catch (RuntimeException error) {
            log.error("Không gửi được cập nhật sau commit", error);
        }
    }
}
