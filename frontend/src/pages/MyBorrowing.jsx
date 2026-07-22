import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import { getMemberReservations } from "../services/reservationService";
import { getMemberTransactions } from "../services/transactionService";

const PICKUP_WINDOW_DAYS = 3;

function daysBetween(from, to) {
  const diff = new Date(to) - new Date(from);
  return Math.floor(diff / (1000 * 60 * 60 * 24));
}

function daysUntil(dateString) {
  if (!dateString) return null;
  const diff = new Date(dateString) - new Date();
  return Math.ceil(diff / (1000 * 60 * 60 * 24));
}

// Transaction still only knows ACTIVE/RETURNED — a booking's "Checked Out"/"Returned"
// state is derived here by matching a COMPLETED reservation to the transaction that
// followed it for the same title, since checkout auto-completes the reservation
// (see TransactionService.performCheckout on the backend) without linking the two rows.
function matchTransaction(reservation, transactions, usedIds) {
  const candidates = transactions
    .filter((t) => t.resourceId === reservation.resourceId && !usedIds.has(t.id))
    .filter((t) => !reservation.approvedAt || t.checkOutDate >= reservation.approvedAt)
    .sort((a, b) => new Date(a.checkOutDate) - new Date(b.checkOutDate));
  return candidates[0] || null;
}

function buildRows(reservations, transactions) {
  const usedTransactionIds = new Set();
  const rows = [];

  reservations
    .slice()
    .sort((a, b) => new Date(b.reservationDate) - new Date(a.reservationDate))
    .forEach((r) => {
      const row = {
        key: `reservation-${r.id}`,
        resourceTitle: r.resourceTitle,
        category: "—",
        date: r.reservationDate,
        dateLabel: "Booked",
      };

      if (r.status === "PENDING") {
        row.badge = <Badge status="pending">Pending</Badge>;
      } else if (r.status === "REJECTED") {
        row.badge = <Badge status="rejected">Rejected</Badge>;
        row.note = r.reason ? `Reason: ${r.reason}` : null;
      } else if (r.status === "APPROVED") {
        const daysLeft = r.approvedAt ? PICKUP_WINDOW_DAYS - daysBetween(r.approvedAt, new Date()) : null;
        if (daysLeft !== null && daysLeft < 0) {
          row.badge = <Badge status="expired">Expired</Badge>;
          row.note = "Pickup window passed — awaiting librarian cancellation.";
        } else {
          row.badge = <Badge status="approved">Approved</Badge>;
          row.note = daysLeft !== null ? `${daysLeft}d left to pick up` : "Ready for pickup";
        }
      } else if (r.status === "COMPLETED") {
        const transaction = matchTransaction(r, transactions, usedTransactionIds);
        if (transaction) {
          usedTransactionIds.add(transaction.id);
          row.category = transaction.resourceCategory || "—";
          if (transaction.status === "RETURNED") {
            row.badge = <Badge status="returned">Returned</Badge>;
            row.note = `Returned ${transaction.checkInDate}`;
          } else {
            const remaining = daysUntil(transaction.dueDate);
            row.badge = (
              <Badge status={remaining < 0 ? "overdue" : "checked-out"}>
                {remaining < 0 ? `${Math.abs(remaining)}d overdue` : "Checked out"}
              </Badge>
            );
            row.note = `Due ${transaction.dueDate}`;
          }
        } else {
          row.badge = <Badge status="approved">Approved</Badge>;
        }
      } else {
        return;
      }

      rows.push(row);
    });

  // Loans that didn't originate from a tracked booking (e.g. a librarian checked the
  // member out directly) still belong in this view.
  transactions
    .filter((t) => !usedTransactionIds.has(t.id))
    .sort((a, b) => new Date(b.checkOutDate) - new Date(a.checkOutDate))
    .forEach((t) => {
      const remaining = daysUntil(t.dueDate);
      rows.push({
        key: `transaction-${t.id}`,
        resourceTitle: t.resourceTitle,
        category: t.resourceCategory || "—",
        date: t.checkOutDate,
        dateLabel: "Checked out",
        badge:
          t.status === "RETURNED" ? (
            <Badge status="returned">Returned</Badge>
          ) : (
            <Badge status={remaining < 0 ? "overdue" : "checked-out"}>
              {remaining < 0 ? `${Math.abs(remaining)}d overdue` : "Checked out"}
            </Badge>
          ),
        note: t.status === "RETURNED" ? `Returned ${t.checkInDate}` : `Due ${t.dueDate}`,
      });
    });

  return rows;
}

function MyBorrowing() {
  const user = JSON.parse(localStorage.getItem("user")) || {};
  const [reservations, setReservations] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!user.id) return;
    Promise.all([
      getMemberReservations(user.id).catch(() => ({ data: [] })),
      getMemberTransactions(user.id).catch(() => ({ data: [] })),
    ])
      .then(([r, t]) => {
        setReservations(r.data || []);
        setTransactions(t.data || []);
      })
      .catch(() => setMessage("Unable to load your bookings right now."));
  }, [user.id]);

  const rows = buildRows(reservations, transactions);

  return (
    <AppLayout
      eyebrow="Borrowing"
      title="My Borrowing"
      description="Track every booking from request to return — pending approval, ready for pickup, checked out, or returned."
    >
      {message ? <p className="message-banner error">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        {rows.length === 0 ? (
          <EmptyState icon="borrow" title="Nothing booked yet" description="Book a title from the catalog to see its status here." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Resource</th>
                  <th>Category</th>
                  <th>Date</th>
                  <th>Status</th>
                  <th>Details</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row) => (
                  <tr key={row.key}>
                    <td>{row.resourceTitle}</td>
                    <td className="mono">{row.category}</td>
                    <td className="mono">{row.dateLabel} {row.date}</td>
                    <td>{row.badge}</td>
                    <td>{row.note || "—"}</td>
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

export default MyBorrowing;
