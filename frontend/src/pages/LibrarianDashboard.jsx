import { useNavigate } from "react-router-dom";

function LibrarianDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "null");

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Librarian Workspace</p>
          <h1>Hello, {user?.firstName || "Librarian"}.</h1>
          <p>Manage resources, approve bookings, and coordinate library operations.</p>
        </div>
        <button className="button secondary logout-btn" onClick={handleLogout}>Logout</button>
      </header>

      <section className="dashboard-grid">
        <article className="dashboard-card">
          <h2>Resources</h2>
          <p className="metric">18</p>
          <p className="card-copy">Track study rooms, equipment, and available materials.</p>
        </article>
        <article className="dashboard-card">
          <h2>Bookings</h2>
          <p className="metric">7</p>
          <p className="card-copy">Review and approve member booking requests.</p>
        </article>
      </section>
    </div>
  );
}

export default LibrarianDashboard;
