import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getBooks, reserveBook } from "../services/bookService";
// Frontend catalog integration 
function Dashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user")) || {};
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

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  const handleReserve = async (bookId) => {
    try {
      await reserveBook(bookId);
      setMessage("Reservation request recorded. Availability is updated instantly.");
      loadBooks(search);
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to reserve this title right now.");
    }
  };

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Member Catalog</p>
          <h1>Welcome back, {user?.firstName || "Library User"}.</h1>
          <p>Search the catalog, review real-time availability, and reserve books before they are taken.</p>
        </div>
        <button className="button secondary logout-btn" onClick={handleLogout}>Logout</button>
      </header>

      {message ? <p className="message-banner">{message}</p> : null}

      <section className="dashboard-card" style={{ marginBottom: "1.5rem" }}>
        <h2>Search the catalog</h2>
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

      <section className="dashboard-grid">
        {filteredBooks.map((book) => (
          <article key={book.id} className="dashboard-card catalog-card">
            <div className="catalog-cover">{book.coverImage ? <img src={book.coverImage} alt={book.title} /> : <span>{book.title?.[0] || "B"}</span>}</div>
            <h3>{book.title}</h3>
            <p className="catalog-meta">{book.author}</p>
            <p className="catalog-meta">{book.category}</p>
            <p className="catalog-desc">{book.description || "No description provided yet."}</p>
            <div className="catalog-footer">
              <span className={`availability-badge ${book.availableCopies > 0 ? "available" : "unavailable"}`}>
                {book.availableCopies > 0 ? `${book.availableCopies} available` : "Out of stock"}
              </span>
              <button className="button primary small" onClick={() => handleReserve(book.id)} disabled={book.availableCopies <= 0}>
                Reserve
              </button>
            </div>
          </article>
        ))}
      </section>
    </div>
  );
}

export default Dashboard;
