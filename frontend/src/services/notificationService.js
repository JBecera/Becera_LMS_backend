// Client-side only notification center for booking status changes. There's no
// push/websocket backend, so this diffs each fetched reservation list against a
// per-user snapshot of last-seen statuses and turns PENDING -> APPROVED/REJECTED
// transitions into notification entries, all persisted in localStorage.

const snapshotKey = (userId) => `bookingSnapshot_${userId}`;
const notificationsKey = (userId) => `notifications_${userId}`;

function readJson(key, fallback) {
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : fallback;
  } catch {
    return fallback;
  }
}

function writeJson(key, value) {
  localStorage.setItem(key, JSON.stringify(value));
}

export function getNotifications(userId) {
  return readJson(notificationsKey(userId), []);
}

export function markRead(userId, id) {
  const notifications = getNotifications(userId).map((n) => (n.id === id ? { ...n, read: true } : n));
  writeJson(notificationsKey(userId), notifications);
  return notifications;
}

export function markAllRead(userId) {
  const notifications = getNotifications(userId).map((n) => ({ ...n, read: true }));
  writeJson(notificationsKey(userId), notifications);
  return notifications;
}

// Compares the given reservations (a member's bookings) against the last-seen
// snapshot, appends a notification for any newly APPROVED/REJECTED booking, and
// updates the snapshot. Returns the full, updated notifications list.
export function syncBookingNotifications(userId, reservations) {
  const snapshot = readJson(snapshotKey(userId), {});
  const notifications = getNotifications(userId);
  let changed = false;

  reservations.forEach((r) => {
    const previousStatus = snapshot[r.id];
    if (previousStatus !== r.status && (r.status === "APPROVED" || r.status === "REJECTED")) {
      const alreadyNotified = notifications.some((n) => n.reservationId === r.id && n.status === r.status);
      if (!alreadyNotified) {
        notifications.unshift({
          id: `${r.id}-${r.status}-${Date.now()}`,
          reservationId: r.id,
          status: r.status,
          title: r.resourceTitle || "Your booking",
          message:
            r.status === "APPROVED"
              ? `${r.resourceTitle || "Your booking"} was approved — pick it up within 3 days.`
              : `${r.resourceTitle || "Your booking"} was rejected${r.reason ? `: ${r.reason}` : "."}`,
          createdAt: new Date().toISOString(),
          read: false,
        });
        changed = true;
      }
    }
    if (previousStatus !== r.status) {
      snapshot[r.id] = r.status;
      changed = true;
    }
  });

  if (changed) {
    writeJson(snapshotKey(userId), snapshot);
    writeJson(notificationsKey(userId), notifications);
  }

  return notifications;
}
