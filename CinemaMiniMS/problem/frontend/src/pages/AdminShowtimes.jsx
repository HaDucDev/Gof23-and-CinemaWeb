import React, { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { request } from '../lib/api';
import { useResource } from '../lib/hooks';
import { dateTime, money, time } from '../lib/format';
import { Empty, ErrorBox, Loading, Title } from '../components/Common';

export default function AdminShowtimes() {
  const resource = useResource('/api/admin/showtimes');
  const [form, setForm] = useState({ movieId: '', startsAt: '', basePrice: '', vipSurcharge: '', disabledSeatNos: [] });
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  const [message, setMessage] = useState('');
  const [uncertain, setUncertain] = useState(false);
  const initialized = useRef(false);
  const template = resource.data?.template;
  useEffect(() => {
    if (resource.data && !initialized.current) {
      initialized.current = true;
      setForm(current => ({ ...current, movieId: String(resource.data.movies[0]?.movieId || ''), basePrice: String(resource.data.template.basePrice), vipSurcharge: String(resource.data.template.vipSurcharge) }));
    }
  }, [resource.data]);
  function update(event) { setForm({ ...form, [event.target.name]: event.target.value }); }
  async function submit(event) {
    event.preventDefault();
    if (busy || uncertain) return;
    setBusy(true); setError(null); setMessage('');
    try {
      const value = await request('/api/admin/showtimes/from-template', { method: 'POST', body: { movieId: Number(form.movieId), startsAt: `${form.startsAt}:00+07:00`, basePrice: Number(form.basePrice), vipSurcharge: Number(form.vipSurcharge), disabledSeatNos: form.disabledSeatNos } });
      setMessage(`Đã tạo suất chiếu ${value.title} lúc ${dateTime(value.startsAt)}.`);
      setForm(current => ({ ...current, startsAt: '', disabledSeatNos: [] }));
      resource.reload();
    } catch (e) {
      setError(e);
      if (!e.status || e.status >= 500) { setUncertain(true); resource.reload(); }
    } finally { setBusy(false); }
  }
  return <>
    <Title eyebrow="QUẢN TRỊ CINEMAMINI" action={<Link className="btn btn-outline-dark" to="/admin/bookings">Quản lý đơn vé →</Link>}>Quản lý suất chiếu</Title>
    <ErrorBox error={resource.error} retry={resource.reload} /><ErrorBox error={error} />
    {message && <div className="alert alert-success" role="status">{message}</div>}
    {uncertain && <div className="alert alert-warning">Chưa xác định kết quả tạo suất. Kiểm tra lịch chiếu trước khi gửi yêu cầu mới.<button className="btn btn-sm btn-outline-dark ms-3" disabled={resource.loading || Boolean(resource.error)} onClick={() => { setUncertain(false); setError(null); }}>Tôi đã kiểm tra lịch chiếu</button></div>}
    {resource.loading && !resource.data ? <Loading /> : resource.data && <div className="row g-4"><section className="col-lg-5"><form className="surface p-4" onSubmit={submit}><h3>Tạo suất từ mẫu</h3><div className="template-note"><strong>{template.name}</strong><p className="mb-0 small muted">Giá gốc {money(template.basePrice)} · phụ thu VIP {money(template.vipSurcharge)} · {template.seats.length} ghế</p></div><fieldset disabled={busy || uncertain}>
      <label className="form-label" htmlFor="movieId">Phim</label><select id="movieId" name="movieId" className="form-select mb-3" value={form.movieId} onChange={update} required><option value="" disabled>Chọn phim</option>{resource.data.movies.map(movie => <option key={movie.movieId} value={movie.movieId}>{movie.title}</option>)}</select>
      <label className="form-label" htmlFor="startsAt">Giờ bắt đầu (giờ Việt Nam)</label><input id="startsAt" name="startsAt" type="datetime-local" className="form-control mb-3" value={form.startsAt} onChange={update} required />
      <div className="row"><div className="col-6"><label className="form-label" htmlFor="basePrice">Giá cơ bản (đ)</label><input id="basePrice" name="basePrice" type="number" min="1" max="9999999999999" step="1" className="form-control mb-3" value={form.basePrice} onChange={update} required /></div><div className="col-6"><label className="form-label" htmlFor="vipSurcharge">Phụ thu VIP (đ)</label><input id="vipSurcharge" name="vipSurcharge" type="number" min="0" max="9999999999999" step="1" className="form-control mb-3" value={form.vipSurcharge} onChange={update} required /></div></div>
      <p className="form-label mb-2">Ghế không mở bán trong suất mới</p><div className="admin-seat-list">{template.seats.map(seat => <label key={seat.seatNo}><input type="checkbox" checked={form.disabledSeatNos.includes(seat.seatNo)} onChange={e => setForm({ ...form, disabledSeatNos: e.target.checked ? [...form.disabledSeatNos, seat.seatNo] : form.disabledSeatNos.filter(no => no !== seat.seatNo) })} /><span>{seat.seatNo}</span></label>)}</div><p className="small muted mt-3">Thời gian dọn phòng: {template.cleanupMinutes} phút sau khi phim kết thúc. Hệ thống kiểm tra trùng lịch phòng trước khi tạo.</p><button className="btn btn-primary w-100">{busy ? 'Đang tạo suất…' : 'Tạo suất chiếu'}</button>
    </fieldset></form></section><section className="col-lg-7"><div className="surface p-4"><div className="d-flex justify-content-between gap-2"><h3>Lịch chiếu</h3><button className="btn btn-sm btn-outline-secondary" disabled={resource.loading} onClick={resource.reload}>Làm mới</button></div><div className="table-responsive"><table className="table align-middle mt-3"><thead><tr><th>Phim</th><th>Bắt đầu</th><th>Phòng sẵn sàng</th><th>Giá</th></tr></thead><tbody>{resource.data.showtimes.map(show => <tr key={show.showtimeId}><td><Link to={`/showtimes/${encodeURIComponent(show.showtimeId)}/seats`}>{show.title}</Link></td><td className="text-nowrap">{dateTime(show.startsAt)}<small className="d-block muted">Kết thúc {time(show.endsAt)}</small></td><td>{time(show.roomAvailableAt)}</td><td className="text-nowrap">{money(show.basePrice)}<small className="d-block muted">VIP +{money(show.vipSurcharge)}</small></td></tr>)}</tbody></table></div>{!resource.data.showtimes.length && <Empty title="Chưa có suất chiếu">Tạo suất chiếu đầu tiên từ mẫu bên cạnh.</Empty>}</div></section></div>}
  </>;
}
