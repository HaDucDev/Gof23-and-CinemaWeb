import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../lib/auth';
import { useMutation, useResource } from '../lib/hooks';
import { dateTime, money } from '../lib/format';
import { Empty, ErrorBox, Loading, RetryMutation, Title } from '../components/Common';
import { LedgerTable } from './BookingDetail';

function Simulation() {
  const { user } = useAuth();
  const resource = useResource('/api/admin/payment-simulation');
  const mutation = useMutation(`${user.accountId}:simulation`);
  const [outcome, setOutcome] = useState('SUCCESS');
  const [saved, setSaved] = useState(false);
  useEffect(() => { if (resource.data) setOutcome(resource.data.outcome); }, [resource.data]);
  function accepted() { setSaved(true); resource.reload(); }
  async function submit(event) {
    event.preventDefault();
    const value = await mutation.run({ path: '/api/admin/payment-simulation', method: 'PUT', body: { outcome } });
    if (value) accepted();
  }
  return <section className="surface p-4 mb-4 simulation-panel"><div><p className="eyebrow">CẤU HÌNH THANH TOÁN GIẢ LẬP</p><h3>Kết quả cho lượt thanh toán mới</h3><p className="small muted mb-0">Lưu cấu hình trước khi khách bấm thanh toán. Hoàn tiền sử dụng phương thức gốc.</p>{resource.data && <p className="small mt-2 mb-0">Đang áp dụng: <strong>{resource.data.outcome === 'SUCCESS' ? 'Thành công' : 'Thất bại'}</strong>{resource.data.updatedAt && <> · {dateTime(resource.data.updatedAt)}</>}</p>}</div><div>
    <ErrorBox error={resource.error} retry={resource.reload} /><RetryMutation mutation={mutation} onSuccess={accepted} />
    {saved && <p className="small text-success" role="status">Đã lưu cấu hình.</p>}
    {resource.loading && !resource.data ? <Loading /> : <form onSubmit={submit}><fieldset className="d-flex align-items-end flex-wrap gap-2" disabled={mutation.busy || Boolean(mutation.pending) || !resource.data}><div><label className="form-label" htmlFor="outcome">Kết quả thanh toán giả lập</label><select id="outcome" className="form-select" value={outcome} onChange={e => { setOutcome(e.target.value); setSaved(false); }}><option value="SUCCESS">Thành công</option><option value="FAIL">Thất bại</option></select></div><button className="btn btn-primary">{mutation.busy ? 'Đang lưu…' : 'Lưu cấu hình'}</button></fieldset></form>}
    </div></section>;
}

export default function AdminPayments() {
  const [limit, setLimit] = useState('100');
  const [filter, setFilter] = useState('');
  const [operation, setOperation] = useState('');
  const resource = useResource(`/api/admin/payments?limit=${limit}`);
  const rows = (resource.data || []).filter(row => (!operation || row.operationType === operation) && `${row.bookingId} ${row.referenceCode || ''} ${row.paymentMethod} ${row.operationType}`.toLowerCase().includes(filter.toLowerCase()));
  const incoming = (resource.data || []).filter(row => Number(row.amount) > 0).reduce((sum, row) => sum + Number(row.amount), 0);
  const outgoing = (resource.data || []).filter(row => Number(row.amount) < 0).reduce((sum, row) => sum + Number(row.amount), 0);
  return <>
    <Title eyebrow="QUẢN TRỊ CINEMAMINI" action={<Link className="btn btn-outline-dark" to="/admin/bookings">Xem đơn &amp; nhật ký xử lý →</Link>}>Giao dịch</Title>
    <Simulation />
    <div className="row g-3 mb-4">{[['Khoản thu', incoming], ['Khoản hoàn', outgoing], ['Thu ròng', incoming + outgoing]].map(([label, value]) => <div className="col-sm-4" key={label}><div className="surface p-4"><p className="eyebrow">{label}</p><strong className="ledger-total">{money(value)}</strong><small className="d-block muted">Trong {resource.data?.length || 0} giao dịch gần nhất</small></div></div>)}</div>
    <section className="surface p-4"><div className="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-3"><h3 className="mb-0">Lịch sử thu / hoàn tiền</h3><div className="d-flex gap-2 flex-wrap"><label className="visually-hidden" htmlFor="ledger-filter">Tìm giao dịch</label><input id="ledger-filter" className="form-control filter-input" type="search" placeholder="Mã đơn, tham chiếu, phương thức…" value={filter} onChange={e => setFilter(e.target.value)} /><label className="visually-hidden" htmlFor="ledger-operation">Loại giao dịch</label><select id="ledger-operation" className="form-select w-auto" value={operation} onChange={event => setOperation(event.target.value)}><option value="">Tất cả giao dịch</option><option value="PAYMENT">Thanh toán</option><option value="REFUND">Hoàn tiền</option></select><label className="visually-hidden" htmlFor="ledger-limit">Số giao dịch</label><select className="form-select limit-select" id="ledger-limit" value={limit} onChange={e => setLimit(e.target.value)}><option value="100">100 dòng</option><option value="250">250 dòng</option><option value="500">500 dòng</option></select><button className="btn btn-outline-secondary btn-sm" onClick={resource.reload} disabled={resource.loading}>Làm mới</button></div></div><ErrorBox error={resource.error} retry={resource.reload} />{resource.loading && !resource.data ? <Loading /> : resource.data && !rows.length ? <Empty title="Chưa có giao dịch phù hợp">Khoản thu và khoản hoàn được hiển thị khi có phát sinh.</Empty> : <LedgerTable rows={rows} admin />}</section>
  </>;
}
