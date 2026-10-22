package structural.bridge.problem;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

// Điểm chạy Bridge bản Problem: chọn 9 class cho từng cặp Remote và thiết bị.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // TV: Basic bật máy, tăng 30 lên 40, rồi giảm về 30.
        System.out.println("=== BASIC REMOTE + TV: POWER + VOLUME ===");
        BasicTvRemote basicTvRemote = new BasicTvRemote();
        basicTvRemote.power();
        basicTvRemote.volumeUp();
        basicTvRemote.volumeDown();

        System.out.println();
        // Advanced có đủ các nút Basic; gọi cả tăng/giảm, rồi thêm tắt tiếng.
        System.out.println("=== ADVANCED REMOTE + TV: POWER + VOLUME + MUTE ===");
        AdvancedTvRemote advancedTvRemote = new AdvancedTvRemote();
        advancedTvRemote.power();
        advancedTvRemote.volumeUp();
        advancedTvRemote.volumeDown();
        advancedTvRemote.mute();

        System.out.println();
        // Premium có đủ các nút Advanced; lưu 40, giảm về 30, mute rồi khôi phục 40.
        System.out.println("=== PREMIUM REMOTE + TV: POWER + VOLUME + MUTE + SAVE / RESTORE ===");
        PremiumTvRemote premiumTvRemote = new PremiumTvRemote();
        premiumTvRemote.power();
        premiumTvRemote.volumeUp();
        premiumTvRemote.saveVolume();
        premiumTvRemote.volumeDown();
        premiumTvRemote.mute();
        premiumTvRemote.restoreVolume();

        System.out.println();
        // Radio: Basic bật máy, tăng 30 lên 40, rồi giảm về 30.
        System.out.println("=== BASIC REMOTE + RADIO: POWER + VOLUME ===");
        BasicRadioRemote basicRadioRemote = new BasicRadioRemote();
        basicRadioRemote.power();
        basicRadioRemote.volumeUp();
        basicRadioRemote.volumeDown();

        System.out.println();
        // Advanced có đủ các nút Basic; gọi cả tăng/giảm, rồi thêm tắt tiếng.
        System.out.println("=== ADVANCED REMOTE + RADIO: POWER + VOLUME + MUTE ===");
        AdvancedRadioRemote advancedRadioRemote = new AdvancedRadioRemote();
        advancedRadioRemote.power();
        advancedRadioRemote.volumeUp();
        advancedRadioRemote.volumeDown();
        advancedRadioRemote.mute();

        System.out.println();
        // Premium có đủ các nút Advanced; lưu 40, giảm về 30, mute rồi khôi phục 40.
        System.out.println(
                "=== PREMIUM REMOTE + RADIO: POWER + VOLUME + MUTE + SAVE / RESTORE ===");
        PremiumRadioRemote premiumRadioRemote = new PremiumRadioRemote();
        premiumRadioRemote.power();
        premiumRadioRemote.volumeUp();
        premiumRadioRemote.saveVolume();
        premiumRadioRemote.volumeDown();
        premiumRadioRemote.mute();
        premiumRadioRemote.restoreVolume();

        System.out.println();
        // Projector: Basic bật máy, tăng 30 lên 40, rồi giảm về 30.
        System.out.println("=== BASIC REMOTE + PROJECTOR: POWER + VOLUME ===");
        BasicProjectorRemote basicProjectorRemote = new BasicProjectorRemote();
        basicProjectorRemote.power();
        basicProjectorRemote.volumeUp();
        basicProjectorRemote.volumeDown();

        System.out.println();
        // Advanced có đủ các nút Basic; gọi cả tăng/giảm, rồi thêm tắt tiếng.
        System.out.println("=== ADVANCED REMOTE + PROJECTOR: POWER + VOLUME + MUTE ===");
        AdvancedProjectorRemote advancedProjectorRemote = new AdvancedProjectorRemote();
        advancedProjectorRemote.power();
        advancedProjectorRemote.volumeUp();
        advancedProjectorRemote.volumeDown();
        advancedProjectorRemote.mute();

        System.out.println();
        // Premium có đủ các nút Advanced; lưu 40, giảm về 30, mute rồi khôi phục 40.
        System.out.println(
                "=== PREMIUM REMOTE + PROJECTOR: POWER + VOLUME + MUTE + SAVE / RESTORE ===");
        PremiumProjectorRemote premiumProjectorRemote = new PremiumProjectorRemote();
        premiumProjectorRemote.power();
        premiumProjectorRemote.volumeUp();
        premiumProjectorRemote.saveVolume();
        premiumProjectorRemote.volumeDown();
        premiumProjectorRemote.mute();
        premiumProjectorRemote.restoreVolume();
    }
}
