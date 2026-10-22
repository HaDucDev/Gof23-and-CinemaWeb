package behavioral.iterator;

import java.util.Objects;

/** Dữ liệu một bài hát dùng chung cho Problem và Solution; không chứa logic duyệt. */
public final class Song {
    private final String title; // Tên bài hát hiển thị khi phát.
    private final String artist; // Tên ca sĩ của bài hát.

    public Song(String title, String artist) {
        this.title = Objects.requireNonNull(title);
        this.artist = Objects.requireNonNull(artist);
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    // println(song) dùng phương thức này để in dạng "Tên bài - Ca sĩ".
    @Override
    public String toString() {
        return title + " - " + artist;
    }
}
