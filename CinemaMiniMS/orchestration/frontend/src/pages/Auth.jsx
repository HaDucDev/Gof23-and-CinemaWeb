import React, { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../lib/auth';
import { request } from '../lib/api';
import { ErrorBox } from '../components/Common';

export default function Auth({ register = false }) {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  if (user) return <Navigate to={user.role === 'ADMIN' ? '/admin/showtimes' : '/movies'} replace />;
  async function submit(event) {
    event.preventDefault();
    if (busy) return;
    const values = Object.fromEntries(new FormData(event.currentTarget));
    setError(null);
    if (register && values.password !== values.confirmPassword) { setError(new Error('Mật khẩu nhập lại không khớp.')); return; }
    if (register && new TextEncoder().encode(values.password).length > 72) { setError(new Error('Mật khẩu tối đa 72 byte UTF-8. Vui lòng dùng mật khẩu ngắn hơn.')); return; }
    setBusy(true);
    try {
      if (register) {
        await request('/api/auth/register', { method: 'POST', body: { email: values.email.trim(), password: values.password, fullName: values.fullName.trim() } });
        navigate('/login', { state: { message: 'Đăng ký thành công. Đăng nhập để bắt đầu đặt vé.', from: location.state?.from }, replace: true });
      } else {
        const profile = await login(values.email.trim(), values.password);
        const from = location.state?.from;
        navigate(profile.role === 'ADMIN' ? '/admin/showtimes' : (from?.startsWith('/') && !from.startsWith('//') ? from : '/movies'), { replace: true });
      }
    } catch (e) { setError(e); } finally { setBusy(false); }
  }
  return <div className="auth-wrap mx-auto">
    <section className="auth-story"><p className="eyebrow">CINEMAMINI · CHÀO BẠN</p><h1>{register ? <>Thêm một người bạn,<br />thêm một câu chuyện.</> : <>Cuộc hẹn điện ảnh<br />bắt đầu từ đây.</>}</h1><p>{register ? 'Tạo tài khoản để giữ chỗ, đặt vé và lưu lại những cuộc hẹn ở rạp.' : 'Đăng nhập để chọn chỗ ngồi yêu thích và tìm lại những tấm vé của bạn.'}</p><div className="auth-illustration" aria-hidden="true">✦<span>Một rạp nhỏ.<br />Nhiều câu chuyện lớn.</span></div></section>
    <form className="surface auth-card" onSubmit={submit}>
      <h2 className="mb-4">{register ? 'Tạo tài khoản' : 'Chào mừng trở lại'}</h2>
      {location.state?.message && <div className="alert alert-success" role="status">{location.state.message}</div>}
      <ErrorBox error={error} />
      <fieldset disabled={busy}>
        {register && <><label className="form-label" htmlFor="fullName">Họ và tên</label><input className="form-control mb-3" id="fullName" name="fullName" autoComplete="name" maxLength={120} required /></>}
        <label className="form-label" htmlFor="email">Email</label><input className="form-control mb-3" id="email" name="email" type="email" autoComplete="email" maxLength={254} required />
        <label className="form-label" htmlFor="password">Mật khẩu</label><input className="form-control mb-3" id="password" name="password" type={showPassword ? 'text' : 'password'} autoComplete={register ? 'new-password' : 'current-password'} minLength={register ? 8 : undefined} maxLength={register ? 72 : undefined} required />
        {register && <p className="small muted">Ít nhất 8 ký tự, tối đa 72 byte UTF-8.</p>}
        {register && <><label className="form-label" htmlFor="confirmPassword">Nhập lại mật khẩu</label><input className="form-control mb-3" id="confirmPassword" name="confirmPassword" type={showPassword ? 'text' : 'password'} autoComplete="new-password" minLength={8} maxLength={72} required /></>}
        <div className="form-check mb-4"><input className="form-check-input" id="show-password" type="checkbox" checked={showPassword} onChange={event => setShowPassword(event.target.checked)} aria-controls={register ? 'password confirmPassword' : 'password'} autoComplete="off" /><label className="form-check-label" htmlFor="show-password">Hiển thị mật khẩu</label></div>
        <button className="btn btn-primary w-100 mt-2">{busy ? 'Đang xử lý…' : register ? 'Đăng ký' : 'Đăng nhập'}</button>
      </fieldset>
      <p className="small muted mt-4 mb-0">{register ? 'Bạn đã có tài khoản? ' : 'Bạn chưa có tài khoản? '}<Link to={register ? '/login' : '/register'} state={{ from: location.state?.from }}>{register ? 'Đăng nhập' : 'Đăng ký ngay'}</Link></p>
    </form>
  </div>;
}
