package structural.bridge.solution;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import structural.bridge.solution.device.ProjectorDevice;
import structural.bridge.solution.device.RadioDevice;
import structural.bridge.solution.device.TvDevice;
import structural.bridge.solution.remote.AdvancedRemote;
import structural.bridge.solution.remote.BasicRemote;
import structural.bridge.solution.remote.PremiumRemote;

// Điểm chạy Bridge: kết hợp các Remote với thiết bị và so sánh đủ 9 tình huống.
public class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        // Cấu hình UTF-8 để tiếng Việt hiển thị đúng trên console; không tham gia xử lý nghiệp vụ.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // TV: Basic bật máy, tăng 30 lên 40, rồi giảm về 30.
        System.out.println("=== BASIC REMOTE + TV: POWER + VOLUME ===");
        TvDevice basicTvDevice = new TvDevice();
        BasicRemote basicTvRemote = new BasicRemote(basicTvDevice);
        basicTvRemote.power();
        basicTvRemote.volumeUp();
        basicTvRemote.volumeDown();

        System.out.println();
        // Advanced có đủ các nút Basic; gọi cả tăng/giảm, rồi thêm tắt tiếng.
        System.out.println("=== ADVANCED REMOTE + TV: POWER + VOLUME + MUTE ===");
        TvDevice advancedTvDevice = new TvDevice();
        AdvancedRemote advancedTvRemote = new AdvancedRemote(advancedTvDevice);
        advancedTvRemote.power();
        advancedTvRemote.volumeUp();
        advancedTvRemote.volumeDown();
        advancedTvRemote.mute();

        System.out.println();
        // Premium có đủ các nút Advanced; lưu 40, giảm về 30, mute rồi khôi phục 40.
        System.out.println("=== PREMIUM REMOTE + TV: POWER + VOLUME + MUTE + SAVE / RESTORE ===");
        TvDevice premiumTvDevice = new TvDevice();
        PremiumRemote premiumTvRemote = new PremiumRemote(premiumTvDevice);
        premiumTvRemote.power();
        premiumTvRemote.volumeUp();
        premiumTvRemote.saveVolume();
        premiumTvRemote.volumeDown();
        premiumTvRemote.mute();
        premiumTvRemote.restoreVolume();

        System.out.println();
        // Radio: Basic bật máy, tăng 30 lên 40, rồi giảm về 30.
        System.out.println("=== BASIC REMOTE + RADIO: POWER + VOLUME ===");
        RadioDevice basicRadioDevice = new RadioDevice();
        BasicRemote basicRadioRemote = new BasicRemote(basicRadioDevice);
        basicRadioRemote.power();
        basicRadioRemote.volumeUp();
        basicRadioRemote.volumeDown();

        System.out.println();
        // Advanced có đủ các nút Basic; gọi cả tăng/giảm, rồi thêm tắt tiếng.
        System.out.println("=== ADVANCED REMOTE + RADIO: POWER + VOLUME + MUTE ===");
        RadioDevice advancedRadioDevice = new RadioDevice();
        AdvancedRemote advancedRadioRemote = new AdvancedRemote(advancedRadioDevice);
        advancedRadioRemote.power();
        advancedRadioRemote.volumeUp();
        advancedRadioRemote.volumeDown();
        advancedRadioRemote.mute();

        System.out.println();
        // Premium có đủ các nút Advanced; lưu 40, giảm về 30, mute rồi khôi phục 40.
        System.out.println(
                "=== PREMIUM REMOTE + RADIO: POWER + VOLUME + MUTE + SAVE / RESTORE ===");
        RadioDevice premiumRadioDevice = new RadioDevice();
        PremiumRemote premiumRadioRemote = new PremiumRemote(premiumRadioDevice);
        premiumRadioRemote.power();
        premiumRadioRemote.volumeUp();
        premiumRadioRemote.saveVolume();
        premiumRadioRemote.volumeDown();
        premiumRadioRemote.mute();
        premiumRadioRemote.restoreVolume();

        System.out.println();
        // Projector: Basic bật máy, tăng 30 lên 40, rồi giảm về 30.
        System.out.println("=== BASIC REMOTE + PROJECTOR: POWER + VOLUME ===");
        ProjectorDevice basicProjectorDevice = new ProjectorDevice();
        BasicRemote basicProjectorRemote = new BasicRemote(basicProjectorDevice);
        basicProjectorRemote.power();
        basicProjectorRemote.volumeUp();
        basicProjectorRemote.volumeDown();

        System.out.println();
        // Advanced có đủ các nút Basic; gọi cả tăng/giảm, rồi thêm tắt tiếng.
        System.out.println("=== ADVANCED REMOTE + PROJECTOR: POWER + VOLUME + MUTE ===");
        ProjectorDevice advancedProjectorDevice = new ProjectorDevice();
        AdvancedRemote advancedProjectorRemote = new AdvancedRemote(advancedProjectorDevice);
        advancedProjectorRemote.power();
        advancedProjectorRemote.volumeUp();
        advancedProjectorRemote.volumeDown();
        advancedProjectorRemote.mute();

        System.out.println();
        // Premium có đủ các nút Advanced; lưu 40, giảm về 30, mute rồi khôi phục 40.
        System.out.println(
                "=== PREMIUM REMOTE + PROJECTOR: POWER + VOLUME + MUTE + SAVE / RESTORE ===");
        ProjectorDevice premiumProjectorDevice = new ProjectorDevice();
        PremiumRemote premiumProjectorRemote = new PremiumRemote(premiumProjectorDevice);
        premiumProjectorRemote.power();
        premiumProjectorRemote.volumeUp();
        premiumProjectorRemote.saveVolume();
        premiumProjectorRemote.volumeDown();
        premiumProjectorRemote.mute();
        premiumProjectorRemote.restoreVolume();
    }
}
