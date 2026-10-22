import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../lib/auth';
import { API_BASE, request } from '../lib/api';
import { readSession, useMutation, useNow, useResource, writeSession } from '../lib/hooks';
import { dateTime, groups, money } from '../lib/format';
import { Back, Countdown, ErrorBox, Loading, RetryMutation } from '../components/Common';

export default function Seats() {
  const { id } = useParams();
  const { user, loading: authLoading } = useAuth();
  const navigate = useNavigate();
  const now = useNow();
  const shows = useResource('/api/showtimes');
  const inventory = useResource(`/api/showtimes/${encodeURIComponent(id)}/seats`);
  const holdKey = `cinema:hold:${user?.accountId || 'guest'}:${id}`;
  const [hold, setHold] = useState(null);
  const [restoring, setRestoring] = useState(true);
  const [restoreError, setRestoreError] = useState(null);
  const [message, setMessage] = useState(null);
  const [connected, setConnected] = useState(false);
  const holdMutation = useMutation(`${user?.accountId || 'guest'}:hold:${id}`);
  const checkout = useMutation(`${user?.accountId || 'guest'}:checkout:${id}`);
  const [contact, setContact] = useState({ contactName: user?.fullName || '', contactEmail: user?.email || '', contactPhone: '', note: '' });
  const restoringRef = useRef(false);
  const show = (shows.data || []).find(s => s.showtimeId === id) || hold?.showtime;
  const expired = hold?.status === 'ACTIVE' && new Date(hold.expiresAt).getTime() <= now;
  const active = hold?.status === 'ACTIVE' && !expired;
  const selected = active ? hold.seats || [] : [];
  const locked = holdMutation.busy || checkout.busy || restoring || Boolean(holdMutation.pending || checkout.pending || restoreError);
  const started = show && new Date(show.startsAt).getTime() <= now;
  const atCheckout = ['CHECKOUT', 'PAYMENT_PROCESSING', 'CONSUMED'].includes(hold?.status) && hold?.bookingId;
  const subtotal = selected.reduce((sum, seat) => sum + Number(seat.basePrice) + Number(seat.vipSurcharge), 0);
  const percent = user?.customerType === 'STUDENT' ? 10 : user?.customerType === 'MEMBER' ? 15 : 0;
  const total = Math.round(subtotal * (100 - percent) / 100);

  const acceptHold = useCallback(value => {
    const keep = ['ACTIVE', 'CHECKOUT', 'PAYMENT_PROCESSING'].includes(value.status);
    setHold(keep ? value : null);
    writeSession(holdKey, keep ? value.holdId : null);
    setRestoreError(null);
    inventory.reload();
  }, [holdKey, inventory.reload]);

  const restore = useCallback(async () => {
    const savedId = readSession(holdKey);
    if (!savedId || !user || restoringRef.current) { setRestoring(false); return; }
    restoringRef.current = true;
    setRestoring(true);
    try { acceptHold(await request(`/api/holds/${savedId}`)); }
    catch (e) {
      if (e.status === 404) { writeSession(holdKey, null); setHold(null); }
      else setRestoreError(e);
    } finally { setRestoring(false); restoringRef.current = false; }
  }, [holdKey, user, acceptHold]);

  useEffect(() => { if (!authLoading) restore(); }, [authLoading, restore]);
  useEffect(() => {
    if (!expired) return;
    restore();
    inventory.reload();
  }, [expired, restore, inventory.reload]);
  useEffect(() => {
    const stream = new EventSource(`${API_BASE}/api/showtimes/${encodeURIComponent(id)}/seat-events`, { withCredentials: true });
    stream.addEventListener('ready', () => { setConnected(true); inventory.reload(); });
    stream.addEventListener('seats-changed', inventory.reload);
    stream.onopen = () => setConnected(true);
    stream.onerror = () => setConnected(false);
    const fallback = setInterval(inventory.reload, 15000);
    const focus = () => { inventory.reload(); if (!holdMutation.busy && !checkout.busy) restore(); };
    window.addEventListener('focus', focus);
    return () => { stream.close(); clearInterval(fallback); window.removeEventListener('focus', focus); };
  }, [id, inventory.reload, restore, holdMutation.busy, checkout.busy]);

  async function toggle(seat) {
    if (locked || started || atCheckout || !user || user.role !== 'USER') return;
    const nos = selected.map(s => s.seatNo);
    const next = nos.includes(seat.seatNo) ? nos.filter(no => no !== seat.seatNo) : [...nos, seat.seatNo];
    if (next.length > 6) { setMessage(new Error('Mỗi lần đặt tối đa 6 ghế cùng suất chiếu.')); return; }
    setMessage(null);
    const operation = !active
      ? { path: '/api/holds', method: 'POST', body: { showtimeId: id, seatNos: next } }
      : { path: `/api/holds/${hold.holdId}${next.length ? '/seats' : ''}`, method: next.length ? 'PUT' : 'DELETE', body: { token: hold.token, revision: hold.revision, ...(next.length ? { seatNos: next } : {}) } };
    const value = await holdMutation.run(operation);
    if (value) acceptHold(value);
    else { inventory.reload(); await restore(); }
  }
  async function release() {
    const value = await holdMutation.run({ path: `/api/holds/${hold.holdId}`, method: 'DELETE', body: { token: hold.token, revision: hold.revision } });
    if (value) acceptHold(value);
    else restore();
  }
  function goToBooking(value) {
    navigate(`/bookings/${value.bookingId}/payment`);
  }
  async function submit(event) {
    event.preventDefault();
    if (locked || !active || !selected.length || started) return;
    const value = await checkout.run({ path: '/api/bookings', method: 'POST', body: { holdId: hold.holdId, token: hold.token, revision: hold.revision, ...contact, contactName: contact.contactName.trim(), contactEmail: contact.contactEmail.trim(), contactPhone: contact.contactPhone.trim() } });
    if (value) goToBooking(value);
    else restore();
  }
  const update = event => setContact({ ...contact, [event.target.name]: event.target.value });
  return <>
    <Back />
    <div className="section-title mt-0"><div><p className="eyebrow">CHỌN CHỖ NGỒI CỦA BẠN</p><h1 className="page-title">{show?.title || 'Chọn ghế'}</h1><p className="muted">{dateTime(show?.startsAt)} · Phòng 01{show ? ` · ${show.durationMinutes} phút` : ''}</p></div><span className={`connection-status ${connected ? 'connected' : ''}`} role="status">{connected ? '● Ghế được cập nhật trực tiếp' : '○ Đang kết nối lại · cập nhật mỗi 15 giây'}</span></div>
    <ErrorBox error={shows.error} retry={shows.reload} />
    <ErrorBox error={inventory.error} retry={inventory.reload} />
    <ErrorBox error={restoreError} retry={restore} />
    <ErrorBox error={message} />
    <RetryMutation mutation={holdMutation} onSuccess={acceptHold} />
    <RetryMutation mutation={checkout} onSuccess={goToBooking} />
    {started && <div className="alert alert-warning">Suất chiếu đã bắt đầu, không nhận đặt vé mới.</div>}
    {expired && <div className="alert alert-warning">Phiên giữ ghế đã hết hạn. Chọn lại ghế còn trống để tiếp tục.</div>}
    <div className="row g-4"><section className="col-lg-8"><div className="surface auditorium"><div className="screen">MÀN HÌNH</div>
      {inventory.loading && !inventory.data ? <Loading text="Đang tải sơ đồ ghế…" /> : <div className="seat-grid" aria-label="Sơ đồ 30 ghế, A đến E, mỗi hàng 6 ghế" aria-busy={holdMutation.busy}>
        {(inventory.data || []).map(seat => {
          const mine = selected.some(s => s.seatNo === seat.seatNo);
          const vip = seat.seatType === 'VIP';
          const state = mine ? 'mine' : seat.status === 'DISABLED' ? 'disabled' : seat.status === 'SOLD' ? 'booked' : seat.status === 'HELD' ? (seat.heldByMe ? 'mine' : 'held') : vip ? 'vip' : '';
          const label = seat.status === 'DISABLED' ? 'Không mở bán' : seat.status === 'SOLD' ? 'Đã bán' : seat.status === 'HELD' ? (mine ? 'Bạn đang giữ' : seat.heldByMe ? 'Bạn đang giữ trong phiên khác' : 'Người khác đang giữ') : 'Còn trống';
          return <button type="button" key={seat.seatNo} className={`seat-button ${state}`} aria-label={`Ghế ${seat.seatNo}, ${vip ? 'VIP' : 'thường'}, ${label}`} aria-pressed={mine} title={`${seat.seatNo} · ${vip ? 'VIP' : 'Thường'} · ${label} · ${money(Number(seat.basePrice) + Number(seat.vipSurcharge))}`} disabled={locked || started || atCheckout || !user || user.role !== 'USER' || (!mine && seat.status !== 'AVAILABLE')} onClick={() => toggle(seat)}><span className="seat-number">{seat.seatNo}</span>{vip && <span className="seat-type-label" aria-hidden="true">VIP</span>}</button>;
        })}
      </div>}
      <div className="seat-legend">{[['', 'Thường còn trống'], ['vip', 'VIP còn trống'], ['mine', 'Bạn đang giữ'], ['held', 'Người khác giữ'], ['booked', 'Đã bán'], ['disabled', 'Không mở bán']].map(([cls, label]) => <span key={label}><i className={`legend-dot ${cls}`} aria-hidden="true" />{label}</span>)}</div><p className="muted text-center small mt-3 mb-0">A–C: ghế thường · D–E: ghế VIP · Tối đa 6 ghế</p>
    </div></section>
    <aside className="col-lg-4"><div className="surface booking-summary"><p className="eyebrow">LỰA CHỌN CỦA BẠN</p><h3 aria-live="polite">{selected.length ? selected.map(s => s.seatNo).join(', ') : atCheckout ? 'Đã tạo đơn' : 'Chưa chọn ghế'}</h3>
      {active ? <Countdown expiresAt={hold.expiresAt} /> : <p className="hold-clock">{restoring ? 'Đang kiểm tra phiên giữ ghế…' : 'Giữ ghế tối đa 5 phút'}</p>}
      <div className="price-row"><span>Tạm tính</span><strong>{money(subtotal)}</strong></div><div className="price-row"><span>Ưu đãi {user?.customerType ? `(${groups[user.customerType]} ${percent}%)` : ''}</span><span>−{money(subtotal - total)}</span></div><div className="price-row total"><span>Thành tiền dự kiến</span><strong>{money(total)}</strong></div>
      <p className="muted small">{show && <>Ghế thường {money(show.basePrice)} · VIP {money(Number(show.basePrice) + Number(show.vipSurcharge))}. </>}Giá chính thức được xác nhận khi tạo đơn.</p>
      {!user && !authLoading && <><p className="muted">Đăng nhập để giữ ghế và đặt vé.</p><Link className="btn btn-primary w-100" to="/login" state={{ from: `/showtimes/${id}/seats` }}>Đăng nhập</Link></>}
      {user?.role === 'ADMIN' && <p className="muted">Dùng tài khoản khách hàng để đặt vé.</p>}
      {atCheckout && <Link className="btn btn-primary w-100" to={`/bookings/${hold.bookingId}/payment`}>Xem đơn &amp; tiếp tục thanh toán →</Link>}
      {user?.role === 'USER' && !atCheckout && <form onSubmit={submit}><fieldset disabled={locked || started}>
        <label className="form-label" htmlFor="contactName">Tên người nhận</label><input className="form-control mb-3" id="contactName" name="contactName" autoComplete="name" value={contact.contactName} onChange={update} maxLength={120} required />
        <label className="form-label" htmlFor="contactEmail">Email liên hệ</label><input className="form-control mb-3" id="contactEmail" name="contactEmail" type="email" autoComplete="email" value={contact.contactEmail} onChange={update} maxLength={254} required />
        <label className="form-label" htmlFor="contactPhone">Điện thoại <span className="muted">(tùy chọn)</span></label><input className="form-control mb-3" id="contactPhone" name="contactPhone" type="tel" autoComplete="tel" value={contact.contactPhone} onChange={update} maxLength={30} />
        <label className="form-label" htmlFor="note">Ghi chú <span className="muted">(tùy chọn)</span></label><textarea className="form-control mb-3" rows={2} id="note" name="note" value={contact.note} onChange={update} maxLength={1000} />
        <button className="btn btn-primary w-100" disabled={!active || !selected.length || inventory.loading}>{checkout.busy ? 'Đang tạo đơn…' : holdMutation.busy ? 'Đang xác nhận ghế…' : 'Tạo đơn & thanh toán →'}</button><button type="button" className="btn btn-link w-100 mt-2" disabled={!active || !selected.length} onClick={release}>Trả toàn bộ ghế</button>
      </fieldset></form>}
    </div></aside></div>
  </>;
}
