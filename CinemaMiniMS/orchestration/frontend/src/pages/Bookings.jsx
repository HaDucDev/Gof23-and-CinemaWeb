import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useResource } from '../lib/hooks';
import { dateTime, money } from '../lib/format';
import { Badge, Empty, ErrorBox, Loading, Title } from '../components/Common';

export default function Bookings({ admin = false }) {
  const resource = useResource(admin ? '/api/admin/bookings' : '/api/bookings');
  const [filter, setFilter] = useState('');
  useEffect(() => {
    window.addEventListener('focus', resource.reload);
    return () => window.removeEventListener('focus', resource.reload);
  }, [resource.reload]);
  const rows = (resource.data || []).filter(row => `${row.movieTitle} ${row.bookingId} ${row.ticketCode || ''} ${row.contactName || ''}`.toLowerCase().includes(filter.toLowerCase()));
  return <>
    <Title eyebrow={admin ? 'QUẢN TRỊ CINEMAMINI' : 'NHỮNG CUỘC HẸN CỦA BẠN'} action={<Link className="btn btn-primary" to={admin ? '/admin/payments' : '/movies'}>{admin ? 'Xem giao dịch' : 'Đặt vé mới'}</Link>}>{admin ? 'Quản lý đơn vé' : 'Vé của tôi'}</Title>
    <div className="d-flex justify-content-between flex-wrap gap-2 mb-4"><label className="visually-hidden" htmlFor="booking-filter">Tìm theo phim, mã đơn hoặc mã vé</label><input className="form-control filter-input" id="booking-filter" type="search" placeholder="Tìm phim, mã đơn, mã vé…" value={filter} onChange={e => setFilter(e.target.value)} /><button className="btn btn-outline-secondary btn-sm" disabled={resource.loading} onClick={resource.reload}>Làm mới</button></div>
    <ErrorBox error={resource.error} retry={resource.reload} />
    {resource.loading && !resource.data ? <Loading /> : resource.data && !rows.length ? <Empty title={filter ? 'Không tìm thấy vé phù hợp' : 'Chưa có cuộc hẹn nào'}>{filter ? 'Thử tìm bằng tên phim hoặc mã vé khác.' : <><p>Một câu chuyện mới đang chờ bạn ở rạp.</p><Link to="/movies">Khám phá phim →</Link></>}</Empty> : <div className="row g-4">{rows.map(row => <div className="col-md-6" key={row.bookingId}><article className="surface p-4 h-100"><div className="d-flex justify-content-between flex-wrap gap-2 mb-3"><span className="eyebrow booking-code mb-0" title={row.bookingId}>{row.ticketCode || row.bookingId}</span><Badge status={row.status} /></div><h3>{row.movieTitle}</h3><p className="muted">{dateTime(row.startsAt)} · Phòng 01</p>{admin && <p className="small muted">Tài khoản #{row.accountId}</p>}<div className="d-flex justify-content-between align-items-center gap-3"><strong>{money(row.amount)}</strong><Link className="btn btn-outline-dark btn-sm" to={`${admin ? '/admin' : ''}/bookings/${row.bookingId}`}>Xem chi tiết →</Link></div></article></div>)}</div>}
  </>;
}
