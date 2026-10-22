package behavioral.iterator.problem;

import behavioral.iterator.Song;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Trình phát trước khi áp dụng Iterator: vừa phát bài, vừa tự duyệt mọi kiểu danh sách.
 * Đọc từ problem/Main.java vào nextSong() để thấy player phải tự chuyển đĩa/ca sĩ.
 */
public final class MusicPlayerBad {
    // Các loại nguồn bài hát để nextSong() chọn cách duyệt: yêu thích, album hoặc theo ca sĩ.
    private enum Type {
        NONE,
        FAVORITES,
        ALBUM,
        ARTISTS
    }

    // Loại nguồn bài hát đang chọn; NONE nghĩa là chưa chọn nguồn.
    private Type type = Type.NONE;
    // Danh sách các bài hát yêu thích (Song), không chia theo đĩa hoặc ca sĩ.
    private List<Song> favorites;
    // Album: danh sách đĩa, mỗi đĩa chứa một danh sách bài hát.
    private List<List<Song>> discs;
    // Khóa là tên ca sĩ; giá trị là danh sách bài hát của ca sĩ đó; giữ thứ tự thêm ca sĩ.
    private LinkedHashMap<String, List<Song>> artists;
    // Danh sách tên ca sĩ lấy từ các khóa của artists, dùng artistIndex để chọn ca sĩ tiếp theo.
    private List<String> artistNames;
    // Chỉ số bài hát tiếp theo trong favorites, đĩa hiện tại hoặc danh sách bài của ca sĩ hiện tại;
    // tính từ 0.
    private int songIndex;
    private int discIndex; // Vị trí đĩa đang duyệt khi chọn ALBUM, tính từ 0.
    private int artistIndex; // Chỉ số tên ca sĩ đang duyệt trong artistNames, tính từ 0.

    // songs là danh sách bài hát yêu thích; chọn danh sách này rồi đưa vị trí phát về đầu.
    public void selectFavorites(List<Song> songs) {
        type = Type.FAVORITES;
        favorites = songs;
        reset();
        System.out.println("[PLAYER] Kiểm tra loại danh sách: Yêu thích");
    }

    // albumDiscs là danh sách đĩa trong album; mỗi đĩa là một danh sách bài hát mà player phải tự
    // duyệt.
    public void selectAlbum(List<List<Song>> albumDiscs) {
        type = Type.ALBUM;
        discs = albumDiscs;
        reset();
        System.out.println("[PLAYER] Kiểm tra loại danh sách: Album nhiều đĩa");
    }

    // artistSongs ánh xạ tên ca sĩ tới danh sách bài hát; player lưu cả dữ liệu lẫn thứ tự ca sĩ để
    // tự duyệt.
    public void selectArtists(LinkedHashMap<String, List<Song>> artistSongs) {
        type = Type.ARTISTS;
        artists = artistSongs;
        artistNames = new ArrayList<String>(artistSongs.keySet());
        reset();
        System.out.println("[PLAYER] Kiểm tra loại danh sách: Thư viện theo ca sĩ");
    }

    // Chọn nguồn mới hoặc chọn lại nguồn cũ đều phát từ bài đầu tiên.
    private void reset() {
        songIndex = 0;
        discIndex = 0;
        artistIndex = 0;
    }

    // Mỗi lần bấm lấy tối đa một bài; null nghĩa là chưa chọn nguồn hoặc đã hết bài.
    public Song nextSong() {
        // Trình phát phải biết cả loại dữ liệu lẫn cách duyệt từng loại.
        switch (type) {
            case FAVORITES:
                if (songIndex < favorites.size()) {
                    System.out.println("[PLAYER] Lấy bài theo vị trí " + (songIndex + 1));
                    return play(favorites.get(songIndex++));
                }
                break;
            case ALBUM:
                // Hết bài trong đĩa thì sang đĩa sau; vòng lặp cũng bỏ qua đĩa rỗng.
                while (discIndex < discs.size()) {
                    List<Song> disc = discs.get(discIndex);
                    if (songIndex < disc.size()) {
                        System.out.println(
                                "[PLAYER] Đang ở đĩa "
                                        + (discIndex + 1)
                                        + ", bài "
                                        + (songIndex + 1));
                        return play(disc.get(songIndex++));
                    }
                    discIndex++;
                    songIndex = 0;
                    if (discIndex < discs.size()) {
                        System.out.println(
                                "[PLAYER] Đã hết đĩa "
                                        + discIndex
                                        + ", chuyển sang đĩa "
                                        + (discIndex + 1));
                    }
                }
                break;
            case ARTISTS:
                // Hết bài của ca sĩ hiện tại thì sang ca sĩ kế tiếp và duyệt bài lại từ vị trí 0.
                while (artistIndex < artistNames.size()) {
                    // Tên ca sĩ đang được chọn để lấy bài hát.
                    String artist = artistNames.get(artistIndex);
                    // Danh sách bài hát của ca sĩ vừa lấy tên.
                    List<Song> songs = artists.get(artist);
                    if (songIndex < songs.size()) {
                        System.out.println("[PLAYER] Lấy bài của " + artist);
                        return play(songs.get(songIndex++));
                    }
                    artistIndex++;
                    songIndex = 0;
                    if (artistIndex < artistNames.size()) {
                        System.out.println(
                                "[PLAYER] Đã hết bài của "
                                        + artist
                                        + ", chuyển sang "
                                        + artistNames.get(artistIndex));
                    }
                }
                break;
            default:
                System.out.println("[PLAYER] Hãy chọn danh sách trước");
                return null;
        }
        System.out.println("[PLAYER] Đã hết danh sách");
        return null;
    }

    // Chỉ mô phỏng phát nhạc bằng console; trả bài để có thể kiểm tra thứ tự phát.
    private Song play(Song song) {
        System.out.println("[PLAY] " + song);
        return song;
    }
}
