package behavioral.iterator;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/** Tạo dữ liệu mẫu cho hai bản demo để so sánh cùng bài hát và cùng thứ tự phát. */
public final class DemoData {
    // Danh sách bài hát yêu thích gồm Bài A, Bài B và Bài C.
    private final List<Song> favorites;
    private final List<List<Song>> discs; // Album hai đĩa: [A, B] và [C, D].
    // Tên Ca sĩ A -> [Bài A, Bài B]; tên Ca sĩ B -> [Bài C, Bài D].
    private final LinkedHashMap<String, List<Song>> artists;

    public DemoData() {
        Song a = new Song("Bài A", "Ca sĩ A");
        Song b = new Song("Bài B", "Ca sĩ A");
        Song c = new Song("Bài C", "Ca sĩ B");
        Song d = new Song("Bài D", "Ca sĩ B");

        // Cùng các object Song được tổ chức theo ba cấu trúc khác nhau.
        favorites = Arrays.asList(a, b, c);
        discs = Arrays.asList(Arrays.asList(a, b), Arrays.asList(c, d));
        // LinkedHashMap giữ thứ tự tên ca sĩ đã thêm: Ca sĩ A trước, Ca sĩ B sau.
        artists = new LinkedHashMap<String, List<Song>>();
        artists.put("Ca sĩ A", Arrays.asList(a, b));
        artists.put("Ca sĩ B", Arrays.asList(c, d));
    }

    public List<Song> getFavorites() {
        return favorites;
    }

    public List<List<Song>> getDiscs() {
        return discs;
    }

    public LinkedHashMap<String, List<Song>> getArtists() {
        return artists;
    }
}
