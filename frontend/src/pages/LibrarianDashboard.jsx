import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import { createBook, deleteBook, getBooks, updateBook } from "../services/bookService";
// Frontend catalog integration 
function LibrarianDashboard() {
  const navigate = useNavigate();
  const user = JSON.parse(localStorage.getItem("user") || "null");

  const [books, setBooks] = useState([]);
  const [bookForm, setBookForm] = useState({ id: null, title: "", author: "", isbn: "", category: "", description: "", coverImage: "", availableCopies: 1 });
  const [message, setMessage] = useState("");

  const loadData = async () => {
    try {
      const response = await getBooks();
      setBooks(response.data);
    } catch (error) {
      console.error(error);
      setMessage("Unable to load the book catalog.");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("user");
    navigate("/login");
  };

  const handleBookChange = (event) => {
    const { name, value } = event.target;
    setBookForm((current) => ({ ...current, [name]: name === "availableCopies" ? Number(value) : value }));
  };

  const handleBookSubmit = async (event) => {
    event.preventDefault();
    try {
      if (bookForm.id) {
        await updateBook(bookForm.id, bookForm);
        setMessage("Book updated successfully.");
      } else {
        await createBook(bookForm);
        setMessage("Book added to the catalog.");
      }
      setBookForm({ id: null, title: "", author: "", isbn: "", category: "", description: "", coverImage: "", availableCopies: 1 });
      loadData();
    } catch (error) {
      setMessage(error.response?.data?.error || "Unable to save book.");
    }
  };

  const handleEdit = (book) => {
    setBookForm({ ...book, availableCopies: book.availableCopies ?? 1 });
  };

  const handleDelete = async (id) => {
    try {
      await deleteBook(id);
      setMessage("Book removed from the catalog.");
      loadData();
    } catch (error) {
      setMessage("Unable to remove this book.");
    }
  };

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Librarian Workspace</p>
          <h1>Hello, {user?.firstName || "Librarian"}.</h1>
          <p>Manage the catalog, update availability, and keep each title searchable for members.</p>
        </div>
        <button className="button secondary logout-btn" onClick={handleLogout}>Logout</button>
      </header>

      {message ? <p className="message-banner">{message}</p> : null}

      <section className="dashboard-card" style={{ marginBottom: "1.5rem" }}>
        <h2>{bookForm.id ? "Edit book" : "Add a new book"}</h2>
        <form onSubmit={handleBookSubmit} className="form-grid">
          <input className="form-input" name="title" placeholder="Title" value={bookForm.title} onChange={handleBookChange} required />
          <input className="form-input" name="author" placeholder="Author" value={bookForm.author} onChange={handleBookChange} required />
          <input className="form-input" name="isbn" placeholder="ISBN" value={bookForm.isbn} onChange={handleBookChange} required />
          <input className="form-input" name="category" placeholder="Category" value={bookForm.category} onChange={handleBookChange} required />
          <input className="form-input" name="coverImage" placeholder="Cover image URL" value={bookForm.coverImage} onChange={handleBookChange} />
          <input className="form-input" name="availableCopies" type="number" min="0" placeholder="Available copies" value={bookForm.availableCopies} onChange={handleBookChange} required />
          <textarea className="form-input" name="description" placeholder="Description" value={bookForm.description} onChange={handleBookChange} rows="3" />
          <button className="button primary" type="submit">{bookForm.id ? "Save book" : "Create book"}</button>
        </form>
      </section>

      <section className="dashboard-card">
        <h2>Catalog overview</h2>
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Author</th>
                <th>ISBN</th>
                <th>Category</th>
                <th>Copies</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {books.map((book) => (
                <tr key={book.id}>
                  <td>{book.title}</td>
                  <td>{book.author}</td>
                  <td>{book.isbn}</td>
                  <td>{book.category}</td>
                  <td>{book.availableCopies}</td>
                  <td>
                    <button type="button" className="table-action" onClick={() => handleEdit(book)}>Edit</button>
                    <button type="button" className="table-action danger" onClick={() => handleDelete(book.id)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

export default LibrarianDashboard;
