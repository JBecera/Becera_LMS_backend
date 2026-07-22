// The library closes at 6 PM. A booking's pickup deadline is closing time on its pickup date,
// after which an un-collected booking expires (and the backend auto-cancels it).
export const CLOSING_HOUR = 18;
export const CLOSING_LABEL = "6:00 PM";

const MIN_MS = 60 * 1000;
const HOUR_MS = 60 * MIN_MS;
const DAY_MS = 24 * HOUR_MS;

// Local closing time on the pickup date. pickupDate is a plain "YYYY-MM-DD" string, so this reads
// as 6 PM on that date in the viewer's timezone (Manila for library users, matching the backend).
export function pickupDeadline(pickupDate) {
  if (!pickupDate) return null;
  const deadline = new Date(`${pickupDate}T00:00:00`);
  deadline.setHours(CLOSING_HOUR, 0, 0, 0);
  return deadline;
}

// { expired, label } for a booking's pickup window. `now` is passed in so callers can re-render it
// live on a timer. Counts down as "Xd Xh Xm" until under a day remains, then "Xh Xm".
export function pickupCountdown(pickupDate, now = Date.now()) {
  const deadline = pickupDeadline(pickupDate);
  if (!deadline) {
    return { expired: false, label: "Ready for pickup" };
  }
  const remaining = deadline.getTime() - now;
  if (remaining <= 0) {
    return { expired: true, label: "Pickup window expired" };
  }
  const days = Math.floor(remaining / DAY_MS);
  const hours = Math.floor((remaining % DAY_MS) / HOUR_MS);
  const minutes = Math.floor((remaining % HOUR_MS) / MIN_MS);
  const label = days >= 1
    ? `${days}d ${hours}h ${minutes}m left to pick up`
    : `${hours}h ${minutes}m left to pick up`;
  return { expired: false, label };
}

// "by 6:00 PM on 2026-07-23" — reminds the member of the exact cut-off.
export function pickupByLabel(pickupDate) {
  return `by ${CLOSING_LABEL} on ${pickupDate}`;
}
