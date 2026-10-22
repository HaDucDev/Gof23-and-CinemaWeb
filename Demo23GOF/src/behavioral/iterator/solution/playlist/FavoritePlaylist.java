package behavioral.iterator.solution.playlist;

import behavioral.iterator.Song;
import behavioral.iterator.solution.iterator.FavoriteIterator;
import java.util.Iterator;
import java.util.List;

/** Nguồn bài yêu thích: giữ danh sách phẳng và tạo FavoriteIterator để duyệt nó. */
public final class FavoritePlaylist implements Playlist {
    // Danh sách các bài hát yêu thích; vị trí đang duyệt được giữ ở FavoriteIterator.
    private final List<Song> songs;

    public FavoritePlaylist(List<Song> songs) {
        this.songs = songs;
    }

    // Trả object FavoriteIterator do demo tự viết qua kiểu chung Iterator<Song> của Java.
    // Mỗi lần gọi tạo bộ duyệt mới để từng lượt nghe có vị trí độc lập.
    @Override
    public Iterator<Song> iterator() {
        return new FavoriteIterator(songs);
    }
}
