import React, { useEffect, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useAuth } from '../lib/auth';
import { useMutation, useNow, useResource } from '../lib/hooks';
import { dateTime, groups, methods, money, processing } from '../lib/format';
import { Back, Badge, Countdown, ErrorBox, Loading, RetryMutation, Title } from '../components/Common';

export function LedgerTable({ rows = [], admin = false }) {
  if (!rows.length) return <p className="muted small mb-0">Chưa phát sinh khoản thu hoặc hoàn tiền.</p>;
  return <div className="table-responsive"><table className="table">
    <thead><tr><th>Thời gian</th>{admin && <th>Đơn vé</th>}<th>Loại</th><th>Phương thức</th><th>Số tiền</th><th>ID / Mã tham chiếu</th><th>Giao dịch gốc</th></tr></thead>
    <tbody>{rows.map(row => <tr key={row.transactionId}>
      <td className="text-nowrap">{dateTime(row.createdAt)}</td>
      {admin && <td><Link className="booking-code" title={row.bookingId} to={`/admin/bookings/${row.bookingId}`}>{row.bookingId.slice(0, 8)}…</Link></td>}
      <td>{row.operationType === 'REFUND' ? 'Hoàn tiền' : 'Thu tiền'}</td>
      <td>{methods[row.paymentMethod] || row.paymentMethod}</td>
      <td className={Number(row.amount) < 0 ? 'text-danger text-nowrap' : 'text-nowrap'}>{money(row.amount)}</td>
      <td><span className="reference-code d-block">{row.referenceCode || row.transactionId}</span>{row.referenceCode && <small className="reference-code d-block muted">{row.transactionId}</small>}</td>
      <td><span className="reference-code">{row.originalTransactionId || '—'}</span></td>
    </tr>)}</tbody>
  </table></div>;
}

function Timeline({ resource, saga }) {
  const operations = { PAY: 'Thanh toán', CANCEL_PAID: 'Hủy vé và hoàn tiền', CANCEL_UNPAID: 'Hủy đơn chưa thanh toán' };
  return <details className="surface p-4 mt-4 saga-panel" open><summary><strong>Tiến trình xử lý Saga</strong>{saga?.operationType && <span className="small muted">{operations[saga.operationType] || saga.operationType}</span>}</summary>
    {saga && <dl className="row small mt-3 mb-0">
      <dt className="col-sm-4">Thanh toán</dt><dd className="col-sm-8">{saga.paymentStatus ? <Badge status={saga.paymentStatus} /> : 'Chờ xác nhận'}</dd>
      <dt className="col-sm-4">Ghế</dt><dd className="col-sm-8">{saga.seatStatus ? <Badge status={saga.seatStatus} /> : 'Chờ xác nhận'}</dd>
      <dt className="col-sm-4">Sự kiện gần nhất</dt><dd className="col-sm-8">{saga.lastEvent || 'Chưa nhận sự kiện'}</dd>
    </dl>}
    <ErrorBox error={resource.error} retry={resource.reload} />
    {resource.loading && !resource.data ? <Loading /> : !resource.data?.length ? <p className="muted small mt-3 mb-0">Chưa có hoạt động.</p> : <ol className="saga-timeline">{resource.data.map((step, index) => <li key={`${step.happenedAt}-${index}`}><div className="d-flex justify-content-between flex-wrap gap-2"><strong>{step.serviceName} · {step.action}</strong><small className="muted">{dateTime(step.happenedAt)}</small></div><span className="small">{step.result}</span>{step.detail && <pre className="timeline-detail">{typeof step.detail === 'string' ? step.detail : JSON.stringify(step.detail, null, 2)}</pre>}</li>)}</ol>}
  </details>;
}

export default function BookingDetail({ paymentPage = false, admin = false }) {
  const { id } = useParams();
  const { user } = useAuth();
  const now = useNow();
  const path = `${admin ? '/api/admin' : '/api'}/bookings/${id}`;
  const detail = useResource(path);
  const timeline = useResource(admin ? `${path}/timeline` : null);
  const mutation = useMutation(`${user.accountId}:booking:${id}`);
  const booking = detail.data;
  const [method, setMethod] = useState('');
  const [confirmCancel, setConfirmCancel] = useState(false);
  const [reason, setReason] = useState('');
  const expired = booking && new Date(booking.expiresAt).getTime() <= now;
  const inProgress = processing(booking?.status);
  const locked = mutation.busy || Boolean(mutation.pending) || inProgress;
  const expiryRef = useRef(false);
  const refresh = () => { detail.reload(); timeline.reload(); };
  useEffect(() => {
    const refreshData = () => { detail.reload(); timeline.reload(); };
    const pollMs = inProgress ? 1500 : booking?.status === 'PENDING' ? 5000 : 0;
    const poll = pollMs ? setInterval(refreshData, pollMs) : null;
    window.addEventListener('focus', refreshData);
    return () => { clearInterval(poll); window.removeEventListener('focus', refreshData); };
  }, [booking?.status, inProgress, detail.reload, timeline.reload]);
  useEffect(() => {
    if (expired && booking?.status === 'PENDING' && !expiryRef.current) { expiryRef.current = true; detail.reload(); timeline.reload(); }
  }, [expired, booking?.status, detail.reload, timeline.reload]);
  async function pay(event) {
    event.preventDefault();
    if (!method || locked || expired || booking.status !== 'PENDING') return;
    const value = await mutation.run({ path: `/api/bookings/${id}/payments`, method: 'POST', body: { paymentMethod: method } });
    if (value) accepted(value); else refresh();
  }
  function accepted(value) {
    detail.setData(current => ({ ...current, ...value }));
    setConfirmCancel(false);
    refresh();
  }
  async function cancel(event) {
    event.preventDefault();
    if (locked) return;
    const value = await mutation.run({ path: `/api/bookings/${id}/cancel`, method: 'POST', body: { reason: reason.trim() || 'Khách hủy vé' } });
    if (value) accepted(value); else refresh();
  }
  if (!booking) return <><Back to={admin ? '/admin/bookings' : '/bookings'}>{admin ? 'Quản lý đơn vé' : 'Vé của tôi'}</Back><ErrorBox error={detail.error} retry={detail.reload} />{detail.loading && <Loading />}</>;
  const canCancel = !admin && !locked && (booking.status === 'PENDING' || (booking.status === 'CONFIRMED' && new Date(booking.startsAt).getTime() > now));
  const transactions = booking.payment?.transactions || [];
  const paid = transactions.filter(row => row.operationType === 'PAYMENT').reduce((sum, row) => sum + Number(row.amount), 0);
  const refunded = -transactions.filter(row => row.operationType === 'REFUND').reduce((sum, row) => sum + Number(row.amount), 0);
  const charge = booking.payment?.payment;
  const failureMessage = admin ? booking.failureReason : charge?.status === 'FAILED' ? 'Thanh toán không thành công.' : '';
  return <div className={paymentPage ? 'narrow-page mx-auto' : ''}>
    <Back to={admin ? '/admin/bookings' : '/bookings'}>{admin ? 'Quản lý đơn vé' : 'Vé của tôi'}</Back>
    <Title eyebrow={paymentPage ? 'HOÀN TẤT CUỘC HẸN ĐIỆN ẢNH' : 'THÔNG TIN CUỘC HẸN CỦA BẠN'} action={<button className="btn btn-outline-secondary btn-sm" onClick={refresh} disabled={detail.loading}>Làm mới</button>}>{paymentPage && booking.status === 'PENDING' ? 'Thanh toán' : 'Chi tiết vé'}</Title>
    <ErrorBox error={detail.error} retry={detail.reload} />
    <RetryMutation mutation={mutation} onSuccess={accepted} />
    <article className="surface ticket-detail mb-4"><div className="ticket-heading justify-content-between flex-wrap"><div><p className="eyebrow mb-2">CINEMAMINI · PHÒNG 01</p><h2 className="mb-2">{booking.movieTitle}</h2><p className="mb-0">{dateTime(booking.startsAt)}</p></div><Badge status={booking.status} /></div><div className="p-4">
      <p className="small muted mb-2">Mã đơn <span className="booking-code">{booking.bookingId}</span></p>
      {booking.ticketCode && <div className={`ticket-code-block ${booking.status !== 'CONFIRMED' ? 'invalid-ticket' : ''}`}><span className="eyebrow">{booking.status === 'CONFIRMED' ? 'MÃ VÉ CỦA BẠN' : 'MÃ VÉ ĐÃ VÔ HIỆU'}</span><strong>{booking.ticketCode}</strong><small>{booking.status === 'CONFIRMED' ? 'Xuất trình mã vé khi đến rạp.' : 'Vé này không còn giá trị sử dụng.'}</small></div>}
      <div className="ticket-seats mb-4">{(booking.items || []).map(item => <span key={item.seatNoSnapshot}>{item.seatNoSnapshot}<small className="d-block fw-normal muted">{item.seatTypeSnapshot === 'VIP' ? 'VIP' : 'Thường'}</small></span>)}</div>
      <div className="row"><div className="col-md-6"><dl><dt>Người nhận</dt><dd>{booking.contactName}</dd><dt>Liên hệ</dt><dd>{booking.contactEmail}<br />{booking.contactPhone}</dd>{booking.note && <><dt>Ghi chú</dt><dd>{booking.note}</dd></>}</dl></div><div className="col-md-6"><div className="price-row"><span>Tạm tính</span><strong>{money(booking.subtotal)}</strong></div><div className="price-row"><span>{groups[booking.customerType] || booking.customerType} · giảm {booking.discountPercent}%</span><span>−{money(Number(booking.subtotal) - Number(booking.amount))}</span></div><div className="price-row total"><span>Tổng thanh toán</span><strong>{money(booking.amount)}</strong></div></div></div>
      {booking.status === 'PENDING' && <Countdown expiresAt={booking.expiresAt} />}
      {inProgress && <div className="alert alert-info mb-0" role="status"><span className="spinner-border spinner-border-sm me-2" aria-hidden="true" />{booking.status === 'COMPENSATING' ? 'Thanh toán thất bại. Hệ thống đang trả ghế trước khi hủy đơn.' : booking.status === 'CANCELLING' ? 'Đang xử lý hủy vé và hoàn tiền nếu có.' : 'Hệ thống đang xử lý. Kết quả sẽ tự cập nhật.'}</div>}
      {booking.status === 'CONFIRMED' && <div className="alert alert-success mb-0">Đặt vé thành công. Hẹn gặp bạn tại rạp!</div>}
      {['CANCELLED', 'EXPIRED'].includes(booking.status) && <div className="alert alert-secondary mb-0">{booking.status === 'EXPIRED' ? 'Đơn đã hết hạn.' : 'Đơn đã hủy.'} {failureMessage}<Link to="/movies" className="ms-2">Chọn suất chiếu khác →</Link></div>}
    </div></article>
    {!admin && booking.status === 'PENDING' && (paymentPage ? <form className="surface p-4 mb-4" onSubmit={pay}><h3 className="mb-4">Chọn phương thức thanh toán</h3><fieldset disabled={locked || expired}><div className="payment-methods">{Object.entries(methods).map(([value, label]) => <label className="payment-method" key={value}><input type="radio" name="paymentMethod" value={value} checked={method === value} onChange={() => setMethod(value)} required /><span>{label}</span></label>)}</div><button className="btn btn-primary w-100 mt-4" disabled={!method || expired}>{mutation.busy ? 'Đang gửi yêu cầu…' : `Thanh toán ${money(booking.amount)}`}</button></fieldset></form> : <Link className={`btn btn-primary mb-4 ${expired || locked ? 'disabled' : ''}`} aria-disabled={expired || locked} to={`/bookings/${id}/payment`}>Tiếp tục thanh toán →</Link>)}
    {canCancel && !confirmCancel && <button className="btn btn-outline-danger mb-4" onClick={() => setConfirmCancel(true)}>Hủy {booking.status === 'CONFIRMED' ? 'vé & hoàn tiền' : 'đơn'}</button>}
    {confirmCancel && canCancel && <form className="surface p-4 mb-4" onSubmit={cancel}><h3>Xác nhận hủy {booking.status === 'CONFIRMED' ? 'vé' : 'đơn'}</h3><p className="small muted">{booking.status === 'CONFIRMED' ? `Hoàn toàn bộ ${money(booking.amount)} theo phương thức thanh toán ban đầu và trả ghế.` : 'Trả toàn bộ ghế đã giữ. Đơn chưa thanh toán không phát sinh hoàn tiền.'}</p><label className="form-label" htmlFor="reason">Lý do hủy (tùy chọn)</label><input id="reason" className="form-control mb-3" value={reason} onChange={e => setReason(e.target.value)} maxLength={500} /><div className="d-flex gap-2"><button className="btn btn-danger">Xác nhận hủy</button><button type="button" className="btn btn-outline-secondary" onClick={() => setConfirmCancel(false)}>Giữ lại vé</button></div></form>}
    <section className="surface p-4"><h3 className="mb-3">Lịch sử thanh toán</h3>{charge && <p className="small">{methods[charge.paymentMethod]} · <Badge status={charge.status} /></p>}<LedgerTable rows={transactions} />{transactions.length > 0 && <div className="mt-3"><div className="price-row"><span>Đã thanh toán</span><strong>{money(paid)}</strong></div><div className="price-row"><span>Đã hoàn</span><strong>{money(refunded)}</strong></div><div className="price-row total"><span>Số tiền ròng</span><strong>{money(paid - refunded)}</strong></div></div>}</section>
    {admin && <Timeline resource={timeline} saga={booking.saga} />}
  </div>;
}
