import { useNavigate } from "react-router-dom";

function AdminDashboard() {
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
          <p className="eyebrow">Admin Panel</p>
          <h1>Welcome, {user?.firstName || "Admin"}.</h1>
          <p>You can manage library users and librarians from this secure workspace.</p>
        </div>
        <button className="button secondary logout-btn" onClick={handleLogout}>Logout</button>
      </header>

      <section className="dashboard-grid">
        <article className="dashboard-card">
          <h2>Manage users</h2>
          <p className="metric">12</p>
          <p className="card-copy">Approve or remove accounts and oversee access.</p>
        </article>
        <article className="dashboard-card">
          <h2>Manage librarians</h2>
          <p className="metric">4</p>
          <p className="card-copy">Assign staff permissions and monitor activity.</p>
        </article>
      </section>
    </div>
  );
}

export default AdminDashboard;
