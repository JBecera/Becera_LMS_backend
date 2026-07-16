import { useEffect, useMemo, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import { getBooks } from "../services/bookService";
import { createReservation } from "../services/reservationService";

function Catalog() {
  const [books, setBooks] = useState([]);
  const [search, setSearch] = useState("");
  const [message, setMessage] = useState("");

  const loadBooks = async (query = "") => {
    try {
      const response = await getBooks(query);
      setBooks(response.data);
    } catch (error) {
      setMessage("Unable to load the catalog right now.");
    }
  };

  useEffect(() => {
    loadBooks();
  }, []);

  const filteredBooks = useMemo(() => {
    const query = search.toLowerCase();
    if (!query) return books;
    return books.filter((book) => `${book.title} ${book.author}`.toLowerCase().includes(query));
  }, [books, search]);

  const handleReserve = async (bookId) => {
    try {
      await createReservation(bookId);
      setMessage("You're on the waitlist. A librarian will approve your reservation once a copy is free.");
      loadBooks(search);
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to reserve this title right now.");
    }
  };

  return (
    <AppLayout
      eyebrow="Catalog"
      title="Search the catalog"
      description="Browse by title or author, check real-time availability, and join the waitlist when every copy is checked out."
    >
      {message ? <p className="message-banner">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        <input
          className="form-input"
          placeholder="Search by title or author"
          value={search}
          onChange={(event) => {
            setSearch(event.target.value);
            loadBooks(event.target.value);
          }}
        />
      </section>

      {filteredBooks.length === 0 ? (
        <section className="panel-card">
          <EmptyState icon="catalog" title="No matching titles" description="Try a different title or author, or clear your search." />
        </section>
      ) : (
        <section className="dashboard-grid" style={{ marginTop: "1.5rem" }}>
          {filteredBooks.map((book) => (
            <article key={book.id} className="dashboard-card catalog-card">
              <div className="catalog-cover">
                {book.coverImage ? <img src={book.coverImage} alt={book.title} /> : <span>{book.title?.[0] || "B"}</span>}
              </div>
              <h3>{book.title}</h3>
              <p className="catalog-meta">{book.author}</p>
              <p className="catalog-meta">{book.category}</p>
              <p className="catalog-desc">{book.description || "No description provided yet."}</p>
              <div className="catalog-footer">
                <Badge status={book.availableCopies > 0 ? "available" : "unavailable"}>
                  {book.availableCopies > 0 ? `${book.availableCopies} available` : "Out of stock"}
                </Badge>
                {book.availableCopies > 0 ? (
                  <span className="catalog-meta">Ask a librarian to check this out for you.</span>
                ) : (
                  <button className="button primary auto" onClick={() => handleReserve(book.id)}>
                    Join waitlist
                  </button>
                )}
              </div>
            </article>
          ))}
        </section>
      )}
    </AppLayout>
  );
}

export default Catalog;
