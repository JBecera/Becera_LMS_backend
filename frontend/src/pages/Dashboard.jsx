import { useNavigate } from "react-router-dom";

function Dashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user")) || {};

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Library Resource Dashboard</p>
          <h1>Welcome back, {user?.firstName || "Library User"}.</h1>
          <p>
            View your current bookings, loan history, and upcoming library services from one secure place.
          </p>
        </div>
        <button className="button secondary logout-btn" onClick={handleLogout}>
          Logout
        </button>
      </header>

      <section className="dashboard-grid">
        <article className="dashboard-card">
          <h2>Active reservations</h2>
          <p className="metric">4</p>
          <p className="card-copy">Current resource bookings ready for pickup or room access.</p>
        </article>

        <article className="dashboard-card">
          <h2>Pending requests</h2>
          <p className="metric">1</p>
          <p className="card-copy">Requests awaiting approval from library staff.</p>
        </article>

        <article className="dashboard-card">
          <h2>Borrowed items</h2>
          <p className="metric">2</p>
          <p className="card-copy">Items currently checked out under your account.</p>
        </article>
      </section>

      <section className="overview-grid">
        <article className="stat-card">
          <p className="stat-label">Next room booking</p>
          <strong>Design Lab 3 · May 22 · 10:00 AM</strong>
        </article>
        <article className="stat-card">
          <p className="stat-label">Account status</p>
          <strong>Good standing</strong>
        </article>
        <article className="stat-card">
          <p className="stat-label">Support</p>
          <strong>Contact library staff anytime</strong>
        </article>
      </section>
    </div>
  );
}

export default Dashboard;
