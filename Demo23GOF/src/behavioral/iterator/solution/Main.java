package behavioral.iterator.solution;

import behavioral.iterator.DemoData;
import behavioral.iterator.Song;
import behavioral.iterator.solution.player.MusicPlayer;
import behavioral.iterator.solution.playlist.AlbumPlaylist;
import behavioral.iterator.solution.playlist.ArtistPlaylist;
import behavioral.iterator.solution.playlist.FavoritePlaylist;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * Điểm chạy bản Solution: bọc dữ liệu thành Playlist rồi đưa cho MusicPlayer.
 * Luồng chính: selectPlaylist() -> Playlist.iterator() -> hasNext()/next() ở iterator cụ thể.
 */
public final class Main {
    public static void main(String[] args) throws UnsupportedEncodingException {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        // DemoData tạo dữ liệu mẫu; khi chạy, console hiển thị nguồn được chọn, lần bấm Next và bài
        // thực sự lấy được.
        DemoData data = new DemoData();
        System.out.println("DEMO ITERATOR: CÙNG MỘT NÚT BÀI TIẾP THEO");
        System.out.println("===== SOLUTION: ITERATOR PATTERN =====");
        MusicPlayer player = new MusicPlayer();

        choose("danh sách yêu thích");
        player.selectPlaylist(new FavoritePlaylist(data.getFavorites()));
        // Có 3 bài nhưng bấm 4 lần để đối chiếu cả tình huống hết bài với bản Problem.
        for (int i = 0; i < 4; i++) {
            pressNext();
            player.nextSong();
        }

        choose("Album nhiều đĩa");
        player.selectPlaylist(new AlbumPlaylist(data.getDiscs()));
        for (int i = 0; i < 5; i++) {
            pressNext();
            player.nextSong();
        }

        choose("thư viện theo ca sĩ");
        player.selectPlaylist(new ArtistPlaylist(data.getArtists()));
        for (int i = 0; i < 5; i++) {
            pressNext();
            player.nextSong();
        }

        choose("danh sách yêu thích rỗng");
        player.selectPlaylist(new FavoritePlaylist(Collections.<Song>emptyList()));
        pressNext();
        player.nextSong();

        // Chọn Album sẽ tạo iterator mới, nên không dùng tiếp vị trí đang nghe ở Yêu thích.
        System.out.println("\n[USER] Chuyển danh sách khi đang nghe dở");
        player.selectPlaylist(new FavoritePlaylist(data.getFavorites()));
        pressNext();
        player.nextSong();
        pressNext();
        player.nextSong();
        player.selectPlaylist(new AlbumPlaylist(data.getDiscs()));
        pressNext();
        player.nextSong();
        System.out.println(
                "[GIẢI THÍCH] Album bắt đầu lại từ Bài A, không tiếp tục vị trí của Yêu thích.");
        printComparison();
    }

    private static void choose(String name) {
        System.out.println("\n[USER] Chọn " + name);
    }

    private static void pressNext() {
        System.out.println("\n[USER] Bấm Bài tiếp theo");
    }

    private static void printComparison() {
        // Phần này chỉ in nhận xét về thiết kế; dữ liệu mẫu và tiêu chí kiểm tra được giải thích
        // trong README.
        System.out.println("\n===== SO SÁNH NƠI CHỨA CÁCH DUYỆT =====");
        System.out.println(
                "PROBLEM: MusicPlayerBad giữ các chỉ số và tự duyệt từng nguồn bài hát.");
        System.out.println(
                "SOLUTION: FavoriteIterator, AlbumIterator và ArtistIterator giữ cách duyệt cùng vị trí riêng.");
        System.out.println(
                "MusicPlayer giữ bộ duyệt hiện tại, gọi hasNext() và next() để lấy bài.");
        System.out.println(
                "Iterator<Song> là kiểu interface của Java; object thực tế được tạo từ các class bộ duyệt trên.");
    }
}
