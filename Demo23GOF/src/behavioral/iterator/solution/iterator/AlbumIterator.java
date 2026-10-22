package behavioral.iterator.solution.iterator;

import behavioral.iterator.Song;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/** Bộ duyệt album: tự chuyển đĩa và bỏ qua đĩa rỗng để player chỉ nhận từng Song. */
// Iterator<Song> là interface có sẵn của Java; AlbumIterator là class demo tự viết để cài đặt cách
// duyệt album.
public final class AlbumIterator implements Iterator<Song> {
    // Danh sách đĩa trong album; mỗi phần tử là danh sách các bài hát của một đĩa.
    private final List<List<Song>> discs;
    private int discIndex; // Chỉ số đĩa đang duyệt trong discs, tính từ 0.
    // Chỉ số bài hát tiếp theo trong discs.get(discIndex), tính từ 0; về 0 khi đổi đĩa.
    private int songIndex;

    public AlbumIterator(List<List<Song>> discs) {
        this.discs = discs;
    }

    @Override
    public boolean hasNext() {
        // Chỉ bỏ qua đĩa đã hết hoặc rỗng, chưa lấy bài hát nào.
        while (discIndex < discs.size() && songIndex >= discs.get(discIndex).size()) {
            discIndex++;
            songIndex = 0;
        }
        return discIndex < discs.size();
    }

    @Override
    public Song next() {
        // Gọi hasNext() ở đây để next() trực tiếp cũng xử lý được việc chuyển đĩa.
        if (!hasNext()) {
            throw new NoSuchElementException("Đã hết album");
        }
        return discs.get(discIndex).get(songIndex++);
    }
}
