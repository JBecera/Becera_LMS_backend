import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import { cancelReservation, getMemberReservations } from "../services/reservationService";
import { getMemberTransactions } from "../services/transactionService";
import { pickupCountdown } from "../utils/pickup";

function daysUntil(dateString) {
  if (!dateString) return null;
  return Math.ceil((new Date(dateString) - new Date()) / (1000 * 60 * 60 * 24));
}

// Transaction only stores ACTIVE/RETURNED, so a booking's "Checked out"/"Returned" state is derived
// by matching a COMPLETED reservation to the loan that followed it for the same title.
function matchTransaction(reservation, transactions, usedIds) {
  return transactions
    .filter((t) => t.resourceId === reservation.resourceId && !usedIds.has(t.id))
    .filter((t) => !reservation.approvedAt || t.checkOutDate >= reservation.approvedAt)
    .sort((a, b) => new Date(a.checkOutDate) - new Date(b.checkOutDate))[0] || null;
}

function buildRows(reservations, transactions) {
  const usedTransactionIds = new Set();
  const active = [];
  const history = [];

  reservations
    .slice()
    .sort((a, b) => new Date(b.reservationDate) - new Date(a.reservationDate))
    .forEach((r) => {
      const row = {
        key: `reservation-${r.id}`,
        resourceTitle: r.resourceTitle,
        date: r.pickupDate || r.reservationDate,
        cancellable: r.status === "PENDING",
        id: r.id,
      };

      if (r.status === "PENDING") {
        row.badge = <Badge status="pending">Pending</Badge>;
        row.note = "Awaiting librarian approval";
        active.push(row);
      } else if (r.status === "APPROVED") {
        const { expired, label } = pickupCountdown(r.approvedAt);
        if (expired) {
          row.badge = <Badge status="expired">Expired</Badge>;
          row.note = "Pickup window passed";
        } else {
          row.badge = <Badge status="approved">Approved</Badge>;
          row.note = label;
        }
        active.push(row);
      } else if (r.status === "REJECTED") {
        row.badge = <Badge status="rejected">Rejected</Badge>;
        row.note = r.reason ? `Reason: ${r.reason}` : "—";
        history.push(row);
      } else if (r.status === "COMPLETED") {
        const t = matchTransaction(r, transactions, usedTransactionIds);
        if (t) {
          usedTransactionIds.add(t.id);
          if (t.status === "RETURNED") {
            row.badge = <Badge status="returned">Returned</Badge>;
            row.note = `Returned ${t.checkInDate}`;
            history.push(row);
          } else {
            const remaining = daysUntil(t.dueDate);
            row.badge = (
              <Badge status={remaining < 0 ? "overdue" : "checked-out"}>
                {remaining < 0 ? `${Math.abs(remaining)}d overdue` : "Checked out"}
              </Badge>
            );
            row.note = `Due ${t.dueDate}`;
            active.push(row);
          }
        } else {
          row.badge = <Badge status="approved">Approved</Badge>;
          active.push(row);
        }
      }
    });

  // Loans not originating from a tracked booking (e.g. a direct librarian checkout) still belong here.
  transactions
    .filter((t) => !usedTransactionIds.has(t.id))
    .sort((a, b) => new Date(b.checkOutDate) - new Date(a.checkOutDate))
    .forEach((t) => {
      const remaining = daysUntil(t.dueDate);
      const row = { key: `transaction-${t.id}`, resourceTitle: t.resourceTitle, date: t.checkOutDate };
      if (t.status === "RETURNED") {
        row.badge = <Badge status="returned">Returned</Badge>;
        row.note = `Returned ${t.checkInDate}`;
        history.push(row);
      } else {
        row.badge = (
          <Badge status={remaining < 0 ? "overdue" : "checked-out"}>
            {remaining < 0 ? `${Math.abs(remaining)}d overdue` : "Checked out"}
          </Badge>
        );
        row.note = `Due ${t.dueDate}`;
        active.push(row);
      }
    });

  return { active, history };
}

function BookingsTable({ rows, onCancel }) {
  return (
    <div className="table-wrap">
      <table className="data-table">
        <thead>
          <tr>
            <th>Resource</th>
            <th>Date</th>
            <th>Status</th>
            <th>Details</th>
            {onCancel ? <th></th> : null}
          </tr>
        </thead>
        <tbody>
          {rows.map((row) => (
            <tr key={row.key}>
              <td>{row.resourceTitle}</td>
              <td className="mono">{row.date}</td>
              <td>{row.badge}</td>
              <td>{row.note || "—"}</td>
              {onCancel ? (
                <td>
                  {row.cancellable ? (
                    <button className="table-action danger" onClick={() => onCancel(row.id)}>Cancel</button>
                  ) : null}
                </td>
              ) : null}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function MyBookings() {
  const user = JSON.parse(localStorage.getItem("user")) || {};
  const [reservations, setReservations] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [message, setMessage] = useState("");

  const load = () => {
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
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.id]);

  const handleCancel = async (id) => {
    try {
      await cancelReservation(id);
      setMessage("Booking cancelled.");
      load();
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to cancel this booking.");
    }
  };

  const { active, history } = buildRows(reservations, transactions);

  return (
    <AppLayout
      eyebrow="Bookings"
      title="My Bookings"
      description="Every booking from request to return — pending approval, ready for pickup, checked out, and past records."
    >
      {message ? <p className="message-banner">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        <h2>Active</h2>
        {active.length === 0 ? (
          <EmptyState icon="borrow" title="Nothing active" description="Book a title from the catalog to see it here." />
        ) : (
          <BookingsTable rows={active} onCancel={handleCancel} />
        )}
      </section>

      <section className="panel-card">
        <h2>History</h2>
        {history.length === 0 ? (
          <EmptyState icon="history" title="No history yet" description="Returned and rejected bookings will appear here." />
        ) : (
          <BookingsTable rows={history} />
        )}
      </section>
    </AppLayout>
  );
}

export default MyBookings;
