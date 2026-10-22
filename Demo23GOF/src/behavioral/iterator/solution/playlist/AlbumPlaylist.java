package behavioral.iterator.solution.playlist;

import behavioral.iterator.Song;
import behavioral.iterator.solution.iterator.AlbumIterator;
import java.util.Iterator;
import java.util.List;

/** Nguồn album nhiều đĩa: giữ cấu trúc lồng nhau, giao cách chuyển đĩa cho AlbumIterator. */
public final class AlbumPlaylist implements Playlist {
    // Danh sách đĩa trong album; mỗi phần tử là danh sách các bài hát của một đĩa.
    private final List<List<Song>> discs;

    public AlbumPlaylist(List<List<Song>> discs) {
        this.discs = discs;
    }

    // Kiểu trả về chung là Iterator<Song>; object thực tế được tạo ở dưới là AlbumIterator của
    // demo.
    // Mỗi lần gọi tạo bộ duyệt mới, bắt đầu ở đĩa 0, bài 0.
    @Override
    public Iterator<Song> iterator() {
        return new AlbumIterator(discs);
    }
}
