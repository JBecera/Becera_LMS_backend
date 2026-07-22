// A booking approved on `approvedAt` (a YYYY-MM-DD date) must be collected within this many days.
export const PICKUP_WINDOW_DAYS = 3;

const HOUR_MS = 1000 * 60 * 60;
const DAY_MS = HOUR_MS * 24;

// Returns { expired, label } for an approved booking's pickup window, counting down in whole days
// until the final day, then switching to hours so the last stretch doesn't read as "0d left".
export function pickupCountdown(approvedAt, windowDays = PICKUP_WINDOW_DAYS) {
  if (!approvedAt) {
    return { expired: false, label: "Ready for pickup" };
  }
  const deadline = new Date(`${approvedAt}T00:00:00`).getTime() + windowDays * DAY_MS;
  const remaining = deadline - Date.now();

  if (remaining <= 0) {
    return { expired: true, label: "Expired" };
  }
  if (remaining >= DAY_MS) {
    return { expired: false, label: `${Math.ceil(remaining / DAY_MS)}d left to pick up` };
  }
  return { expired: false, label: `${Math.max(1, Math.ceil(remaining / HOUR_MS))}h left to pick up` };
}
