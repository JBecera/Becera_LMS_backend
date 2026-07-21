import { useEffect, useMemo, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import Stamp from "../components/ui/Stamp";
import { getBook } from "../services/bookService";
import { getMember } from "../services/memberService";
import { selfCheckout } from "../services/transactionService";

// Mirrors TransactionService.MAX_LOAN_DAYS on the backend, which is the source of truth.
const MAX_LOAN_DAYS = 14;

function addDays(date, days) {
  const next = new Date(date);
  next.setDate(next.getDate() + days);
  return next;
}

function toInputValue(date) {
  return date.toISOString().slice(0, 10);
}

function formatDate(date) {
  return date.toLocaleDateString(undefined, { year: "numeric", month: "long", day: "numeric" });
}

function BorrowConfirmation() {
  const { bookId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const storedUser = JSON.parse(localStorage.getItem("user")) || {};

  const [book, setBook] = useState(location.state?.book || null);
  const [loadingBook, setLoadingBook] = useState(!location.state?.book);
  const [loadError, setLoadError] = useState("");
  const [member, setMember] = useState(null);

  const today = useMemo(() => new Date(), []);
  const minReturnDate = useMemo(() => addDays(today, 1), [today]);
  const maxReturnDate = useMemo(() => addDays(today, MAX_LOAN_DAYS), [today]);
  const defaultReturnDate = useMemo(() => addDays(today, 7), [today]);

  const [dueDate, setDueDate] = useState(toInputValue(defaultReturnDate));
  const [fieldError, setFieldError] = useState("");
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

  useEffect(() => {
    if (!storedUser.id) return;
    getMember(storedUser.id)
      .then((res) => setMember(res.data))
      .catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const startOfDay = (date) => new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime();

  const validate = () => {
    if (!dueDate) return "Choose a return date.";
    const chosen = startOfDay(new Date(`${dueDate}T00:00:00`));
    if (chosen < startOfDay(minReturnDate)) {
      return "Return date must be after the borrow date.";
    }
    if (chosen > startOfDay(maxReturnDate)) {
      return `Return date cannot be more than ${MAX_LOAN_DAYS} days from today.`;
    }
    return "";
  };

  const handleConfirm = async (event) => {
    event.preventDefault();
    const error = validate();
    setFieldError(error);
    if (error) return;

    setSubmitError("");
    setSubmitting(true);
    try {
      const response = await selfCheckout({ resourceId: book.id, dueDate });
      setResult(response.data);
    } catch (err) {
      setSubmitError(err.response?.data?.error || "Unable to complete this borrowing request.");
    } finally {
      setSubmitting(false);
    }
  };

  if (loadingBook) {
    return (
      <AppLayout eyebrow="Borrow" title="Borrow confirmation" description="Loading title details…">
        <section className="panel-card" style={{ marginTop: 0 }} />
      </AppLayout>
    );
  }

  if (loadError || !book) {
    return (
      <AppLayout eyebrow="Borrow" title="Borrow confirmation" description="We ran into a problem loading this title.">
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
      <AppLayout eyebrow="Borrow" title="Borrowing confirmed" description="Your loan has been created — no trip to the front desk required.">
        <section className="panel-card confirmation-success" style={{ marginTop: 0 }}>
          <Stamp value="✓" label="Confirmed" />
          <div>
            <h2 style={{ marginBottom: "0.35rem" }}>{book.title}</h2>
            <p className="panel-sub" style={{ marginBottom: "1.25rem" }}>
              Checked out today · due back {result.dueDate}
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

  const fullName = [member?.firstName || storedUser.firstName, member?.lastName || storedUser.lastName].filter(Boolean).join(" ");

  return (
    <AppLayout
      eyebrow="Borrow"
      title="Confirm your borrowing"
      description="Review the details below, choose your return date, and confirm to complete the loan."
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
            <span><strong>Available:</strong> {book.availableCopies}</span>
          </div>
          <Badge status={book.availableCopies > 0 ? "available" : "unavailable"}>
            {book.availableCopies > 0 ? `${book.availableCopies} available` : "Out of stock"}
          </Badge>
        </div>
      </section>

      <section className="panel-card">
        <h2>Borrowing details</h2>
        {submitError ? <p className="message-banner error">{submitError}</p> : null}
        <form onSubmit={handleConfirm} className="form-grid" noValidate>
          <div className="form-group">
            <label className="form-label">Borrow date</label>
            <input className="form-input" value={formatDate(today)} disabled readOnly />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="dueDate">Return date</label>
            <input
              id="dueDate"
              className="form-input"
              type="date"
              min={toInputValue(minReturnDate)}
              max={toInputValue(maxReturnDate)}
              value={dueDate}
              onChange={(event) => {
                setDueDate(event.target.value);
                setFieldError("");
              }}
            />
            {fieldError ? <p className="field-error">{fieldError}</p> : <p className="field-hint">Up to {MAX_LOAN_DAYS} days from today.</p>}
          </div>
        </form>
      </section>

      <section className="panel-card">
        <h2>Borrower</h2>
        <div className="book-summary-meta">
          <span><strong>Name:</strong> {fullName || "—"}</span>
          <span><strong>Member ID:</strong> {member?.memberId || "—"}</span>
          <span><strong>Email:</strong> {member?.email || storedUser.email || "—"}</span>
        </div>
      </section>

      <section className="panel-card confirmation-actions-card">
        <button className="button primary auto" onClick={handleConfirm} disabled={submitting}>
          {submitting ? "Confirming…" : "Confirm Borrowing"}
        </button>
        <button className="button secondary auto" type="button" onClick={() => navigate("/catalog")} disabled={submitting}>
          Cancel
        </button>
      </section>
    </AppLayout>
  );
}

export default BorrowConfirmation;
