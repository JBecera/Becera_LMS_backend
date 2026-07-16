import { useEffect, useMemo, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import EmptyState from "../components/ui/EmptyState";
import { addMember, getMembers } from "../services/memberService";

const emptyForm = { firstName: "", lastName: "", email: "", password: "" };

function Students() {
  const [students, setStudents] = useState([]);
  const [search, setSearch] = useState("");
  const [form, setForm] = useState(emptyForm);
  const [message, setMessage] = useState("");

  const load = () => {
    getMembers()
      .then((res) => setStudents((res.data || []).filter((m) => m.role?.toUpperCase() === "MEMBER")))
      .catch(() => setMessage("Unable to load student accounts."));
  };

  useEffect(() => {
    load();
  }, []);

  const filtered = useMemo(() => {
    const q = search.toLowerCase();
    if (!q) return students;
    return students.filter((s) => `${s.firstName} ${s.lastName} ${s.email}`.toLowerCase().includes(q));
  }, [students, search]);

  const handleRegister = async (event) => {
    event.preventDefault();
    try {
      await addMember({ ...form, role: "MEMBER" });
      setMessage(`Student account created for ${form.firstName} ${form.lastName}.`);
      setForm(emptyForm);
      load();
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to register this student.");
    }
  };

  return (
    <AppLayout
      eyebrow="Students"
      title="Register & search students"
      description="Create new student accounts and look up existing members and their contact details."
    >
      {message ? <p className="message-banner">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        <h2>Register a new student</h2>
        <form onSubmit={handleRegister} className="form-grid">
          <input className="form-input" placeholder="First name" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} required />
          <input className="form-input" placeholder="Last name" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} required />
          <input className="form-input" type="email" placeholder="Email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          <input className="form-input" type="password" placeholder="Temporary password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
          <button className="button primary auto" type="submit">Register student</button>
        </form>
      </section>

      <section className="panel-card">
        <h2>Student directory</h2>
        <input className="form-input" style={{ marginBottom: "1.1rem" }} placeholder="Search by name or email" value={search} onChange={(e) => setSearch(e.target.value)} />
        {filtered.length === 0 ? (
          <EmptyState icon="students" title="No students found" description="Registered students will be listed here." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Student ID</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((s) => (
                  <tr key={s.id}>
                    <td>{s.firstName} {s.lastName}</td>
                    <td>{s.email}</td>
                    <td className="mono">{s.id}</td>
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

export default Students;
