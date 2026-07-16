import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout";
import StatCard from "../components/ui/StatCard";
import Badge from "../components/ui/Badge";
import Stamp from "../components/ui/Stamp";
import EmptyState from "../components/ui/EmptyState";
import { getMemberTransactions } from "../services/transactionService";
import { getMemberReservations } from "../services/reservationService";
import { getMemberFines } from "../services/fineService";

function daysUntil(dateString) {
  if (!dateString) return null;
  const diff = new Date(dateString) - new Date();
  return Math.ceil(diff / (1000 * 60 * 60 * 24));
}

function Dashboard() {
  const user = JSON.parse(localStorage.getItem("user")) || {};
  const [transactions, setTransactions] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [fines, setFines] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!user.id) return;
    Promise.all([
      getMemberTransactions(user.id).catch(() => ({ data: [] })),
      getMemberReservations(user.id).catch(() => ({ data: [] })),
      getMemberFines(user.id).catch(() => ({ data: [] })),
    ])
      .then(([t, r, f]) => {
        setTransactions(t.data || []);
        setReservations(r.data || []);
        setFines(f.data || []);
      })
      .catch(() => setMessage("Some dashboard data could not be loaded right now."));
  }, [user.id]);

  const activeLoans = transactions.filter((t) => t.status !== "RETURNED");
  const overdueLoans = activeLoans.filter((t) => daysUntil(t.dueDate) < 0);
  const pendingReservations = reservations.filter((r) => r.status === "PENDING");
  const unpaidFines = fines.filter((f) => f.paymentStatus !== "PAID");
  const totalOwed = unpaidFines.reduce((sum, f) => sum + (Number(f.amount) || 0), 0);

  return (
    <AppLayout
      eyebrow="Member Dashboard"
      title={`Welcome back, ${user.firstName || "Reader"}.`}
      description="A quick look at what you've borrowed, what's due, and what's waiting on your shelf."
    >
      {message ? <p className="message-banner error">{message}</p> : null}

      <section className="dashboard-grid">
        <StatCard label="Books on loan" value={activeLoans.length} hint="Limit of 3 at a time" />
        <StatCard
          label="Overdue"
          value={overdueLoans.length}
          hint={overdueLoans.length ? "Return these as soon as possible" : "You're all caught up"}
          tone={overdueLoans.length ? "danger" : ""}
        />
        <StatCard label="Pending reservations" value={pendingReservations.length} hint="Awaiting librarian approval" tone="warn" />
        <StatCard label="Fines due" value={`₱${totalOwed.toFixed(2)}`} hint={unpaidFines.length ? "Settle at the front desk" : "Nothing outstanding"} tone={totalOwed > 0 ? "danger" : "accent"} />
      </section>

      <section className="panel-card">
        <h2>Currently borrowed</h2>
        <p className="panel-sub">Due dates count down from today — return before they turn red.</p>

        {activeLoans.length === 0 ? (
          <EmptyState icon="borrow" title="No active loans" description="Reserve a title from the catalog to get started." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Resource</th>
                  <th>Checked out</th>
                  <th>Due</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {activeLoans.map((t) => {
                  const remaining = daysUntil(t.dueDate);
                  return (
                    <tr key={t.id}>
                      <td>{t.resourceTitle || t.resourceId}</td>
                      <td className="mono">{t.checkOutDate}</td>
                      <td className="mono">{t.dueDate}</td>
                      <td>
                        <Badge status={remaining < 0 ? "overdue" : "reserved"}>
                          {remaining < 0 ? `${Math.abs(remaining)}d overdue` : `${remaining}d left`}
                        </Badge>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {overdueLoans.length > 0 ? (
        <section className="panel-card" style={{ display: "flex", gap: "1.25rem", alignItems: "center" }}>
          <Stamp value={overdueLoans.length} label="Overdue" tone="danger" />
          <div>
            <h2 style={{ marginBottom: "0.35rem" }}>Return overdue items to keep borrowing</h2>
            <p className="panel-sub" style={{ marginBottom: 0 }}>
              Accounts with overdue items or unpaid fines are restricted from new checkouts and reservations.{" "}
              <Link to="/history">View your full history</Link>.
            </p>
          </div>
        </section>
      ) : null}
    </AppLayout>
  );
}

export default Dashboard;
