import { useCallback, useEffect, useRef, useState } from 'react';
import { newKey, request, serverNow } from './api';

export function useResource(path) {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(Boolean(path));
  const [error, setError] = useState(null);
  const [version, setVersion] = useState(0);
  const previousPath = useRef(null);
  const reload = useCallback(() => setVersion(v => v + 1), []);
  useEffect(() => {
    if (previousPath.current !== path) setData(null);
    previousPath.current = path;
    setError(null);
    if (!path) { setLoading(false); return; }
    const controller = new AbortController();
    setLoading(true);
    request(path, { signal: controller.signal }).then(value => {
      if (!controller.signal.aborted) setData(value);
    }).catch(e => {
      if (!controller.signal.aborted) setError(e);
    }).finally(() => {
      if (!controller.signal.aborted) setLoading(false);
    });
    return () => controller.abort();
  }, [path, version]);
  return { data, loading, error, reload, setData };
}

export function readSession(key) {
  try { return JSON.parse(sessionStorage.getItem(key)); } catch { return null; }
}

export function writeSession(key, value) {
  try {
    if (value == null) sessionStorage.removeItem(key);
    else sessionStorage.setItem(key, JSON.stringify(value));
  } catch { /* An in-memory operation still works if browser storage is unavailable. */ }
}

// Retain the exact key and body after an uncertain response. Retrying is always explicit.
export function useMutation(scope) {
  const storageKey = `cinema:operation:${scope}`;
  const [pending, setPending] = useState(() => readSession(storageKey));
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState(null);
  const lock = useRef(false);
  const current = useRef(pending);
  async function run(operation) {
    if (lock.current) return null;
    const saved = current.current || { ...operation, key: operation.key || newKey() };
    if (!saved.path) return null;
    current.current = saved;
    setPending(saved);
    writeSession(storageKey, saved);
    lock.current = true;
    setBusy(true);
    setError(null);
    try {
      const result = await request(saved.path, saved);
      current.current = null;
      setPending(null);
      writeSession(storageKey, null);
      return result;
    } catch (e) {
      setError(e);
      if (e.status >= 400 && e.status < 500) {
        current.current = null;
        setPending(null);
        writeSession(storageKey, null);
      }
      return null;
    } finally { lock.current = false; setBusy(false); }
  }
  return { run, retry: () => run(current.current || {}), pending, busy, error };
}

export function useNow() {
  const [now, setNow] = useState(serverNow());
  useEffect(() => {
    const timer = setInterval(() => setNow(serverNow()), 1000);
    return () => clearInterval(timer);
  }, []);
  return now;
}
