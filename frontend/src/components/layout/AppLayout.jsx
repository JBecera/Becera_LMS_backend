import { useEffect, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import Icon from "../ui/Icon";
import NotificationBell from "./NotificationBell";
import { navByRole, roleTag } from "./navConfig";
import { getMemberReservations } from "../../services/reservationService";
import { getNotifications, markAllRead, markRead, syncBookingNotifications } from "../../services/notificationService";

const NOTIFICATION_POLL_MS = 20000;

function initials(user) {
  const a = user?.firstName?.[0] || "";
  const b = user?.lastName?.[0] || "";
  return (a + b).toUpperCase() || "LR";
}

function AppLayout({ eyebrow, title, description, actions, children }) {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "null");
  const role = user?.role?.toUpperCase() || "MEMBER";
  const links = navByRole[role] || navByRole.MEMBER;
  const isMember = role === "MEMBER";

  const [notifications, setNotifications] = useState(() => (isMember && user?.id ? getNotifications(user.id) : []));

  useEffect(() => {
    if (!isMember || !user?.id) return undefined;

    const sync = () => {
      getMemberReservations(user.id)
        .then((res) => setNotifications(syncBookingNotifications(user.id, res.data || [])))
        .catch(() => {});
    };

    sync();
    const interval = setInterval(sync, NOTIFICATION_POLL_MS);
    return () => clearInterval(interval);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleMarkRead = (id) => setNotifications(markRead(user.id, id));
  const handleMarkAllRead = () => setNotifications(markAllRead(user.id));

  const handleLogout = () => {
    localStorage.removeItem("user");
    localStorage.removeItem("token");
    navigate("/login");
  };

  return (
    <div className="app-shell">
      <aside className="app-sidebar">
        <div className="sidebar-mark">
          <div className="plate">LR</div>
          <div className="mark-text">
            <strong>LRBMS</strong>
            <span>Card Catalog Wing</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          <p className="sidebar-section-label">Navigate</p>
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end
              className={({ isActive }) => `sidebar-link${isActive ? " active" : ""}`}
            >
              <Icon name={link.icon} size={17} />
              {link.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-foot">
          <div className="sidebar-user">
            <div className="avatar">{initials(user)}</div>
            <div className="who">
              <strong>{user?.firstName ? `${user.firstName} ${user.lastName || ""}` : "Guest"}</strong>
              <span>{roleTag[role] || role}</span>
            </div>
          </div>
          <button className="sidebar-logout" onClick={handleLogout}>
            <Icon name="logout" size={16} />
            Sign out
          </button>
        </div>
      </aside>

      <main className="app-main">
        <header className="app-topbar">
          <div>
            {eyebrow ? <p className="eyebrow">{eyebrow}</p> : null}
            <h1>{title}</h1>
            {description ? <p>{description}</p> : null}
          </div>
          <div className="app-topbar-actions">
            {isMember ? (
              <NotificationBell
                notifications={notifications}
                onMarkRead={handleMarkRead}
                onMarkAllRead={handleMarkAllRead}
              />
            ) : null}
            {actions ? <div>{actions}</div> : null}
          </div>
        </header>
        {children}
      </main>
    </div>
  );
}

export default AppLayout;
