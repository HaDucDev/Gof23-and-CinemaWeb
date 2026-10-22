const zone = 'Asia/Ho_Chi_Minh';
export const money = value => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(Number(value) || 0);
export function time(value) {
  return value ? new Intl.DateTimeFormat('vi-VN', { timeZone: zone, hour: '2-digit', minute: '2-digit', hour12: false }).format(new Date(value)) : '—';
}
export function day(value) {
  return value ? new Intl.DateTimeFormat('vi-VN', { timeZone: zone, day: '2-digit', month: '2-digit', year: 'numeric' }).format(new Date(value)) : '—';
}
export const dateTime = value => value ? `${time(value)} · ${day(value)}` : '—';
export function dateKey(value) {
  return new Intl.DateTimeFormat('en-CA', { timeZone: zone, year: 'numeric', month: '2-digit', day: '2-digit' }).format(new Date(value));
}
export const statusLabels = {
  CREATING: 'Đang tạo đơn', PENDING: 'Chờ thanh toán', PAYMENT_PROCESSING: 'Đang thanh toán',
  COMPENSATING: 'Đang trả ghế', CANCELLING: 'Đang hủy / hoàn tiền', CONFIRMED: 'Đã xác nhận',
  CANCELLED: 'Đã hủy', EXPIRED: 'Đã hết hạn', FAILED_NEEDS_REVIEW: 'Cần kiểm tra',
  SUCCEEDED: 'Thành công', FAILED: 'Thất bại', COMPLETED: 'Hoàn tất', RUNNING: 'Đang xử lý',
  REFUNDED: 'Đã hoàn tiền',
};
export const processing = value => ['CREATING', 'PAYMENT_PROCESSING', 'COMPENSATING', 'CANCELLING'].includes(value);
export const methods = {
  CASH: 'Tiền mặt', BANK_TRANSFER: 'Chuyển khoản ngân hàng', DOMESTIC_ATM: 'Thẻ ATM nội địa',
  VISA: 'Visa', MASTERCARD: 'Mastercard', MOMO: 'Ví MoMo', ZALOPAY: 'Ví ZaloPay', VNPAY: 'VNPay',
};
export const groups = { REGULAR: 'Khách thường', STUDENT: 'Sinh viên', MEMBER: 'Thành viên' };
