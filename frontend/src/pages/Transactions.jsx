import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import { getBooks } from "../services/bookService";
import { getMembers } from "../services/memberService";
import { checkInResource, checkOutResource, getTransactions } from "../services/transactionService";

function defaultDueDate() {
  const d = new Date();
  d.setDate(d.getDate() + 7);
  return d.toISOString().slice(0, 10);
}

function Transactions() {
  const [transactions, setTransactions] = useState([]);
  const [members, setMembers] = useState([]);
  const [books, setBooks] = useState([]);
  const [form, setForm] = useState({ memberId: "", resourceId: "", dueDate: defaultDueDate() });
  const [message, setMessage] = useState("");

  const load = () => {
    Promise.all([
      getTransactions().catch(() => ({ data: [] })),
      getMembers().catch(() => ({ data: [] })),
      getBooks().catch(() => ({ data: [] })),
    ]).then(([t, m, b]) => {
      setTransactions(t.data || []);
      setMembers((m.data || []).filter((x) => x.role?.toUpperCase() === "MEMBER"));
      setBooks(b.data || []);
    });
  };

  useEffect(() => {
    load();
  }, []);

  const activeLoans = transactions.filter((t) => t.status !== "RETURNED");
  const availableBooks = books.filter((b) => b.availableCopies > 0);

  const handleCheckout = async (event) => {
    event.preventDefault();
    if (!form.memberId || !form.resourceId) {
      setMessage("Choose a member and a resource before checking out.");
      return;
    }
    try {
      await checkOutResource(form);
      setMessage("Resource checked out successfully.");
      setForm({ memberId: "", resourceId: "", dueDate: defaultDueDate() });
      load();
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to complete checkout.");
    }
  };

  const handleCheckIn = async (id) => {
    try {
      await checkInResource(id);
      setMessage("Resource checked in successfully.");
      load();
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to complete check-in.");
    }
  };

  return (
    <AppLayout
      eyebrow="Transactions"
      title="Check-In / Check-Out"
      description="Record borrowing and returns. Availability updates automatically after each transaction."
    >
      {message ? <p className="message-banner">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        <h2>Check out a resource</h2>
        <form onSubmit={handleCheckout} className="form-grid">
          <select className="form-input" value={form.memberId} onChange={(e) => setForm({ ...form, memberId: e.target.value })}>
            <option value="">Select member</option>
            {members.map((m) => (
              <option key={m.id} value={m.id}>{m.firstName} {m.lastName} — {m.email}</option>
            ))}
          </select>
          <select className="form-input" value={form.resourceId} onChange={(e) => setForm({ ...form, resourceId: e.target.value })}>
            <option value="">Select resource</option>
            {availableBooks.map((b) => (
              <option key={b.id} value={b.id}>{b.title} ({b.availableCopies} available)</option>
            ))}
          </select>
          <input className="form-input" type="date" value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })} />
          <button className="button primary auto" type="submit">Check out</button>
        </form>
      </section>

      <section className="panel-card">
        <h2>Active loans</h2>
        {activeLoans.length === 0 ? (
          <EmptyState icon="transactions" title="No active loans" description="Checked-out resources will appear here until they're returned." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Member</th>
                  <th>Resource</th>
                  <th>Checked out</th>
                  <th>Due</th>
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {activeLoans.map((t) => {
                  const overdue = t.dueDate && new Date(t.dueDate) < new Date();
                  return (
                    <tr key={t.id}>
                      <td>{t.memberName || t.memberId}</td>
                      <td>{t.resourceTitle || t.resourceId}</td>
                      <td className="mono">{t.checkOutDate}</td>
                      <td className="mono">{t.dueDate}</td>
                      <td><Badge status={overdue ? "overdue" : "reserved"}>{overdue ? "Overdue" : "On loan"}</Badge></td>
                      <td><button className="table-action" onClick={() => handleCheckIn(t.id)}>Check in</button></td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </AppLayout>
  );
}

export default Transactions;
