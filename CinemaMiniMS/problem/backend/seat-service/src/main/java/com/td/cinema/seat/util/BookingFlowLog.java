package com.td.cinema.seat.util;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

/** Ghi kết quả nghiệp vụ sau commit, giữ đúng file và dòng đã yêu cầu ghi log. */
public final class BookingFlowLog {
    private static final String MESSAGE =
            "BOOKING_FLOW bookingId={} participant={} action={} result={}";

    private BookingFlowLog() {}

    /** Ghi sau commit; nếu không có transaction thì ghi ngay tại vị trí gọi hiện tại. */
    public static void committed(UUID bookingId, String participant, String action, String result) {
        prepare(BookingFlowLog.class, bookingId, participant, action, result).run();
    }

    /**
     * Giữ vị trí bên gọi hàm trung gian trước khi đi vào TransactionTemplate.
     * Phải chạy Runnable trả về bên trong transaction, sau khi đã lưu bước xử lý.
     */
    public static Runnable deferredFromCaller(Class<?> wrapper, UUID bookingId,
            String participant, String action, String result) {
        return prepare(wrapper, bookingId, participant, action, result);
    }

    /** Chụp vị trí gọi ngay, tránh lấy nhầm vị trí của callback afterCommit khi ghi muộn. */
    private static Runnable prepare(Class<?> boundary, UUID bookingId,
            String participant, String action, String result) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        StackTraceElement[] caller = CallerData.extract(new Throwable(), boundary.getName(),
                context.getMaxCallerDataDepth(), context.getFrameworkPackages());
        String loggerName = caller.length == 0 ? boundary.getName() : caller[0].getClassName();
        Logger logger = context.getLogger(loggerName);
        Runnable write = () -> {
            if (!logger.isInfoEnabled()) return;
            // Dùng Logback có sẵn của Spring Boot để gắn vị trí gốc vào sự kiện ghi sau commit.
            LoggingEvent event = new LoggingEvent(BookingFlowLog.class.getName(), logger,
                    Level.INFO, MESSAGE, null, new Object[] {bookingId, participant, action, result});
            event.setCallerData(caller);
            logger.callAppenders(event);
        };
        return () -> afterCommit(write);
    }

    /** Chỉ công bố kết quả đã commit; transaction rollback sẽ không phát dòng kết quả này. */
    private static void afterCommit(Runnable write) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    write.run();
                }
            });
        } else {
            write.run();
        }
    }
}
