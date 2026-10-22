package behavioral.iterator.solution.playlist;

import behavioral.iterator.Song;
import behavioral.iterator.solution.iterator.ArtistIterator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/** Nguồn thư viện theo ca sĩ: tạo ArtistIterator để lần lượt duyệt bài của từng nhóm. */
public final class ArtistPlaylist implements Playlist {
    // Khóa là tên ca sĩ; giá trị là danh sách bài hát của ca sĩ đó; giữ thứ tự thêm ca sĩ.
    private final LinkedHashMap<String, List<Song>> artists;

    public ArtistPlaylist(LinkedHashMap<String, List<Song>> artists) {
        this.artists = artists;
    }

    // Trả object ArtistIterator do demo tự viết qua kiểu chung Iterator<Song> của Java.
    // Bộ duyệt mới tự đi qua map và từng bài trong nhóm; player chỉ nhận lần lượt các Song.
    @Override
    public Iterator<Song> iterator() {
        return new ArtistIterator(artists);
    }
}
