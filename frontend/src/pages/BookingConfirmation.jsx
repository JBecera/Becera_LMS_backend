import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import Stamp from "../components/ui/Stamp";
import { getBook } from "../services/bookService";
import { createReservation } from "../services/reservationService";

function formatDate(date) {
  return date.toLocaleDateString(undefined, { year: "numeric", month: "long", day: "numeric" });
}

function BookingConfirmation() {
  const { bookId } = useParams();
  const location = useLocation();

  const [book, setBook] = useState(location.state?.book || null);
  const [loadingBook, setLoadingBook] = useState(!location.state?.book);
  const [loadError, setLoadError] = useState("");

  const [submitError, setSubmitError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [result, setResult] = useState(null);

  const today = new Date();

  useEffect(() => {
    if (book || !bookId) return;
    setLoadingBook(true);
    getBook(bookId)
      .then((res) => setBook(res.data))
      .catch(() => setLoadError("We couldn't find that title. It may have been removed from the catalog."))
      .finally(() => setLoadingBook(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [bookId]);

  const inStock = book?.availableCopies > 0;

  const handleConfirm = async (event) => {
    event.preventDefault();
    setSubmitError("");
    setSubmitting(true);
    try {
      const response = await createReservation(book.id);
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
          <Stamp value={inStock ? "✓" : result.queuePosition || "•"} label={inStock ? "Pending" : "In line"} tone="warn" />
          <div>
            <h2 style={{ marginBottom: "0.35rem" }}>{book.title}</h2>
            <p className="panel-sub" style={{ marginBottom: "1.25rem" }}>
              {inStock
                ? "A librarian will approve or reject your booking soon. You'll have 3 days to pick it up once approved."
                : result.queuePosition
                  ? `You're #${result.queuePosition} in line. A librarian will approve your booking once a copy is free.`
                  : "A librarian will approve your booking once a copy is free."}
            </p>
            <div className="confirmation-actions">
              <Link to="/my-borrowing" className="button primary auto">View My Borrowing</Link>
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
      description={
        inStock
          ? "This title is in stock — book it for pickup and a librarian will confirm before you collect it."
          : "This title is fully checked out — join the waitlist and we'll hold a copy for you when one returns."
      }
    >
      <section className="panel-card book-summary-card" style={{ marginTop: 0 }}>
        <div className="catalog-cover book-summary-cover">
          {book.coverImage ? <img src={book.coverImage} alt={book.title} /> : <span>{book.title?.[0] || "B"}</span>}
        </div>
        <div className="book-summary-details">
          <h2>{book.title}</h2>
          <p className="panel-sub" style={{ margin: 0 }}>{book.author}</p>
          <div className="book-summary-meta">
            <span><strong>ISBN:</strong> {book.isbn}</span>
            {inStock ? <span><strong>Available:</strong> {book.availableCopies}</span> : null}
          </div>
          <Badge status={inStock ? "available" : "unavailable"}>
            {inStock ? `${book.availableCopies} available` : "Out of stock"}
          </Badge>
        </div>
      </section>

      <section className="panel-card">
        <h2>Booking details</h2>
        {submitError ? <p className="message-banner error">{submitError}</p> : null}
        <div className="book-summary-meta">
          <span><strong>Booking date:</strong> {formatDate(today)}</span>
          <span>
            <strong>Expected pickup:</strong>{" "}
            {inStock ? "Once a librarian approves your booking" : "As soon as a copy is returned — we'll notify you"}
          </span>
          <span><strong>Status:</strong> <Badge status="pending">Pending</Badge></span>
        </div>
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
