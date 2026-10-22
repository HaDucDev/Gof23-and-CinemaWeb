package behavioral.iterator.solution.iterator;

import behavioral.iterator.Song;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/** Bộ duyệt thư viện: hết bài của ca sĩ hiện tại mới chuyển sang nhóm ca sĩ kế tiếp. */
// Class do demo tự viết; implements Iterator<Song> để trả từng bài hát qua các phương thức do
// interface của Java quy định.
public final class ArtistIterator implements Iterator<Song> {
    // Bộ duyệt các cặp tên ca sĩ và danh sách bài hát, theo thứ tự thêm ca sĩ vào LinkedHashMap.
    // Đây là bộ duyệt nhóm có sẵn từ Java; ArtistIterator dùng nó bên trong để lần lượt lấy bài của
    // từng ca sĩ.
    private final Iterator<Map.Entry<String, List<Song>>> artistGroups;
    // Danh sách bài hát của ca sĩ đang duyệt; ban đầu rỗng vì chưa chọn ca sĩ nào.
    private List<Song> currentSongs = Collections.emptyList();
    private int songIndex; // Chỉ số bài sẽ lấy trong currentSongs, tính từ 0.

    public ArtistIterator(LinkedHashMap<String, List<Song>> artists) {
        // Lấy bộ duyệt có sẵn của tập entry trong map; mỗi entry gồm tên ca sĩ và danh sách bài của
        // ca sĩ đó.
        artistGroups = artists.entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
        // Danh sách bài của ca sĩ hiện tại đã hết/rỗng thì sang ca sĩ kế tiếp; chưa lấy bài hát ra
        // để phát.
        while (songIndex >= currentSongs.size() && artistGroups.hasNext()) {
            currentSongs = artistGroups.next().getValue();
            songIndex = 0;
        }
        return songIndex < currentSongs.size();
    }

    @Override
    public Song next() {
        // hasNext() tìm ca sĩ còn bài; next() trả một bài của ca sĩ đó rồi tăng songIndex.
        if (!hasNext()) {
            throw new NoSuchElementException("Đã hết thư viện theo ca sĩ");
        }
        return currentSongs.get(songIndex++);
    }
}
