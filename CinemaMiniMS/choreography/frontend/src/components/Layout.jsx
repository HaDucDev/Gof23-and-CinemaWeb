import React, { useEffect, useRef, useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/auth';
import { request, socketUrl } from '../lib/api';
import { dateTime } from '../lib/format';
import { ErrorBox } from './Common';

function Notifications() {
  const { user } = useAuth();
  const [data, setData] = useState({ items: [], unreadCount: 0 });
  const [error, setError] = useState(null);
  const [busyId, setBusyId] = useState(null);
  const panel = useRef(null);
  useEffect(() => {
    let active = true, socket, retryTimer, reconnectDelay = 1000;
    const controller = new AbortController();
    const refresh = () => request('/api/notifications', { signal: controller.signal }).then(value => {
      if (active) { setData(value); setError(null); }
    }).catch(e => { if (active) setError(e); });
    function connect() {
      if (!active) return;
      socket = new WebSocket(socketUrl());
      socket.onopen = () => { reconnectDelay = 1000; refresh(); };
      socket.onmessage = refresh;
      socket.onerror = () => socket.close();
      socket.onclose = () => {
        if (active) { retryTimer = setTimeout(connect, reconnectDelay); reconnectDelay = Math.min(reconnectDelay * 2, 30000); }
      };
    }
    refresh();
    connect();
    const poll = setInterval(refresh, 30000);
    window.addEventListener('focus', refresh);
    return () => { active = false; controller.abort(); clearInterval(poll); clearTimeout(retryTimer); socket?.close(); window.removeEventListener('focus', refresh); };
  }, [user.accountId]);
  async function read(item) {
    setBusyId(item.notificationId);
    try {
      await request(`/api/notifications/${item.notificationId}/read`, { method: 'POST' });
      setData(await request('/api/notifications'));
    } catch (e) { setError(e); }
    finally { setBusyId(null); }
  }
  return <details className="notification-panel" ref={panel}>
    <summary>Thông báo <span className="notification-count">{data.unreadCount}</span></summary>
    <div className="notification-dropdown"><strong>Thông báo mới nhất</strong><ErrorBox error={error} />
      {!data.items.length && <p className="muted mb-0">Chưa có thông báo.</p>}
      {data.items.map(item => <div className={`notification-item ${item.readAt ? '' : 'unread'}`} key={item.notificationId}>
        <Link to={`${user.role === 'ADMIN' ? '/admin' : ''}/bookings/${item.bookingId}`} onClick={() => { panel.current.open = false; if (!item.readAt) read(item); }}>{item.content}</Link>
        <small>{dateTime(item.createdAt)}</small>
        {!item.readAt && <button disabled={busyId === item.notificationId} onClick={() => read(item)}>Đánh dấu đã đọc</button>}
      </div>)}
    </div>
  </details>;
}

export default function Layout({ children }) {
  const { user, logout, loading } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  async function signOut() {
    setBusy(true);
    try { await logout(); navigate('/movies'); setError(null); } catch (e) { setError(e); }
    finally { setBusy(false); }
  }
  return <>
    <a href="#main" className="skip-link">Đến nội dung chính</a>
    <header className="site-header"><nav className="container nav-bar" aria-label="Điều hướng chính">
      <Link to="/movies" className="brand"><span className="brand-mark">C<span>m</span></span><span>CinemaMini<small>Một chỗ ngồi. Một câu chuyện.</small></span></Link>
      <div className="nav-links">
        <NavLink to="/movies">Phim &amp; suất chiếu</NavLink>
        {user?.role === 'USER' && <NavLink to="/bookings">Vé của tôi</NavLink>}
        {user?.role === 'ADMIN' && <><NavLink to="/admin/showtimes">Quản lý suất</NavLink><NavLink to="/admin/payments">Giao dịch</NavLink></>}
        {user && <><Notifications key={user.accountId} /><span className="account-name">{user.fullName || (user.role === 'ADMIN' ? 'Admin' : user.email)}</span><button className="btn btn-sm btn-outline-secondary" disabled={busy} onClick={signOut}>{busy ? 'Đang thoát…' : 'Đăng xuất'}</button></>}
        {!user && !loading && <><NavLink className="btn btn-sm btn-outline-dark" to="/login">Đăng nhập</NavLink><NavLink className="btn btn-sm btn-primary" to="/register">Đăng ký</NavLink></>}
      </div>
    </nav></header>
    <main className="container page-space" id="main"><ErrorBox error={error} />{children}</main>
    <footer className="site-footer"><div className="container d-flex justify-content-between flex-wrap gap-2"><span>CinemaMini <span className="muted">/ Phòng 01</span></span><span className="muted">30 chỗ ngồi dành cho những câu chuyện lớn.</span></div></footer>
  </>;
}
