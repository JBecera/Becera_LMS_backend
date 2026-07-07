import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function AdminDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "null");

  const [members, setMembers] = useState([]);
  const [librarians, setLibrarians] = useState([]);
  const [form, setForm] = useState({ id: null, firstName: "", lastName: "", email: "", password: "", role: "LIBRARIAN" });
  const [message, setMessage] = useState("");

  const loadData = async () => {
    try {
      const [allMembersRes, librariansRes] = await Promise.all([
        api.get("/members"),
        api.get("/members/role/LIBRARIAN"),
      ]);
      setMembers(allMembersRes.data.filter((member) => member.role !== "ADMIN"));
      setLibrarians(librariansRes.data);
    } catch (error) {
      console.error(error);
      setMessage("Unable to load management data.");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      if (form.id) {
        await api.put(`/members/${form.id}`, form);
        setMessage("Account updated successfully.");
      } else {
        await api.post("/members", { ...form, role: "LIBRARIAN" });
        setMessage("Librarian account created successfully.");
      }
      setForm({ id: null, firstName: "", lastName: "", email: "", password: "", role: "LIBRARIAN" });
      loadData();
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to save account.");
    }
  };

  const handleEdit = (account) => {
    setForm({
      id: account.id,
      firstName: account.firstName,
      lastName: account.lastName,
      email: account.email,
      password: account.password || "",
      role: account.role,
    });
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Remove this account?")) return;
    try {
      await api.delete(`/members/${id}`);
      setMessage("Account removed successfully.");
      loadData();
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to remove account.");
    }
  };

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Admin Panel</p>
          <h1>Welcome, {user?.firstName || "Admin"}.</h1>
          <p>Review all registered members, manage librarian access, and keep account data up to date.</p>
        </div>
        <button className="button secondary logout-btn" onClick={handleLogout}>Logout</button>
      </header>

      {message ? <p className="message-banner">{message}</p> : null}

      <section className="dashboard-grid">
        <article className="dashboard-card">
          <h2>Registered users</h2>
          <p className="metric">{members.length}</p>
          <p className="card-copy">Members currently registered in the library system.</p>
        </article>
        <article className="dashboard-card">
          <h2>Librarian accounts</h2>
          <p className="metric">{librarians.length}</p>
          <p className="card-copy">Staff accounts with librarian permissions.</p>
        </article>
      </section>

      <section className="dashboard-card" style={{ marginTop: "1.5rem" }}>
        <h2>{form.id ? "Edit librarian account" : "Add librarian account"}</h2>
        <form onSubmit={handleSubmit} className="form-grid">
          <input className="form-input" name="firstName" placeholder="First name" value={form.firstName} onChange={handleChange} required />
          <input className="form-input" name="lastName" placeholder="Last name" value={form.lastName} onChange={handleChange} required />
          <input className="form-input" name="email" type="email" placeholder="Email" value={form.email} onChange={handleChange} required />
          <input className="form-input" name="password" type="password" placeholder="Password" value={form.password} onChange={handleChange} required />
          <button className="button primary" type="submit">{form.id ? "Save changes" : "Create librarian"}</button>
        </form>
      </section>

      <section className="dashboard-card" style={{ marginTop: "1.5rem" }}>
        <h2>Member directory</h2>
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {members.map((member) => (
                <tr key={member.id}>
                  <td>{member.firstName} {member.lastName}</td>
                  <td>{member.email}</td>
                  <td>{member.role}</td>
                  <td>
                    <button className="table-action" onClick={() => handleEdit(member)}>Edit</button>
                    <button className="table-action danger" onClick={() => handleDelete(member.id)}>Remove</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

export default AdminDashboard;
