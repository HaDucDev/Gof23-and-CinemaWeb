package behavioral.iterator.solution.iterator;

import behavioral.iterator.Song;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** Bộ duyệt danh sách yêu thích; giữ vị trí bài tiếp theo thay cho MusicPlayer. */
// Class do demo tự viết; implements Iterator<Song> để cài đặt các phương thức của interface có sẵn
// trong Java.
public final class FavoriteIterator implements Iterator<Song> {
    // Danh sách các bài hát yêu thích mà bộ duyệt này đang duyệt.
    private final List<Song> songs;
    // Chỉ số bài hát tiếp theo trong songs mà next() sẽ trả, bắt đầu từ 0.
    private int songIndex;

    public FavoriteIterator(List<Song> songs) {
        this.songs = songs;
    }

    // Kiểm tra còn bài, không tăng vị trí nên có thể gọi nhiều lần.
    @Override
    public boolean hasNext() {
        return songIndex < songs.size();
    }

    @Override
    public Song next() {
        // Theo quy định của Iterator: hết phần tử thì ném exception, không trả null.
        if (!hasNext()) {
            throw new NoSuchElementException("Đã hết danh sách yêu thích");
        }
        // Lấy bài ở vị trí hiện tại rồi mới tăng chỉ số cho lần gọi sau.
        return songs.get(songIndex++);
    }
}
