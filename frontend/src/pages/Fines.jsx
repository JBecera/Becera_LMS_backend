import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import StatCard from "../components/ui/StatCard";
import EmptyState from "../components/ui/EmptyState";
import { useToast } from "../components/ui/ToastProvider";
import { getFines, getMemberFines, settleFine } from "../services/fineService";

function Fines() {
  const toast = useToast();
  const user = JSON.parse(localStorage.getItem("user")) || {};
  const isStaff = ["LIBRARIAN", "ADMIN"].includes(user.role?.toUpperCase());

  const [fines, setFines] = useState([]);
  const [message, setMessage] = useState("");

  const load = () => {
    const request = isStaff ? getFines() : getMemberFines(user.id);
    request
      .then((res) => setFines(res.data || []))
      .catch(() => setMessage("Unable to load fines right now."));
  };

  useEffect(() => {
    if (!isStaff && !user.id) return;
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const unpaid = fines.filter((f) => f.paymentStatus !== "PAID");
  const totalOwed = unpaid.reduce((sum, f) => sum + (Number(f.amount) || 0), 0);

  const handleSettle = async (id) => {
    try {
      await settleFine(id);
      toast.success("Fine marked as settled.");
      load();
    } catch (error) {
      toast.error(error.response?.data?.error || "Unable to update this fine.");
    }
  };

  return (
    <AppLayout
      eyebrow="Fines"
      title={isStaff ? "Fines & penalties" : "My fines"}
      description={
        isStaff
          ? "Overdue penalties across all members. Mark a fine settled once it's paid at the desk."
          : "Outstanding penalties on your account. Unpaid fines restrict new borrowing and reservations."
      }
    >
      {message ? <p className="message-banner">{message}</p> : null}

      {!isStaff ? (
        <section className="dashboard-grid" style={{ marginBottom: "1.5rem" }}>
          <StatCard label="Total owed" value={`₱${totalOwed.toFixed(2)}`} tone={totalOwed > 0 ? "danger" : "accent"} />
          <StatCard label="Open fines" value={unpaid.length} hint={unpaid.length ? "Settle at the front desk" : "You're clear"} />
        </section>
      ) : null}

      <section className="panel-card fines-info" style={{ marginTop: 0, marginBottom: "1.5rem" }}>
        <h2>How fines are calculated</h2>
        <p className="panel-sub">
          Overdue penalties are added automatically when a borrowed item is returned past its due date.
        </p>
        <ul className="fines-info-list">
          <li>
            <strong>₱5.00 per day late</strong> — the fine equals the number of days overdue multiplied by ₱5.00.
            A book returned 3 days late, for example, incurs a ₱15.00 fine.
          </li>
          <li>
            <strong>Loan period is up to 14 days.</strong> Days are counted from the due date to the day the item
            is returned — on-time returns are never fined.
          </li>
          <li>
            <strong>Unpaid fines pause borrowing.</strong> While any fine is unpaid, new checkouts and reservations
            are blocked until it is settled at the front desk.
          </li>
        </ul>
      </section>

      <section className="panel-card" style={{ marginTop: 0 }}>
        {fines.length === 0 ? (
          <EmptyState icon="fines" title="No fines on record" description="Overdue penalties will be listed here as soon as they're issued." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  {isStaff ? <th>Member</th> : null}
                  <th>Reason</th>
                  <th>Amount</th>
                  <th>Issued</th>
                  <th>Status</th>
                  {isStaff ? <th></th> : null}
                </tr>
              </thead>
              <tbody>
                {fines.map((f) => (
                  <tr key={f.id}>
                    {isStaff ? <td>{f.memberName || f.memberId}</td> : null}
                    <td>{f.reason}</td>
                    <td className="mono">₱{Number(f.amount || 0).toFixed(2)}</td>
                    <td className="mono">{f.dateIssued}</td>
                    <td>
                      <Badge status={f.paymentStatus?.toLowerCase() === "paid" ? "settled" : "overdue"}>
                        {f.paymentStatus?.toLowerCase() === "paid" ? "Settled" : "Unpaid"}
                      </Badge>
                    </td>
                    {isStaff ? (
                      <td>
                        {f.paymentStatus !== "PAID" ? (
                          <button className="table-action" onClick={() => handleSettle(f.id)}>Mark settled</button>
                        ) : null}
                      </td>
                    ) : null}
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

export default Fines;
