import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useNow, useResource } from '../lib/hooks';
import { day, money, time } from '../lib/format';
import { Empty, ErrorBox, Loading } from '../components/Common';

export default function Movies() {
  const [page, setPage] = useState(1);
  const now = useNow();
  const movies = useResource('/api/movies');
  const shows = useResource('/api/showtimes');
  useEffect(() => { document.title = 'Phim & suất chiếu · CinemaMini'; }, []);
  const movieList = movies.data || [];
  const pages = Math.ceil(movieList.length / 6);
  const cards = movieList.slice((page - 1) * 6, page * 6);
  return <>
    <section className="hero"><div><p className="eyebrow">RẠP NHỎ · TRẢI NGHIỆM TRỌN VẸN</p><h1>Mỗi bộ phim,<br />một cuộc hẹn mới.</h1><p>Chọn câu chuyện bạn yêu thích.<br />Chúng mình giữ cho bạn một chỗ thật vừa ý.</p></div><div className="hero-art" aria-hidden="true"><div className="orbit" /><div className="ticket-shape">CINEMA<br /><span>MINI</span><hr />PHÒNG 01 / 30 GHẾ</div><span className="spark">✦</span></div></section>
    <div className="section-title" id="movie-list"><div><p className="eyebrow">ĐANG MỞ BÁN</p><h2>Hẹn gặp bạn ở rạp</h2></div><span className="muted">Giờ Việt Nam · Asia/Ho_Chi_Minh</span></div>
    <ErrorBox error={movies.error || shows.error} retry={() => { movies.reload(); shows.reload(); }} />
    {movies.loading ? <Loading /> : movies.data && !movieList.length ? <Empty title="Chưa có phim">Chưa có dữ liệu phim.</Empty> : <div className="row g-4">
      {cards.map((movie, index) => {
        const times = (shows.data || []).filter(show => show.movieId === movie.movieId && new Date(show.startsAt).getTime() > now);
        return <article className="col-lg-4 col-md-6" key={movie.movieId}><div className="movie-card"><div className={`movie-art art-${((page - 1) * 6 + index) % 3}`}><span className="movie-index">{String((page - 1) * 6 + index + 1).padStart(2, '0')}</span><div className="poster-shape" aria-hidden="true" /><div className="poster-title">{movie.title}</div><small>CINEMAMINI ORIGINAL STORIES</small></div><div className="movie-body"><div className="d-flex justify-content-between"><span className="eyebrow">PHIM TRUYỆN</span><span className="muted small">{movie.durationMinutes} phút</span></div><h3>{movie.title}</h3><p className="movie-description">{movie.description}</p><div className="showtime-list">{shows.loading ? <span className="muted small">Đang tải suất chiếu…</span> : shows.error ? <span className="muted small">Chưa tải được lịch chiếu.</span> : times.length ? times.map(show => <Link className="showtime-chip" to={`/showtimes/${encodeURIComponent(show.showtimeId)}/seats`} key={show.showtimeId}><strong>{time(show.startsAt)}</strong><small>{day(show.startsAt)} · đến {time(show.endsAt)}</small><small>Từ {money(show.basePrice)}</small></Link>) : <p className="muted small mb-0">Chưa có suất chiếu sắp tới.</p>}</div></div></div></article>;
      })}
    </div>}
    {pages > 1 && <nav className="movie-pagination" aria-label="Phân trang phim"><p className="muted">Trang {page} / {pages} · {movieList.length} bộ phim</p><div className="d-flex justify-content-center gap-2"><button className="btn btn-sm btn-outline-dark" disabled={page === 1} onClick={() => setPage(page - 1)}>Trước</button>{Array.from({ length: pages }, (_, i) => <button key={i} className={`btn btn-sm ${page === i + 1 ? 'btn-primary' : 'btn-outline-dark'}`} aria-current={page === i + 1 ? 'page' : undefined} onClick={() => setPage(i + 1)}>{i + 1}</button>)}<button className="btn btn-sm btn-outline-dark" disabled={page === pages} onClick={() => setPage(page + 1)}>Sau</button></div></nav>}
  </>;
}
