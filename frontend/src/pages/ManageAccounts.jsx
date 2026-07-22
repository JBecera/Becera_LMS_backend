import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import api from "../services/api";
import { useToast } from "../components/ui/ToastProvider";
import { PASSWORD_HINT, passwordStrengthError } from "../utils/password";
import { emailFormatError } from "../utils/email";

const emptyForm = { id: null, firstName: "", lastName: "", email: "", password: "", role: "LIBRARIAN" };

function ManageAccounts() {
  const toast = useToast();
  const [members, setMembers] = useState([]);
  const [librarians, setLibrarians] = useState([]);
  const [form, setForm] = useState(emptyForm);
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
      setMessage("Unable to load management data.");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    const emailError = emailFormatError(form.email);
    if (emailError) {
      toast.error(emailError);
      return;
    }
    // On create the password is required; on edit it's optional but still must be strong if set.
    if (!form.id || form.password) {
      const passwordError = passwordStrengthError(form.password);
      if (passwordError) {
        toast.error(passwordError);
        return;
      }
    }
    try {
      if (form.id) {
        const { password, ...profileFields } = form;
        await api.put(`/members/${form.id}`, profileFields);
        if (password) {
          await api.put(`/members/${form.id}/password`, { newPassword: password, confirmPassword: password });
        }
        toast.success("Account updated successfully.");
      } else {
        await api.post("/members", { ...form, role: "LIBRARIAN" });
        toast.success("Librarian account created successfully.");
      }
      setForm(emptyForm);
      loadData();
    } catch (error) {
      toast.error(error.response?.data?.error || "Unable to save account.");
    }
  };

  const handleEdit = (account) => {
    setForm({
      id: account.id,
      firstName: account.firstName,
      lastName: account.lastName,
      email: account.email,
      password: "",
      role: account.role,
    });
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Remove this account?")) return;
    try {
      await api.delete(`/members/${id}`);
      toast.success("Account removed successfully.");
      loadData();
    } catch (error) {
      toast.error(error.response?.data?.error || "Unable to remove account.");
    }
  };

  return (
    <AppLayout
      eyebrow="Accounts"
      title="Manage accounts"
      description="Create librarian accounts and keep member records accurate. Only admins can grant librarian access."
      actions={form.id ? <button className="button secondary auto" onClick={() => setForm(emptyForm)}>Cancel edit</button> : null}
    >
      {message ? <p className="message-banner">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        <h2>{form.id ? "Edit account" : "Add librarian account"}</h2>
        <form onSubmit={handleSubmit} className="form-grid">
          <input className="form-input" name="firstName" placeholder="First name" value={form.firstName} onChange={handleChange} required />
          <input className="form-input" name="lastName" placeholder="Last name" value={form.lastName} onChange={handleChange} required />
          <input className="form-input" name="email" type="email" placeholder="Email" value={form.email} onChange={handleChange} required />
          <input
            className="form-input"
            name="password"
            type="password"
            placeholder={form.id ? "New password (leave blank to keep current)" : "Password"}
            value={form.password}
            onChange={handleChange}
            required={!form.id}
          />
          <button className="button primary auto" type="submit">{form.id ? "Save changes" : "Create librarian"}</button>
          <p className="field-hint" style={{ gridColumn: "1 / -1", margin: 0 }}>{PASSWORD_HINT}</p>
        </form>
      </section>

      <section className="dashboard-grid">
        <article className="stat-card">
          <p className="stat-label">Registered users</p>
          <p className="metric">{members.length}</p>
          <p className="hint">Members currently registered</p>
        </article>
        <article className="stat-card">
          <p className="stat-label">Librarian accounts</p>
          <p className="metric">{librarians.length}</p>
          <p className="hint">Staff with librarian permissions</p>
        </article>
      </section>

      <section className="panel-card">
        <h2>Account directory</h2>
        {members.length === 0 ? (
          <EmptyState icon="accounts" title="No accounts yet" description="Registered members and librarians will be listed here." />
        ) : (
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
                    <td><Badge status="neutral">{member.role}</Badge></td>
                    <td>
                      <button className="table-action" onClick={() => handleEdit(member)}>Edit</button>
                      <button className="table-action danger" onClick={() => handleDelete(member.id)}>Remove</button>
                    </td>
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

export default ManageAccounts;
