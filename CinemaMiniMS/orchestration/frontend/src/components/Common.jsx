import React, { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useNow } from '../lib/hooks';
import { statusLabels } from '../lib/format';

export function Title({ children, eyebrow, action }) {
  useEffect(() => { document.title = `${typeof children === 'string' ? children : 'Đặt vé'} · CinemaMini`; }, [children]);
  return <div className="section-title mt-0"><div><p className="eyebrow">{eyebrow}</p><h1 className="page-title mb-0">{children}</h1></div>{action}</div>;
}
export function ErrorBox({ error, retry }) {
  if (!error) return null;
  return <div className="alert alert-danger" role="alert"><span>{error.message || error}</span>{retry && <button type="button" className="btn btn-sm btn-outline-danger ms-3" onClick={retry}>Thử lại</button>}</div>;
}
export function Loading({ text = 'Đang tải dữ liệu…' }) {
  return <div className="loading-state" role="status"><span className="spinner-border spinner-border-sm" aria-hidden="true" /> {text}</div>;
}
export function Empty({ title = 'Chưa có dữ liệu', children }) {
  return <div className="empty-state"><h3>{title}</h3><div className="muted">{children}</div></div>;
}
export function Badge({ status }) { return <span className={`status-badge ${status}`}>{statusLabels[status] || status}</span>; }
export function Countdown({ expiresAt, onExpire }) {
  const now = useNow();
  const seconds = Math.max(0, Math.ceil((new Date(expiresAt).getTime() - now) / 1000));
  useEffect(() => { if (seconds === 0) onExpire?.(); }, [seconds, onExpire]);
  return <p className={`hold-clock ${seconds < 60 ? 'text-danger' : ''}`} role="timer">{seconds > 0 ? `Thời gian giữ ghế: ${Math.floor(seconds / 60).toString().padStart(2, '0')}:${(seconds % 60).toString().padStart(2, '0')}` : 'Phiên giữ ghế đã hết hạn.'}</p>;
}
export function RetryMutation({ mutation, onSuccess }) {
  return <><ErrorBox error={mutation.error} />{mutation.pending && !mutation.busy && <div className="alert alert-warning small"><p>Chưa xác định được kết quả yêu cầu. Gửi lại cùng yêu cầu để kiểm tra an toàn.</p><button className="btn btn-outline-dark btn-sm" onClick={async () => { const result = await mutation.retry(); if (result) onSuccess(result); }}>Thử lại yêu cầu vừa gửi</button></div>}</>;
}
export function Back({ to = '/movies', children = 'Phim & suất chiếu' }) { return <Link className="back-link d-inline-block mb-4" to={to}>← {children}</Link>; }
