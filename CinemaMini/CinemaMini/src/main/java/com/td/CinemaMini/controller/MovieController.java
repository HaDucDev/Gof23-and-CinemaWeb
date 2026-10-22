package com.td.CinemaMini.controller;

import com.td.CinemaMini.exception.BusinessException;
import com.td.CinemaMini.model.*;
import com.td.CinemaMini.repository.*;
import com.td.CinemaMini.service.booking.*;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import static com.td.CinemaMini.dto.Values.map;

@Controller
public class MovieController {
    private static final int MOVIES_PER_PAGE = 9;
    private final MovieRepository movies;
    private final ShowtimeRepository showtimes;
    private final DatabaseClock clock;

    public MovieController(
            MovieRepository movies, ShowtimeRepository showtimes, DatabaseClock clock) {
        this.movies = movies;
        this.showtimes = showtimes;
        this.clock = clock;
    }

    /**
     * Chuyển trang chủ sang danh sách phim và suất chiếu.
     *
     * @return Chuyển hướng đến /movies.
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/movies";
    }

    /**
     * Hiển thị danh sách phim theo trang, kèm các suất chiếu sắp tới của từng phim.
     *
     * @param page Số trang từ query string page, bắt đầu từ 1 và mặc định là 1; mỗi trang hiển thị
     *     9 phim.
     * @param model Model do Spring cấp để đưa danh sách phim, suất sắp tới và thông tin phân trang
     *     ra giao diện.
     * @return Trang movies; chuyển sang trang hợp lệ nếu số trang nằm ngoài phạm vi hỗ trợ.
     */
    @GetMapping("/movies")
    public String movies(@RequestParam(defaultValue = "1") int page, Model model) {
        // JPA only supports query offsets within the signed integer range.
        if (page < 1 || ((long) page - 1) * MOVIES_PER_PAGE > Integer.MAX_VALUE) {
            return "redirect:/movies?page=1";
        }
        Page<Movie> moviePage =
                movies.findAll(
                        PageRequest.of(page - 1, MOVIES_PER_PAGE, Sort.by("id").ascending()));
        int lastPage = Math.max(1, moviePage.getTotalPages());
        if (page > lastPage) {
            return "redirect:/movies?page=" + lastPage;
        }
        List<Map<String, Object>> cards = new ArrayList<Map<String, Object>>();
        List<Showtime> upcoming = showtimes.findByStartsAtAfterOrderByStartsAtAsc(clock.now());
        for (Movie movie : moviePage) {
            List<Showtime> times = new ArrayList<Showtime>();
            for (Showtime time : upcoming) {
                if (time.getMovieId().equals(movie.getId())) {
                    times.add(time);
                }
            }
            cards.add(map("movie", movie, "showtimes", times));
        }
        model.addAttribute("cards", cards);
        model.addAttribute("moviePage", moviePage);
        model.addAttribute("startPage", Math.max(1, page - 2));
        model.addAttribute("endPage", (int) Math.min(lastPage, (long) page + 2));
        return "movies";
    }

    /**
     * Hiển thị trang chọn ghế cho một suất chiếu.
     *
     * @param id ID suất chiếu từ phần {id} trong URL, tương ứng Showtime.id.
     * @param model Model do Spring cấp để đưa suất, phim, mã yêu cầu và thông tin suất đã bắt đầu
     *     hay chưa ra trang chọn ghế.
     * @return Tên trang seats.
     */
    @GetMapping("/showtimes/{id}/seats")
    public String seats(@PathVariable Long id, Model model) {
        Showtime showtime = showtimes.findById(id).orElseThrow(BusinessException::missing);
        model.addAttribute("showtime", showtime);
        model.addAttribute(
                "movie",
                movies.findById(showtime.getMovieId()).orElseThrow(BusinessException::missing));
        model.addAttribute("requestKey", UUID.randomUUID().toString());
        model.addAttribute("started", !clock.now().isBefore(showtime.getStartsAt()));
        return "seats";
    }
}
