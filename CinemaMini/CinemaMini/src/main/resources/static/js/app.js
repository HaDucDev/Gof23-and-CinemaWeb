"use strict";

(() => {
  const money = (value) => new Intl.NumberFormat("vi-VN").format(Number(value || 0)) + " đ";
  const csrf = document.querySelector('meta[name="csrf-token"]')?.content;
  const csrfHeader = document.querySelector('meta[name="csrf-header"]')?.content;
  const key = () => crypto.randomUUID();
  async function json(url, options = {}) {
    const headers = {
      Accept: "application/json",
      ...(options.body ? { "Content-Type": "application/json" } : {}),
    };
    if (csrfHeader && csrf) {
      headers[csrfHeader] = csrf;
    }
    let response;
    try {
      response = await fetch(url, { credentials: "same-origin", ...options, headers });
    } catch (_) {
      const error = new Error(
        "Mất kết nối, chưa xác định kết quả yêu cầu. Hãy thử lại yêu cầu vừa gửi.",
      );
      error.network = true;
      throw error;
    }
    const result = await response
      .json()
      .catch(() => ({ message: "Chưa thể đọc kết quả. Vui lòng tải lại trang." }));
    if (!response.ok) {
      const error = new Error(result.message || "Không thể hoàn tất yêu cầu.");
      error.status = response.status;
      error.code = result.code;
      throw error;
    }
    return result;
  }

  document.querySelectorAll("[data-password-toggle]").forEach((toggle) => {
    const input = document.getElementById(toggle.dataset.passwordToggle);
    if (!input) {
      return;
    }
    const syncVisibility = () => {
      input.type = toggle.checked ? "text" : "password";
    };
    toggle.addEventListener("change", syncVisibility);
    syncVisibility();
  });

  document.querySelectorAll("form[data-submit-lock]").forEach((form) =>
    form.addEventListener("submit", (event) => {
      if (form.dataset.confirm && !window.confirm(form.dataset.confirm)) {
        event.preventDefault();
        return;
      }
      if (form.dataset.submitting) {
        event.preventDefault();
        return;
      }
      form.dataset.submitting = "true";
      form.querySelectorAll('button[type="submit"], button:not([type])').forEach((button) => {
        button.disabled = true;
        button.textContent = "Đang xử lý…";
      });
    }),
  );

  document.querySelectorAll(".countdown").forEach((element) => {
    const expires = Date.parse(element.dataset.expires),
      offset = Date.parse(element.dataset.serverTime) - Date.now();
    function tick() {
      const remaining = Math.max(0, Math.ceil((expires - Date.now() - offset) / 1000));
      element.textContent = remaining
        ? `Giữ ghế còn ${Math.floor(remaining / 60)}:${String(remaining % 60).padStart(2, "0")}`
        : "Đã hết thời gian giữ ghế. Mở lại chi tiết đơn để xem trạng thái mới.";
      if (!remaining) {
        document.querySelectorAll("[data-payment-form] button").forEach((button) => {
          button.disabled = true;
        });
      }
    }
    tick();
    setInterval(tick, 1000);
  });

  const notificationList = document.getElementById("notification-list");
  if (notificationList) {
    let closed = false,
      socket;
    async function refreshNotifications() {
      try {
        const data = await json("/notifications");
        document.getElementById("notification-count").textContent = data.unread;
        notificationList.replaceChildren();
        if (!data.items.length) {
          const p = document.createElement("p");
          p.className = "muted";
          p.textContent = "Bạn chưa có thông báo mới.";
          notificationList.append(p);
        }
        data.items.forEach((item) => {
          const article = document.createElement("article");
          article.className = "notification-item" + (item.readAt ? "" : " unread");
          const link = document.createElement("a");
          link.href = item.url;
          link.textContent = item.content;
          article.append(link);
          const time = document.createElement("small");
          time.textContent = new Intl.DateTimeFormat("vi-VN", {
            dateStyle: "short",
            timeStyle: "short",
            timeZone: "Asia/Ho_Chi_Minh",
          }).format(new Date(item.createdAt));
          article.append(time);
          if (!item.readAt) {
            const button = document.createElement("button");
            button.type = "button";
            button.textContent = "Đánh dấu đã đọc";
            button.addEventListener("click", async () => {
              button.disabled = true;
              try {
                await json(`/notifications/${item.id}/read`, { method: "POST" });
                await refreshNotifications();
              } catch (_) {
                button.disabled = false;
              }
            });
            article.append(button);
          }
          notificationList.append(article);
        });
      } catch (error) {
        if (error.status === 401 || error.status === 403) {
          closed = true;
          socket?.close();
          notificationList.textContent = "Phiên đăng nhập đã kết thúc. Vui lòng đăng nhập lại.";
        }
      }
    }
    function connect() {
      if (closed) {
        return;
      }
      socket = new WebSocket(
        `${location.protocol === "https:" ? "wss" : "ws"}://${location.host}/notifications/socket`,
      );
      socket.onopen = refreshNotifications;
      socket.onmessage = refreshNotifications;
      socket.onclose = () => {
        if (!closed) {
          setTimeout(connect, 5000);
        }
      };
    }
    document.querySelector('form[action="/logout"]')?.addEventListener("submit", () => {
      closed = true;
      socket?.close();
    });
    window.addEventListener("pagehide", () => {
      closed = true;
      socket?.close();
    });
    refreshNotifications();
    connect();
  }

  const app = document.getElementById("seat-app");
  if (!app) {
    return;
  }
  const showtimeId = app.dataset.showtimeId,
    canBook = app.dataset.canBook === "true";
  const grid = document.getElementById("seat-grid"),
    form = document.getElementById("seat-form"),
    notice = document.getElementById("seat-message");
  const retry = document.getElementById("retry-seat"),
    checkout = document.getElementById("checkout-button"),
    release = document.getElementById("release-button");
  const continueLink = document.getElementById("continue-checkout"),
    connection = document.getElementById("seat-connection");
  const storageKey = `cinemamini:${app.dataset.account}:${showtimeId}:pending`;
  const rows = new Map();
  let hold = null,
    busy = false,
    pending = null,
    offset = 0,
    ready = false,
    expiredChecked = false,
    mutationEpoch = 0;
  const queued = [];
  try {
    pending = JSON.parse(sessionStorage.getItem(storageKey) || "null");
  } catch (_) {
    sessionStorage.removeItem(storageKey);
  }
  function message(text, danger = false) {
    notice.textContent = text;
    notice.className = "alert " + (danger ? "alert-warning" : "alert-info");
    notice.hidden = !text;
  }
  function remaining() {
    return hold
      ? Math.max(0, Math.ceil((Date.parse(hold.expiresAt) - Date.now() - offset) / 1000))
      : 0;
  }
  function setHold(value) {
    if (hold && value && hold.id === value.id && Number(value.revision) < Number(hold.revision)) {
      return;
    }
    hold = value && ["ACTIVE", "CHECKOUT"].includes(value.status) ? value : null;
    if (value?.serverTime) {
      offset = Date.parse(value.serverTime) - Date.now();
    }
    expiredChecked = false;
  }
  function updates(values) {
    values.forEach((value) => {
      const current = rows.get(value.seatId);
      if (
        (!current && value.code) ||
        (current && Number(value.version) >= Number(current.version))
      ) {
        rows.set(value.seatId, { ...current, ...value });
      }
    });
  }
  function paint() {
    if (!ready) {
      return;
    }
    const own = new Set(hold?.seatIds || []),
      sorted = [...rows.values()].sort((a, b) => a.code.localeCompare(b.code));
    const active =
      canBook && !busy && !pending && (!hold || (hold.status === "ACTIVE" && remaining() > 0));
    grid.replaceChildren();
    sorted.forEach((row) => {
      const mine = own.has(row.seatId) && row.status === "HELD";
      const button = document.createElement("button");
      button.type = "button";
      button.className =
        "seat-button" +
        (row.type === "VIP" ? " vip" : "") +
        (mine
          ? " mine"
          : row.status === "HELD"
            ? " held"
            : row.status === "BOOKED"
              ? " booked"
              : row.status === "DISABLED"
                ? " disabled"
                : "") +
        (busy ? " busy" : "");
      button.textContent = row.code;
      button.setAttribute("aria-pressed", String(mine));
      button.title = `${row.code} · ${row.type === "VIP" ? "VIP" : "Thường"} · ${money(row.unitPrice)} · ${mine ? "Bạn đang giữ" : { AVAILABLE: "Còn trống", HELD: "Người khác giữ", BOOKED: "Đã bán", DISABLED: "Không mở bán" }[row.status]}`;
      button.setAttribute("aria-label", button.title);
      button.disabled = !active || (!mine && row.status !== "AVAILABLE");
      button.addEventListener("click", () => choose(row, mine));
      grid.append(button);
    });
    const selected = sorted.filter((row) => own.has(row.seatId));
    document.getElementById("selected-seats").textContent = selected.length
      ? selected.map((row) => row.code).join(", ")
      : "Chưa chọn ghế";
    const price = hold?.price;
    document.getElementById("quote-subtotal").textContent = money(price?.subtotal);
    document.getElementById("quote-discount").textContent = price?.discountAmount
      ? "−" + money(price.discountAmount)
      : "0 đ";
    document.getElementById("quote-total").textContent = money(price?.total);
    if (checkout) {
      checkout.disabled = !active || !hold || !selected.length;
    }
    if (release) {
      release.disabled = !active || !hold || !selected.length;
    }
    if (form) {
      const values = {
        holdId: hold?.id || "",
        holdToken: hold?.token || "",
        revision: hold?.revision ?? "",
        seatIds: (hold?.seatIds || []).join(","),
      };
      Object.entries(values).forEach(([name, value]) => {
        form.elements[name].value = value;
      });
      form.hidden = hold?.status === "CHECKOUT";
    }
    continueLink.hidden = !(hold?.status === "CHECKOUT" && hold.bookingId);
    if (!continueLink.hidden) {
      continueLink.href = `/bookings/${hold.bookingId}/payment`;
    }
    retry.hidden = !pending;
    tick();
  }
  async function snapshot() {
    const data = await json(`/showtimes/${showtimeId}/availability`);
    updates(data.seats);
    if (!hold) {
      offset = Date.parse(data.serverTime) - Date.now();
    }
    ready = true;
    queued.splice(0).forEach(updates);
    paint();
  }
  async function ownHold() {
    if (!canBook) {
      return;
    }
    const epoch = mutationEpoch;
    const data = await json(`/showtimes/${showtimeId}/my-hold`);
    if (!busy && epoch === mutationEpoch) {
      setHold(data.hold);
      paint();
    }
  }
  function remember(value) {
    pending = value;
    try {
      if (value) {
        sessionStorage.setItem(storageKey, JSON.stringify(value));
      } else {
        sessionStorage.removeItem(storageKey);
      }
    } catch (_) {}
  }
  async function execute(command) {
    if (busy) {
      return;
    }
    busy = true;
    mutationEpoch++;
    remember(command);
    message("");
    paint();
    try {
      const result = await json(command.url, {
        method: command.method,
        body: JSON.stringify(command.body),
      });
      remember(null);
      setHold(result);
      await snapshot();
    } catch (error) {
      if (!error.network) {
        remember(null);
      }
      message(error.message, true);
      if (!error.network) {
        try {
          const current = await json(`/showtimes/${showtimeId}/my-hold`);
          setHold(current.hold);
          await snapshot();
        } catch (_) {}
      }
    } finally {
      busy = false;
      paint();
    }
  }
  function choose(row, mine) {
    if (busy || pending) {
      return;
    }
    if (!hold) {
      execute({
        url: `/showtimes/${showtimeId}/holds`,
        method: "POST",
        body: { seatId: row.seatId, requestKey: key() },
      });
    } else {
      execute({
        url: `/holds/${hold.id}/seats${mine ? "/" + row.seatId : ""}`,
        method: mine ? "DELETE" : "POST",
        body: { seatId: row.seatId, token: hold.token, revision: hold.revision, requestKey: key() },
      });
    }
  }
  release?.addEventListener("click", () => {
    if (hold && !busy && !pending) {
      execute({
        url: `/holds/${hold.id}/release`,
        method: "POST",
        body: { token: hold.token, revision: hold.revision, requestKey: key() },
      });
    }
  });
  retry.addEventListener("click", () => {
    if (pending) {
      execute(pending);
    }
  });
  form?.addEventListener("submit", (event) => {
    if (
      busy ||
      pending ||
      !hold ||
      !remaining() ||
      hold.status !== "ACTIVE" ||
      !hold.seatIds.length
    ) {
      event.preventDefault();
      message("Vui lòng chọn ghế và chờ yêu cầu giữ ghế hoàn tất.", true);
      return;
    }
    busy = true;
    checkout.disabled = true;
    checkout.textContent = "Đang tạo đơn…";
  });
  function tick() {
    const clock = document.getElementById("hold-clock"),
      seconds = remaining();
    clock.textContent = hold
      ? seconds
        ? `Giữ ghế còn ${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, "0")}`
        : "Đã hết thời gian giữ ghế"
      : "Giữ ghế tối đa 5 phút";
    if (hold && !seconds && !expiredChecked) {
      expiredChecked = true;
      if (checkout) {
        checkout.disabled = true;
      }
      if (release) {
        release.disabled = true;
      }
      grid.querySelectorAll("button").forEach((button) => {
        button.disabled = true;
      });
      json(`/holds/${hold.id}`)
        .then((value) => {
          if (value.status === "EXPIRED") {
            setHold(null);
            message("Đã hết thời gian giữ ghế. Vui lòng chọn lại.", true);
            snapshot().catch(() => {});
          }
        })
        .catch(() => {});
    }
  }
  const source = new EventSource(`/showtimes/${showtimeId}/seat-events`);
  source.addEventListener("availability", (event) => {
    const values = JSON.parse(event.data);
    if (!ready) {
      queued.push(values);
    } else {
      updates(values);
      paint();
    }
  });
  source.onopen = () => {
    connection.textContent = "● Đã kết nối";
    connection.classList.add("connected");
    Promise.all([snapshot(), ownHold()]).catch((error) => message(error.message, true));
  };
  source.onerror = () => {
    connection.textContent = "Đang kết nối lại…";
    connection.classList.remove("connected");
  };
  window.addEventListener("pagehide", () => source.close());
  Promise.all([snapshot(), ownHold()]).catch((error) => message(error.message, true));
  if (pending) {
    message(
      "Có yêu cầu chưa xác định kết quả. Hãy thử lại yêu cầu vừa gửi trước khi chọn tiếp.",
      true,
    );
  }
  setInterval(tick, 1000);
})();
