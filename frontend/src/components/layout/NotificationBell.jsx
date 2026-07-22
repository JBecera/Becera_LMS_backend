import { useEffect, useRef, useState } from "react";
import Icon from "../ui/Icon";

function timeAgo(isoString) {
  const diffMs = Date.now() - new Date(isoString).getTime();
  const minutes = Math.round(diffMs / 60000);
  if (minutes < 1) return "just now";
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.round(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.round(hours / 24);
  return `${days}d ago`;
}

function NotificationBell({ notifications, onMarkRead, onMarkAllRead }) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);

  const unreadCount = notifications.filter((n) => !n.read).length;

  useEffect(() => {
    function handleClickOutside(event) {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="notification-bell" ref={containerRef}>
      <button
        className="notification-bell-trigger"
        onClick={() => setOpen((o) => !o)}
        aria-label="Notifications"
        type="button"
      >
        <Icon name="bell" size={19} />
        {unreadCount > 0 ? <span className="notification-count">{unreadCount}</span> : null}
      </button>

      {open ? (
        <div className="notification-panel">
          <div className="notification-panel-head">
            <strong>Notifications</strong>
            {unreadCount > 0 ? (
              <button className="notification-mark-all" onClick={onMarkAllRead} type="button">
                Mark all read
              </button>
            ) : null}
          </div>
          {notifications.length === 0 ? (
            <p className="notification-empty">You&apos;re all caught up.</p>
          ) : (
            <ul className="notification-list">
              {notifications.map((n) => (
                <li
                  key={n.id}
                  className={`notification-item${n.read ? "" : " unread"}`}
                  onClick={() => onMarkRead(n.id)}
                >
                  <p className="notification-message">{n.message}</p>
                  <span className="notification-time">{timeAgo(n.createdAt)}</span>
                </li>
              ))}
            </ul>
          )}
        </div>
      ) : null}
    </div>
  );
}

export default NotificationBell;
