package behavioral.iterator.solution.player;

import behavioral.iterator.Song;
import behavioral.iterator.solution.playlist.Playlist;
import java.util.Iterator;

/** Trình phát sau khi áp dụng Iterator: giao việc tìm bài cho bộ duyệt của playlist. */
public final class MusicPlayer {
    // Iterator<Song> là kiểu interface của Java: next() trả Song, hasNext() trả boolean.
    // Biến này trỏ tới FavoriteIterator, AlbumIterator hoặc ArtistIterator do playlist tạo; null
    // khi chưa chọn nguồn.
    // Chính object bộ duyệt cụ thể giữ vị trí và cách lấy bài tiếp theo.
    private Iterator<Song> currentIterator;

    // Gọi một lần khi chọn nguồn; giữ iterator này cho các lần bấm tiếp theo.
    public void selectPlaylist(Playlist playlist) {
        // Gọi iterator() của playlist đang chọn; playlist tạo bộ duyệt cụ thể, bắt đầu từ đầu danh
        // sách.
        currentIterator = playlist.iterator();
        System.out.println("[PLAYER] Đã nhận Iterator của danh sách vừa chọn");
    }

    // Chỉ hỏi còn bài rồi lấy bài; việc chuyển đĩa/ca sĩ thuộc về iterator cụ thể.
    public Song nextSong() {
        if (currentIterator == null) {
            System.out.println("[PLAYER] Hãy chọn danh sách trước");
            return null;
        }
        if (!currentIterator.hasNext()) {
            System.out.println("[PLAYER] Đã hết danh sách");
            return null;
        }
        // Chạy next() trên object bộ duyệt cụ thể và nhận một bài hát Song.
        Song song = currentIterator.next();
        System.out.println("[PLAY] " + song);
        return song;
    }
}
