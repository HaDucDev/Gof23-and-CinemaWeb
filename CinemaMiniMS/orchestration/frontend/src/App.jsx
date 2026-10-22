import React, { useEffect } from 'react';
import { Navigate, Route, Routes, useLocation, useParams } from 'react-router-dom';
import { Protected, useAuth } from './lib/auth';
import Layout from './components/Layout';
import { Empty } from './components/Common';
import Movies from './pages/Movies';
import Auth from './pages/Auth';
import Seats from './pages/Seats';
import Bookings from './pages/Bookings';
import BookingDetail from './pages/BookingDetail';
import AdminShowtimes from './pages/AdminShowtimes';
import AdminPayments from './pages/AdminPayments';

function SeatRoute() {
  const { id } = useParams();
  const { user, loading } = useAuth();
  return <Seats key={`${loading ? 'loading' : user?.accountId || 'guest'}:${id}`} />;
}
function DetailRoute(props) {
  const { id } = useParams();
  const { user } = useAuth();
  return <BookingDetail key={`${user.accountId}:${id}:${props.paymentPage || false}:${props.admin || false}`} {...props} />;
}
export default function App() {
  const location = useLocation();
  useEffect(() => { window.scrollTo(0, 0); }, [location.pathname]);
  return <Layout><Routes>
    <Route path="/" element={<Navigate to="/movies" replace />} />
    <Route path="/movies" element={<Movies />} />
    <Route path="/login" element={<Auth key="login" />} />
    <Route path="/register" element={<Auth register key="register" />} />
    <Route path="/showtimes/:id/seats" element={<SeatRoute />} />
    <Route path="/bookings" element={<Protected><Bookings /></Protected>} />
    <Route path="/bookings/:id" element={<Protected><DetailRoute /></Protected>} />
    <Route path="/bookings/:id/payment" element={<Protected><DetailRoute paymentPage /></Protected>} />
    <Route path="/admin/showtimes" element={<Protected admin><AdminShowtimes /></Protected>} />
    <Route path="/admin/bookings" element={<Protected admin><Bookings admin /></Protected>} />
    <Route path="/admin/bookings/:id" element={<Protected admin><DetailRoute admin /></Protected>} />
    <Route path="/admin/payments" element={<Protected admin><AdminPayments /></Protected>} />
    <Route path="*" element={<Empty title="Không tìm thấy trang"><a href="/movies">Quay về phim &amp; suất chiếu →</a></Empty>} />
  </Routes></Layout>;
}
