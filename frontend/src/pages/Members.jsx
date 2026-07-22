import { useEffect, useMemo, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import EmptyState from "../components/ui/EmptyState";
import { addMember, getMembers } from "../services/memberService";
import { PASSWORD_HINT, passwordStrengthError } from "../utils/password";

const emptyForm = { firstName: "", lastName: "", email: "", password: "" };

function Members() {
  const [members, setMembers] = useState([]);
  const [search, setSearch] = useState("");
  const [form, setForm] = useState(emptyForm);
  const [message, setMessage] = useState("");

  const load = () => {
    getMembers()
      .then((res) => setMembers((res.data || []).filter((m) => m.role?.toUpperCase() === "MEMBER")))
      .catch(() => setMessage("Unable to load member accounts."));
  };

  useEffect(() => {
    load();
  }, []);

  const filtered = useMemo(() => {
    const q = search.toLowerCase();
    if (!q) return members;
    return members.filter((m) => `${m.firstName} ${m.lastName} ${m.email}`.toLowerCase().includes(q));
  }, [members, search]);

  const handleRegister = async (event) => {
    event.preventDefault();
    const passwordError = passwordStrengthError(form.password);
    if (passwordError) {
      setMessage(passwordError);
      return;
    }
    try {
      await addMember({ ...form, role: "MEMBER" });
      setMessage(`Member account created for ${form.firstName} ${form.lastName}.`);
      setForm(emptyForm);
      load();
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to register this member.");
    }
  };

  return (
    <AppLayout
      eyebrow="Members"
      title="Register & search members"
      description="Create new member accounts and look up existing members and their contact details."
    >
      {message ? <p className="message-banner">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        <h2>Register a new member</h2>
        <form onSubmit={handleRegister} className="form-grid">
          <input className="form-input" placeholder="First name" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} required />
          <input className="form-input" placeholder="Last name" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} required />
          <input className="form-input" type="email" placeholder="Email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
          <input className="form-input" type="password" placeholder="Temporary password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
          <button className="button primary auto" type="submit">Register member</button>
          <p className="field-hint" style={{ gridColumn: "1 / -1", margin: 0 }}>{PASSWORD_HINT}</p>
        </form>
      </section>

      <section className="panel-card">
        <h2>Member directory</h2>
        <input className="form-input" style={{ marginBottom: "1.1rem" }} placeholder="Search by name or email" value={search} onChange={(e) => setSearch(e.target.value)} />
        {filtered.length === 0 ? (
          <EmptyState icon="students" title="No members found" description="Registered members will be listed here." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Member ID</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((m) => (
                  <tr key={m.id}>
                    <td>{m.firstName} {m.lastName}</td>
                    <td>{m.email}</td>
                    <td className="mono">{m.memberId || m.id}</td>
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

export default Members;
