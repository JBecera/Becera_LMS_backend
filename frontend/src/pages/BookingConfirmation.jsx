import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import Stamp from "../components/ui/Stamp";
import { getBook } from "../services/bookService";
import { createReservation } from "../services/reservationService";

function toInputValue(date) {
  return date.toISOString().slice(0, 10);
}

function formatDate(value) {
  const date = value instanceof Date ? value : new Date(`${value}T00:00:00`);
  return date.toLocaleDateString(undefined, { year: "numeric", month: "long", day: "numeric" });
}

function BookingConfirmation() {
  const { bookId } = useParams();
  const location = useLocation();

  const [book, setBook] = useState(location.state?.book || null);
  const [loadingBook, setLoadingBook] = useState(!location.state?.book);
  const [loadError, setLoadError] = useState("");

  const today = useMemo(() => new Date(), []);
  const minDate = toInputValue(today);
  const maxDate = useMemo(() => {
    const d = new Date(today);
    d.setDate(d.getDate() + 30);
    return toInputValue(d);
  }, [today]);

  const [pickupDate, setPickupDate] = useState(location.state?.pickupDate || toInputValue(today));
  const [submitError, setSubmitError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState(null);

  useEffect(() => {
    if (book || !bookId) return;
    setLoadingBook(true);
    getBook(bookId)
      .then((res) => setBook(res.data))
      .catch(() => setLoadError("We couldn't find that title. It may have been removed from the catalog."))
      .finally(() => setLoadingBook(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [bookId]);

  const handleConfirm = async (event) => {
    event.preventDefault();
    setSubmitError("");
    setSubmitting(true);
    try {
      const response = await createReservation(book.id, pickupDate);
      setResult(response.data);
    } catch (err) {
      setSubmitError(err.response?.data?.error || "Unable to complete this booking.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loadingBook) {
    return (
      <AppLayout eyebrow="Booking" title="Booking confirmation" description="Loading title details…">
        <section className="panel-card" style={{ marginTop: 0 }} />
      </AppLayout>
    );
  }

  if (loadError || !book) {
    return (
      <AppLayout eyebrow="Booking" title="Booking confirmation" description="We ran into a problem loading this title.">
        <section className="panel-card" style={{ marginTop: 0 }}>
          <EmptyState icon="catalog" title="Title not found" description={loadError || "This title is no longer available."} />
          <Link to="/catalog" className="button secondary auto" style={{ marginTop: "1rem", display: "inline-flex" }}>
            Back to catalog
          </Link>
        </section>
      </AppLayout>
    );
  }

  if (result) {
    return (
      <AppLayout eyebrow="Booking" title="Booking submitted" description="A librarian will review your request before it's ready for pickup.">
        <section className="panel-card confirmation-success" style={{ marginTop: 0 }}>
          <Stamp value="✓" label="Pending" tone="warn" />
          <div>
            <h2 style={{ marginBottom: "0.35rem" }}>{book.title}</h2>
            <p className="panel-sub" style={{ marginBottom: "1.25rem" }}>
              Requested for pickup on {formatDate(result.pickupDate || pickupDate)}. A librarian will approve or reject
              your booking soon — once approved, collect it by 6:00 PM on your pickup date or the booking expires.
            </p>
            <div className="confirmation-actions">
              <Link to="/bookings" className="button primary auto">View My Bookings</Link>
              <Link to="/catalog" className="button secondary auto">Back to catalog</Link>
            </div>
          </div>
        </section>
      </AppLayout>
    );
  }

  return (
    <AppLayout
      eyebrow="Booking"
      title="Confirm your booking"
      description="Choose when you&rsquo;d like to pick this up. A librarian confirms every booking before collection."
    >
      <section className="panel-card book-summary-card" style={{ marginTop: 0 }}>
        <div className="catalog-cover book-summary-cover">
          {book.coverImage ? <img src={book.coverImage} alt={book.title} /> : <span>{book.title?.[0] || "B"}</span>}
        </div>
        <div className="book-summary-details">
          <h2>{book.title}</h2>
          <p className="panel-sub" style={{ margin: 0 }}>{book.author}</p>
          <div className="book-summary-meta">
            <span><strong>Category:</strong> {book.category}</span>
            <span><strong>ISBN:</strong> {book.isbn}</span>
          </div>
        </div>
      </section>

      <section className="panel-card">
        <h2>Booking details</h2>
        {submitError ? <p className="message-banner error">{submitError}</p> : null}
        <form onSubmit={handleConfirm} className="form-grid" noValidate>
          <div className="form-group">
            <label className="form-label" htmlFor="pickupDate">Pickup date</label>
            <input
              id="pickupDate"
              className="form-input"
              type="date"
              min={minDate}
              max={maxDate}
              value={pickupDate}
              onChange={(event) => setPickupDate(event.target.value)}
            />
            <p className="field-hint">Up to 30 days from today. Once approved, collect by 6:00 PM that day. Status starts as <Badge status="pending">Pending</Badge></p>
          </div>
        </form>
      </section>

      <section className="panel-card confirmation-actions-card">
        <button className="button primary auto" onClick={handleConfirm} disabled={submitting}>
          {submitting ? "Confirming…" : "Confirm Booking"}
        </button>
        <Link to="/catalog" className="button secondary auto">Cancel</Link>
      </section>
    </AppLayout>
  );
}

export default BookingConfirmation;
