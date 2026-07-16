import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import StatCard from "../components/ui/StatCard";
import EmptyState from "../components/ui/EmptyState";
import api from "../services/api";

function AdminDashboard() {
  const user = JSON.parse(localStorage.getItem("user") || "null");
  const [members, setMembers] = useState([]);
  const [librarians, setLibrarians] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    Promise.all([api.get("/members"), api.get("/members/role/LIBRARIAN")])
      .then(([allMembersRes, librariansRes]) => {
        setMembers(allMembersRes.data.filter((m) => m.role !== "ADMIN"));
        setLibrarians(librariansRes.data);
      })
      .catch(() => setMessage("Unable to load management data."));
  }, []);

  const students = members.filter((m) => m.role?.toUpperCase() === "MEMBER");
  const recentAccounts = [...members].slice(-5).reverse();

  return (
    <AppLayout
      eyebrow="Admin Panel"
      title={`Welcome, ${user?.firstName || "Admin"}.`}
      description="A system-wide view of registered members and staff accounts."
    >
      {message ? <p className="message-banner error">{message}</p> : null}

      <section className="dashboard-grid">
        <StatCard label="Registered students" value={students.length} hint="Members with borrowing access" />
        <StatCard label="Librarian accounts" value={librarians.length} hint="Staff with administrative access" />
        <StatCard label="Total accounts" value={members.length + 1} hint="Including this admin account" />
      </section>

      <section className="panel-card">
        <h2>Recently added accounts</h2>
        <p className="panel-sub">The latest members and librarians registered in the system.</p>
        {recentAccounts.length === 0 ? (
          <EmptyState icon="accounts" title="No accounts yet" description="New registrations will appear here." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                </tr>
              </thead>
              <tbody>
                {recentAccounts.map((m) => (
                  <tr key={m.id}>
                    <td>{m.firstName} {m.lastName}</td>
                    <td>{m.email}</td>
                    <td className="mono">{m.role}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </AppLayout>
  );
}

export default AdminDashboard;
