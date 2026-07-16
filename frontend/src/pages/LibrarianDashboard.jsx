import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import StatCard from "../components/ui/StatCard";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import { getBooks } from "../services/bookService";
import { getTransactions } from "../services/transactionService";
import { getReservations } from "../services/reservationService";
import { getFines } from "../services/fineService";

function LibrarianDashboard() {
  const user = JSON.parse(localStorage.getItem("user") || "null");
  const [books, setBooks] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [fines, setFines] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    Promise.all([
      getBooks().catch(() => ({ data: [] })),
      getTransactions().catch(() => ({ data: [] })),
      getReservations().catch(() => ({ data: [] })),
      getFines().catch(() => ({ data: [] })),
    ])
      .then(([b, t, r, f]) => {
        setBooks(b.data || []);
        setTransactions(t.data || []);
        setReservations(r.data || []);
        setFines(f.data || []);
      })
      .catch(() => setMessage("Some dashboard data could not be loaded right now."));
  }, []);

  const activeLoans = transactions.filter((t) => t.status !== "RETURNED");
  const overdueLoans = activeLoans.filter((t) => t.dueDate && new Date(t.dueDate) < new Date());
  const pendingReservations = reservations.filter((r) => r.status === "PENDING");
  const unpaidFines = fines.filter((f) => f.paymentStatus !== "PAID");

  return (
    <AppLayout
      eyebrow="Librarian Workspace"
      title={`Hello, ${user?.firstName || "Librarian"}.`}
      description="Today's activity across the catalog, checkouts, reservations, and fines."
    >
      {message ? <p className="message-banner error">{message}</p> : null}

      <section className="dashboard-grid">
        <StatCard label="Catalog titles" value={books.length} hint="Total resources on record" />
        <StatCard label="Active loans" value={activeLoans.length} hint="Currently checked out" />
        <StatCard label="Overdue" value={overdueLoans.length} tone={overdueLoans.length ? "danger" : ""} hint="Past due date" />
        <StatCard label="Pending reservations" value={pendingReservations.length} tone={pendingReservations.length ? "warn" : ""} hint="Awaiting approval" />
      </section>

      <section className="panel-card">
        <h2>Reservation queue</h2>
        <p className="panel-sub">The most recent requests waiting on a decision.</p>
        {pendingReservations.length === 0 ? (
          <EmptyState icon="reserve" title="Queue is clear" description="No reservations are waiting on approval right now." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Resource</th>
                  <th>Member</th>
                  <th>Requested</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {pendingReservations.slice(0, 6).map((r) => (
                  <tr key={r.id}>
                    <td>{r.resourceTitle || r.resourceId}</td>
                    <td>{r.memberName || r.memberId}</td>
                    <td className="mono">{r.reservationDate}</td>
                    <td><Badge status="pending" /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {unpaidFines.length > 0 ? (
        <section className="panel-card">
          <h2>Unpaid fines</h2>
          <p className="panel-sub">{unpaidFines.length} member{unpaidFines.length === 1 ? "" : "s"} with an outstanding balance.</p>
        </section>
      ) : null}
    </AppLayout>
  );
}

export default LibrarianDashboard;
