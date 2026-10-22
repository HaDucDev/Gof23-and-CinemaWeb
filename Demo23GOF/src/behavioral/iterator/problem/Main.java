package behavioral.iterator.problem;

import behavioral.iterator.DemoData;
import behavioral.iterator.Song;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Điểm chạy bản Problem: tạo dữ liệu, chọn nguồn và mô phỏng bấm Bài tiếp theo.
 * Logic tìm bài nằm trong MusicPlayerBad.nextSong(); Main chỉ dựng kịch bản thao tác.
 */
public final class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // DemoData tạo dữ liệu mẫu cho ba nguồn bài hát; các println bên dưới chỉ mô tả thao tác
        // đang chạy.
        DemoData data = new DemoData();
        System.out.println("DEMO ITERATOR: CÙNG MỘT NÚT BÀI TIẾP THEO");
        System.out.println("===== PROBLEM: CHƯA DÙNG ITERATOR =====");
        MusicPlayerBad player = new MusicPlayerBad();

        choose("danh sách yêu thích");
        player.selectFavorites(data.getFavorites());
        // Có 3 bài nhưng bấm 4 lần để quan sát cả thông báo hết danh sách.
        for (int i = 0; i < 4; i++) {
            pressNext();
            player.nextSong();
        }

        choose("Album nhiều đĩa");
        player.selectAlbum(data.getDiscs());
        for (int i = 0; i < 5; i++) {
            pressNext();
            player.nextSong();
        }

        choose("thư viện theo ca sĩ");
        player.selectArtists(data.getArtists());
        for (int i = 0; i < 5; i++) {
            pressNext();
            player.nextSong();
        }

        choose("danh sách yêu thích rỗng");
        player.selectFavorites(Collections.<Song>emptyList());
        pressNext();
        player.nextSong();

        // Đang nghe A, B ở Yêu thích rồi chuyển Album: vị trí phải được reset về A.
        System.out.println("\n[USER] Chuyển danh sách khi đang nghe dở");
        player.selectFavorites(data.getFavorites());
        pressNext();
        player.nextSong();
        pressNext();
        player.nextSong();
        player.selectAlbum(data.getDiscs());
        pressNext();
        player.nextSong();

        System.out.println(
                "\n[SO SÁNH] MusicPlayerBad giữ các chỉ số và tự tìm bài tiếp theo cho từng loại danh sách.");
    }

    private static void choose(String name) {
        System.out.println("\n[USER] Chọn " + name);
    }

    private static void pressNext() {
        System.out.println("\n[USER] Bấm Bài tiếp theo");
    }
}
