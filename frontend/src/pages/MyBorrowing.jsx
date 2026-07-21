import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import { getMemberTransactions } from "../services/transactionService";

function daysUntil(dateString) {
  if (!dateString) return null;
  const diff = new Date(dateString) - new Date();
  return Math.ceil(diff / (1000 * 60 * 60 * 24));
}

function MyBorrowing() {
  const user = JSON.parse(localStorage.getItem("user")) || {};
  const [transactions, setTransactions] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!user.id) return;
    getMemberTransactions(user.id)
      .then((res) => setTransactions(res.data || []))
      .catch(() => setMessage("Unable to load your borrowed items right now."));
  }, [user.id]);

  const activeLoans = transactions.filter((t) => t.status !== "RETURNED");

  return (
    <AppLayout
      eyebrow="Borrowing"
      title="My Borrowing"
      description="Everything currently checked out under your account, with days remaining until each due date."
    >
      {message ? <p className="message-banner error">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        {activeLoans.length === 0 ? (
          <EmptyState icon="borrow" title="Nothing borrowed yet" description="Titles you borrow from the catalog will show up here." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Resource</th>
                  <th>Category</th>
                  <th>Checked out</th>
                  <th>Due date</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {activeLoans.map((t) => {
                  const remaining = daysUntil(t.dueDate);
                  return (
                    <tr key={t.id}>
                      <td>{t.resourceTitle || t.resourceId}</td>
                      <td className="mono">{t.resourceCategory || "—"}</td>
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
    </AppLayout>
  );
}

export default MyBorrowing;
