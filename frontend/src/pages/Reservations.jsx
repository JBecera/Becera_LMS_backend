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

function Reservations() {
  const user = JSON.parse(localStorage.getItem("user")) || {};
  const isLibrarian = user.role?.toUpperCase() === "LIBRARIAN" || user.role?.toUpperCase() === "ADMIN";

  const [reservations, setReservations] = useState([]);
  const [message, setMessage] = useState("");

  const load = () => {
    const request = isLibrarian ? getReservations() : getMemberReservations(user.id);
    request
      .then((res) => setReservations(res.data || []))
      .catch(() => setMessage("Unable to load reservations right now."));
  };

  useEffect(() => {
    if (!isLibrarian && !user.id) return;
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleCancel = async (id) => {
    try {
      await cancelReservation(id);
      setMessage("Reservation cancelled.");
      load();
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to cancel this reservation.");
    }
  };

  const handleDecision = async (id, status) => {
    try {
      await updateReservationStatus(id, status);
      setMessage(status === "APPROVED" ? "Reservation approved." : "Reservation rejected.");
      load();
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to update this reservation.");
    }
  };

  return (
    <AppLayout
      eyebrow="Reservations"
      title={isLibrarian ? "Reservation queue" : "My reservations"}
      description={
        isLibrarian
          ? "Approve or reject reservation requests. The system prevents duplicate reservations automatically."
          : "Track the resources you've reserved and their approval status."
      }
    >
      {message ? <p className="message-banner">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        {reservations.length === 0 ? (
          <EmptyState icon="reserve" title="No reservations" description={isLibrarian ? "New requests from members will appear here." : "Reserve a title from the catalog to see it here."} />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Resource</th>
                  {isLibrarian ? <th>Member</th> : null}
                  <th>Reserved on</th>
                  <th>Status</th>
                  <th>Position</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {reservations.map((r) => (
                  <tr key={r.id}>
                    <td>{r.resourceTitle || r.resourceId}</td>
                    {isLibrarian ? <td>{r.memberName || r.memberId}</td> : null}
                    <td className="mono">{r.reservationDate}</td>
                    <td>
                      <Badge status={r.status?.toLowerCase()} />
                    </td>
                    <td className="mono">{r.queuePosition ? `#${r.queuePosition}` : "—"}</td>
                    <td>
                      {isLibrarian && r.status === "PENDING" ? (
                        <>
                          <button className="table-action" onClick={() => handleDecision(r.id, "APPROVED")}>Approve</button>
                          <button className="table-action danger" onClick={() => handleDecision(r.id, "REJECTED")}>Reject</button>
                        </>
                      ) : null}
                      {!isLibrarian && r.status === "PENDING" ? (
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

export default Reservations;
