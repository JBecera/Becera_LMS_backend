import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import { getBooks, getBooksAvailableOn } from "../services/bookService";

function toInputValue(date) {
  return date.toISOString().slice(0, 10);
}

function Catalog() {
  const navigate = useNavigate();
  const location = useLocation();
  const [books, setBooks] = useState([]);
  const [search, setSearch] = useState("");
  // Restore the date when a member returns here via "Change date" on the booking page.
  const [pickupDate, setPickupDate] = useState(location.state?.pickupDate || "");
  const [message, setMessage] = useState("");

  const today = useMemo(() => new Date(), []);
  const minDate = toInputValue(today);
  const maxDate = useMemo(() => {
    const d = new Date(today);
    d.setDate(d.getDate() + 30);
    return toInputValue(d);
  }, [today]);

  // When a pickup date is chosen the list reflects per-date availability; otherwise it shows
  // current on-shelf copies. Server-side search only applies to the no-date view; the date view
  // fetches the full availability set and the client-side filter below narrows it.
  const loadCatalog = async (query = "", date = "") => {
    try {
      const response = date ? await getBooksAvailableOn(date) : await getBooks(query);
      setBooks(response.data);
      setMessage("");
    } catch (error) {
      setMessage("Unable to load the catalog right now.");
    }
  };

  useEffect(() => {
    loadCatalog(search, pickupDate);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const filteredBooks = useMemo(() => {
    const query = search.toLowerCase();
    if (!query) return books;
    return books.filter((book) => `${book.title} ${book.author}`.toLowerCase().includes(query));
  }, [books, search]);

  const availabilityCount = (book) =>
    pickupDate ? book.availableOnDate ?? 0 : book.availableCopies ?? 0;

  const handleAction = (book) => {
    navigate(`/catalog/${book.id}/reserve`, { state: { book, pickupDate: pickupDate || undefined } });
  };

  return (
    <AppLayout
      eyebrow="Catalog"
      title="Search the catalog"
      description="Browse by title or author, pick a date to see what's available then, and book resources for pickup entirely online."
    >
      {message ? <p className="message-banner">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        <div className="catalog-filters">
          <input
            className="form-input"
            placeholder="Search by title or author"
            value={search}
            onChange={(event) => {
              setSearch(event.target.value);
              if (!pickupDate) loadCatalog(event.target.value);
            }}
          />
          <div className="catalog-date-filter">
            <label className="form-label" htmlFor="pickupDate">Pickup date</label>
            <input
              id="pickupDate"
              className="form-input"
              type="date"
              min={minDate}
              max={maxDate}
              value={pickupDate}
              onChange={(event) => {
                setPickupDate(event.target.value);
                loadCatalog(search, event.target.value);
              }}
            />
            {pickupDate ? (
              <button
                type="button"
                className="button secondary auto"
                onClick={() => {
                  setPickupDate("");
                  loadCatalog(search, "");
                }}
              >
                Clear date
              </button>
            ) : null}
          </div>
        </div>
        <p className="field-hint" style={{ marginTop: "0.75rem" }}>
          {pickupDate
            ? `Showing availability for ${pickupDate}. A librarian still approves each booking before pickup.`
            : "Showing copies available now. Pick a date to book in advance."}
        </p>
      </section>

      {filteredBooks.length === 0 ? (
        <section className="panel-card">
          <EmptyState icon="catalog" title="No matching titles" description="Try a different title or author, or clear your search." />
        </section>
      ) : (
        <section className="dashboard-grid" style={{ marginTop: "1.5rem" }}>
          {filteredBooks.map((book) => {
            const count = availabilityCount(book);
            const canBook = count > 0;
            return (
              <article key={book.id} className="dashboard-card catalog-card">
                <div className="catalog-cover">
                  {book.coverImage ? <img src={book.coverImage} alt={book.title} /> : <span>{book.title?.[0] || "B"}</span>}
                </div>
                <h3>{book.title}</h3>
                <p className="catalog-meta">{book.author}</p>
                <p className="catalog-meta">{book.category}</p>
                <p className="catalog-desc">{book.description || "No description provided yet."}</p>
                <div className="catalog-footer">
                  <Badge status={canBook ? "available" : "unavailable"}>
                    {canBook
                      ? `${count} available${pickupDate ? " that day" : ""}`
                      : pickupDate ? "None free that day" : "Out of stock"}
                  </Badge>
                  <button className="button primary auto" onClick={() => handleAction(book)} disabled={!canBook}>
                    Book for Pickup
                  </button>
                </div>
              </article>
            );
          })}
        </section>
      )}
    </AppLayout>
  );
}

export default Catalog;
