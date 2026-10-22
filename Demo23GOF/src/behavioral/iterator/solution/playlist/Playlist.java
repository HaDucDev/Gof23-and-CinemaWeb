package behavioral.iterator.solution.playlist;

import behavioral.iterator.Song;
import java.util.Iterator;

/** Playlist do demo khai báo; mở rộng Iterable<Song> của Java để mỗi nguồn cung cấp bộ duyệt bài hát. */
public interface Playlist extends Iterable<Song> {
    // Iterator<Song> là kiểu trả về (interface có sẵn của Java); iterator() là tên phương thức.
    // Object trả về do demo tự viết: FavoriteIterator, AlbumIterator hoặc ArtistIterator.
    // Mỗi lần gọi trả một iterator mới có vị trí riêng, bắt đầu từ đầu danh sách.
    @Override
    Iterator<Song> iterator();
}
