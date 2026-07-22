import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import {
  cancelReservation,
  getMemberReservations,
  getReservations,
  updateReservationStatus,
} from "../services/reservationService";
import { getTransactions } from "../services/transactionService";
import { getFines } from "../services/fineService";
import { checkOutResource } from "../services/transactionService";

const PICKUP_WINDOW_DAYS = 3;

function daysBetween(from, to) {
  const diff = new Date(to) - new Date(from);
  return Math.floor(diff / (1000 * 60 * 60 * 24));
}

function toInputValue(date) {
  return date.toISOString().slice(0, 10);
}

function defaultDueDate() {
  const d = new Date();
  d.setDate(d.getDate() + 7);
  return toInputValue(d);
}

function minDueDate() {
  const d = new Date();
  d.setDate(d.getDate() + 1);
  return toInputValue(d);
}

function MemberBookings({ user }) {
  const [reservations, setReservations] = useState([]);
  const [message, setMessage] = useState("");

  const load = () => {
    getMemberReservations(user.id)
      .then((res) => setReservations(res.data || []))
      .catch(() => setMessage("Unable to load your bookings right now."));
  };

  useEffect(() => {
    if (!user.id) return;
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleCancel = async (id) => {
    try {
      await cancelReservation(id);
      setMessage("Booking cancelled.");
      load();
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to cancel this booking.");
    }
  };

  return (
    <AppLayout
      eyebrow="Bookings"
      title="My bookings"
      description="Track the resources you've booked and their approval status."
    >
      {message ? <p className="message-banner">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        {reservations.length === 0 ? (
          <EmptyState icon="reserve" title="No bookings" description="Book a title from the catalog to see it here." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Resource</th>
                  <th>Booked on</th>
                  <th>Status</th>
                  <th>Details</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {reservations.map((r) => (
                  <tr key={r.id}>
                    <td>{r.resourceTitle || r.resourceId}</td>
                    <td className="mono">{r.reservationDate}</td>
                    <td>
                      <Badge status={r.status?.toLowerCase()} />
                    </td>
                    <td>
                      {r.status === "REJECTED" && r.reason ? `Reason: ${r.reason}` : null}
                      {r.status === "PENDING" && r.queuePosition ? `#${r.queuePosition} in line` : null}
                    </td>
                    <td>
                      {r.status === "PENDING" ? (
                        <button className="table-action danger" onClick={() => handleCancel(r.id)}>Cancel</button>
                      ) : null}
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

function LibrarianBookings() {
  const [reservations, setReservations] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [fines, setFines] = useState([]);
  const [message, setMessage] = useState(null);

  const [rejectDrafts, setRejectDrafts] = useState({});
  const [checkoutDrafts, setCheckoutDrafts] = useState({});

  const load = () => {
    Promise.all([
      getReservations().catch(() => ({ data: [] })),
      getTransactions().catch(() => ({ data: [] })),
      getFines().catch(() => ({ data: [] })),
    ]).then(([r, t, f]) => {
      setReservations(r.data || []);
      setTransactions(t.data || []);
      setFines(f.data || []);
    });
  };

  useEffect(() => {
    load();
  }, []);

  const activeLoanCount = (memberId) =>
    transactions.filter((t) => t.memberId === memberId && t.status === "ACTIVE").length;

  const unpaidFineTotal = (memberId) =>
    fines
      .filter((f) => f.memberId === memberId && f.paymentStatus !== "PAID")
      .reduce((sum, f) => sum + (Number(f.amount) || 0), 0);

  const pending = reservations.filter((r) => r.status === "PENDING");
  const approved = reservations.filter((r) => r.status === "APPROVED");
  const history = reservations.filter((r) => r.status === "REJECTED" || r.status === "COMPLETED");

  const handleApprove = async (id) => {
    try {
      await updateReservationStatus(id, "APPROVED");
      setMessage({ type: "success", text: "Booking approved." });
      load();
    } catch (error) {
      setMessage({ type: "error", text: error.response?.data?.error || "Unable to approve this booking." });
    }
  };

  const handleRejectSubmit = async (id) => {
    const reason = (rejectDrafts[id] || "").trim();
    if (!reason) {
      setMessage({ type: "error", text: "A rejection reason is required." });
      return;
    }
    try {
      await updateReservationStatus(id, "REJECTED", reason);
      setMessage({ type: "success", text: "Booking rejected." });
      setRejectDrafts((prev) => {
        const next = { ...prev };
        delete next[id];
        return next;
      });
      load();
    } catch (error) {
      setMessage({ type: "error", text: error.response?.data?.error || "Unable to reject this booking." });
    }
  };

  const handleCheckout = async (reservation) => {
    const dueDate = checkoutDrafts[reservation.id] || defaultDueDate();
    try {
      await checkOutResource({ memberId: reservation.memberId, resourceId: reservation.resourceId, dueDate });
      setMessage({ type: "success", text: "Marked as checked out." });
      load();
    } catch (error) {
      setMessage({ type: "error", text: error.response?.data?.error || "Unable to check this member out." });
    }
  };

  const handleCancelExpired = async (id) => {
    try {
      await cancelReservation(id);
      setMessage({ type: "success", text: "Expired booking cancelled." });
      load();
    } catch (error) {
      setMessage({ type: "error", text: error.response?.data?.error || "Unable to cancel this booking." });
    }
  };

  return (
    <AppLayout
      eyebrow="Bookings"
      title="Booking approvals"
      description="Approve or reject pending bookings, check members out on pickup, and clear expired holds."
    >
      {message ? (
        <p className={`message-banner${message.type === "error" ? " error" : ""}`}>{message.text}</p>
      ) : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        <h2>Pending approvals</h2>
        {pending.length === 0 ? (
          <EmptyState icon="reserve" title="Nothing pending" description="New booking requests from members will appear here." />
        ) : (
          <div className="dashboard-grid">
            {pending.map((r) => {
              const isRejecting = rejectDrafts[r.id] !== undefined;
              return (
                <article key={r.id} className="dashboard-card booking-card">
                  <h3>{r.resourceTitle}</h3>
                  <p className="catalog-meta">{r.memberName}</p>
                  <div className="book-summary-meta">
                    <span><strong>Booked on:</strong> {r.reservationDate}</span>
                    <span><strong>Active loans:</strong> {activeLoanCount(r.memberId)}</span>
                    <span><strong>Unpaid fines:</strong> ₱{unpaidFineTotal(r.memberId).toFixed(2)}</span>
                  </div>

                  {isRejecting ? (
                    <div className="form-group" style={{ marginTop: "0.75rem" }}>
                      <label className="form-label">Rejection reason</label>
                      <input
                        className="form-input"
                        value={rejectDrafts[r.id]}
                        onChange={(e) => setRejectDrafts((prev) => ({ ...prev, [r.id]: e.target.value }))}
                        placeholder="e.g. Member has an overdue item"
                      />
                      <div className="catalog-footer" style={{ marginTop: "0.5rem" }}>
                        <button className="table-action danger" onClick={() => handleRejectSubmit(r.id)}>Confirm reject</button>
                        <button
                          className="table-action"
                          onClick={() =>
                            setRejectDrafts((prev) => {
                              const next = { ...prev };
                              delete next[r.id];
                              return next;
                            })
                          }
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  ) : (
                    <div className="catalog-footer" style={{ marginTop: "0.75rem" }}>
                      <button className="button primary auto" onClick={() => handleApprove(r.id)}>Approve</button>
                      <button
                        className="button secondary auto"
                        onClick={() => setRejectDrafts((prev) => ({ ...prev, [r.id]: "" }))}
                      >
                        Reject
                      </button>
                    </div>
                  )}
                </article>
              );
            })}
          </div>
        )}
      </section>

      <section className="panel-card">
        <h2>Approved — awaiting pickup</h2>
        {approved.length === 0 ? (
          <EmptyState icon="reserve" title="Nothing awaiting pickup" description="Approved bookings will appear here until the member is checked out." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Member</th>
                  <th>Resource</th>
                  <th>Approved</th>
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {approved.map((r) => {
                  const daysLeft = r.approvedAt ? PICKUP_WINDOW_DAYS - daysBetween(r.approvedAt, new Date()) : null;
                  const expired = daysLeft !== null && daysLeft < 0;
                  return (
                    <tr key={r.id}>
                      <td>{r.memberName}</td>
                      <td>{r.resourceTitle}</td>
                      <td className="mono">{r.approvedAt || "—"}</td>
                      <td>
                        {expired ? (
                          <Badge status="expired">Expired</Badge>
                        ) : (
                          <Badge status="approved">{daysLeft}d left to pick up</Badge>
                        )}
                      </td>
                      <td>
                        {expired ? (
                          <button className="table-action danger" onClick={() => handleCancelExpired(r.id)}>Cancel expired</button>
                        ) : (
                          <div style={{ display: "flex", gap: "0.5rem", alignItems: "center" }}>
                            <input
                              className="form-input"
                              type="date"
                              min={minDueDate()}
                              value={checkoutDrafts[r.id] || defaultDueDate()}
                              onChange={(e) => setCheckoutDrafts((prev) => ({ ...prev, [r.id]: e.target.value }))}
                            />
                            <button className="table-action" onClick={() => handleCheckout(r)}>Mark checked out</button>
                          </div>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="panel-card">
        <h2>History</h2>
        {history.length === 0 ? (
          <EmptyState icon="reserve" title="No history yet" description="Rejected and completed bookings will appear here." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Member</th>
                  <th>Resource</th>
                  <th>Booked on</th>
                  <th>Status</th>
                  <th>Details</th>
                </tr>
              </thead>
              <tbody>
                {history.map((r) => (
                  <tr key={r.id}>
                    <td>{r.memberName}</td>
                    <td>{r.resourceTitle}</td>
                    <td className="mono">{r.reservationDate}</td>
                    <td><Badge status={r.status?.toLowerCase()} /></td>
                    <td>{r.status === "REJECTED" && r.reason ? r.reason : "—"}</td>
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

function Reservations() {
  const user = JSON.parse(localStorage.getItem("user")) || {};
  const isLibrarian = user.role?.toUpperCase() === "LIBRARIAN" || user.role?.toUpperCase() === "ADMIN";

  return isLibrarian ? <LibrarianBookings /> : <MemberBookings user={user} />;
}

export default Reservations;
