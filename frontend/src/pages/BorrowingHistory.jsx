import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import { getMemberTransactions } from "../services/transactionService";

function BorrowingHistory() {
  const user = JSON.parse(localStorage.getItem("user")) || {};
  const [transactions, setTransactions] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!user.id) return;
    getMemberTransactions(user.id)
      .then((res) => setTransactions(res.data || []))
      .catch(() => setMessage("Unable to load your history right now."));
  }, [user.id]);

  const pastLoans = transactions
    .filter((t) => t.status === "RETURNED")
    .sort((a, b) => new Date(b.checkInDate || b.checkOutDate) - new Date(a.checkInDate || a.checkOutDate));

  return (
    <AppLayout
      eyebrow="History"
      title="Borrowing History"
      description="A complete record of resources you've checked out and returned."
    >
      {message ? <p className="message-banner error">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        {pastLoans.length === 0 ? (
          <EmptyState icon="history" title="No history yet" description="Returned items will show up here for your records." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Resource</th>
                  <th>Checked out</th>
                  <th>Due date</th>
                  <th>Returned</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {pastLoans.map((t) => (
                  <tr key={t.id}>
                    <td>{t.resourceTitle || t.resourceId}</td>
                    <td className="mono">{t.checkOutDate}</td>
                    <td className="mono">{t.dueDate}</td>
                    <td className="mono">{t.checkInDate}</td>
                    <td>
                      <Badge status={t.checkInDate && t.dueDate && new Date(t.checkInDate) > new Date(t.dueDate) ? "overdue" : "returned"}>
                        {t.checkInDate && t.dueDate && new Date(t.checkInDate) > new Date(t.dueDate) ? "Returned late" : "Returned on time"}
                      </Badge>
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

export default BorrowingHistory;
