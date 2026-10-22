export const API_BASE = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '');
let csrfPromise;
let clockOffset = 0;
export const serverNow = () => Date.now() + clockOffset;

export class ApiError extends Error {
  constructor(message, status = 0, code = '') {
    super(message);
    this.status = status;
    this.code = code;
  }
}

export function resetCsrf() { csrfPromise = undefined; }

async function token() {
  if (!csrfPromise) {
    csrfPromise = request('/api/auth/csrf').catch(error => {
      resetCsrf();
      throw error;
    });
  }
  return csrfPromise;
}

export async function request(path, options = {}) {
  const { method = 'GET', body, key, signal } = options;
  const headers = { Accept: 'application/json' };
  if (method !== 'GET') {
    const csrf = await token();
    headers[csrf.headerName] = csrf.token;
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (key) headers['Idempotency-Key'] = key;
  }
  let response;
  try {
    response = await fetch(API_BASE + path, {
      method, headers, credentials: 'include', signal,
      ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
    });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new ApiError('Không kết nối được máy chủ. Kiểm tra Booking API và kết nối mạng.');
  }
  // HTTP Date is supplied by the server. It only drives the display and button clock;
  // the backend remains responsible for validating every deadline.
  const serverDate = Date.parse(response.headers.get('Date') || '');
  if (Number.isFinite(serverDate)) clockOffset = serverDate + 500 - Date.now();
  const text = await response.text();
  let data;
  try { data = text ? JSON.parse(text) : null; } catch { data = null; }
  if (!response.ok) {
    if (response.status === 401 && !path.startsWith('/api/auth/')) {
      window.dispatchEvent(new Event('cinema:session-expired'));
    }
    if (response.status === 403) resetCsrf();
    throw new ApiError(data?.message || `Máy chủ chưa xử lý được yêu cầu (${response.status}).`, response.status, data?.code);
  }
  if (!data && response.status !== 204) {
    throw new ApiError('Máy chủ trả dữ liệu không hợp lệ. Vui lòng kiểm tra Booking API.', 502);
  }
  return data;
}

export function socketUrl() {
  const url = new URL(API_BASE + '/ws/notifications', window.location.origin);
  url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
  return url.href;
}

export const newKey = () => crypto.randomUUID();
