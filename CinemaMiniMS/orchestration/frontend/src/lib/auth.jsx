import React, { createContext, useContext, useEffect, useState } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { request, resetCsrf } from './api';
import { Loading } from '../components/Common';

const AuthContext = createContext(null);
export const useAuth = () => useContext(AuthContext);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  useEffect(() => {
    const controller = new AbortController();
    request('/api/auth/me', { signal: controller.signal }).then(setUser).catch(e => {
      if (e.name !== 'AbortError' && e.status !== 401) setError(e);
    }).finally(() => { if (!controller.signal.aborted) setLoading(false); });
    const expired = () => { setUser(null); resetCsrf(); };
    window.addEventListener('cinema:session-expired', expired);
    return () => { controller.abort(); window.removeEventListener('cinema:session-expired', expired); };
  }, []);
  async function login(email, password) {
    const profile = await request('/api/auth/login', { method: 'POST', body: { email, password } });
    resetCsrf();
    setUser(profile);
    setError(null);
    return profile;
  }
  async function logout() {
    await request('/api/auth/logout', { method: 'POST' });
    resetCsrf();
    setUser(null);
  }
  return <AuthContext.Provider value={{ user, loading, error, login, logout }}>{children}</AuthContext.Provider>;
}

export function Protected({ admin = false, children }) {
  const { user, loading } = useAuth();
  const location = useLocation();
  if (loading) return <Loading />;
  if (!user) return <Navigate to="/login" state={{ from: location.pathname }} replace />;
  if (admin && user.role !== 'ADMIN') return <Navigate to="/movies" replace />;
  if (!admin && user.role === 'ADMIN') return <Navigate to="/admin/showtimes" replace />;
  return children;
}
