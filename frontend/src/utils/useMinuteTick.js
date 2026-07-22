import { useEffect, useState } from "react";

// Re-renders the calling component on an interval (default every minute) and returns the current
// timestamp, so live countdowns like the pickup deadline stay current without a page refresh.
export function useMinuteTick(intervalMs = 60 * 1000) {
  const [now, setNow] = useState(() => Date.now());
  useEffect(() => {
    const id = setInterval(() => setNow(Date.now()), intervalMs);
    return () => clearInterval(id);
  }, [intervalMs]);
  return now;
}
