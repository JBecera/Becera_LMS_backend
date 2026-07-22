import { useEffect, useState } from "react";
import AppLayout from "../components/layout/AppLayout";
import Badge from "../components/ui/Badge";
import EmptyState from "../components/ui/EmptyState";
import { useToast } from "../components/ui/ToastProvider";
import { createBook, deleteBook, getBooks, updateBook } from "../services/bookService";

const emptyForm = { id: null, title: "", author: "", isbn: "", category: "", description: "", coverImage: "", totalCopies: 1, availableCopies: 1 };

function ManageCatalog() {
  const toast = useToast();
  const [books, setBooks] = useState([]);
  const [bookForm, setBookForm] = useState(emptyForm);
  const [message, setMessage] = useState("");

  const loadData = async () => {
    try {
      const response = await getBooks();
      setBooks(response.data);
    } catch (error) {
      setMessage("Unable to load the book catalog.");
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const numericFields = ["totalCopies", "availableCopies"];

  const handleBookChange = (event) => {
    const { name, value } = event.target;
    setBookForm((current) => ({ ...current, [name]: numericFields.includes(name) ? Number(value) : value }));
  };

  const handleBookSubmit = async (event) => {
    event.preventDefault();
    try {
      if (bookForm.id) {
        await updateBook(bookForm.id, bookForm);
        toast.success("Book updated successfully.");
      } else {
        await createBook(bookForm);
        toast.success("Book added to the catalog.");
      }
      setBookForm(emptyForm);
      loadData();
    } catch (error) {
      toast.error(error.response?.data?.error || "Unable to save book.");
    }
  };

  const handleEdit = (book) => {
    setBookForm({
      ...book,
      totalCopies: book.totalCopies ?? book.availableCopies ?? 1,
      availableCopies: book.availableCopies ?? 1,
    });
  };

  const handleDelete = async (id) => {
    try {
      await deleteBook(id);
      toast.success("Book removed from the catalog.");
      loadData();
    } catch (error) {
      toast.error("Unable to remove this book.");
    }
  };

  return (
    <AppLayout
      eyebrow="Catalog"
      title="Manage the catalog"
      description="Add new titles, update details, and keep availability accurate for every member search."
      actions={bookForm.id ? <button className="button secondary auto" onClick={() => setBookForm(emptyForm)}>Cancel edit</button> : null}
    >
      {message ? <p className="message-banner">{message}</p> : null}

      <section className="panel-card" style={{ marginTop: 0 }}>
        <h2>{bookForm.id ? "Edit book" : "Add a new book"}</h2>
        <p className="panel-sub">Details shown here are what members see when they search the catalog.</p>
        <form onSubmit={handleBookSubmit} className="form-grid">
          <input className="form-input" name="title" placeholder="Title" value={bookForm.title} onChange={handleBookChange} required />
          <input className="form-input" name="author" placeholder="Author" value={bookForm.author} onChange={handleBookChange} required />
          <input className="form-input" name="isbn" placeholder="ISBN" value={bookForm.isbn} onChange={handleBookChange} required />
          <input className="form-input" name="category" placeholder="Category" value={bookForm.category} onChange={handleBookChange} required />
          <input className="form-input" name="coverImage" placeholder="Cover image URL" value={bookForm.coverImage} onChange={handleBookChange} />
          <input className="form-input" name="totalCopies" type="number" min="0" placeholder="Total copies" value={bookForm.totalCopies} onChange={handleBookChange} required />
          {bookForm.id ? (
            <input className="form-input" name="availableCopies" type="number" min="0" max={bookForm.totalCopies} placeholder="Available copies" value={bookForm.availableCopies} onChange={handleBookChange} required />
          ) : null}
          <textarea className="form-input" name="description" placeholder="Description" value={bookForm.description} onChange={handleBookChange} rows="3" style={{ gridColumn: "1 / -1" }} />
          {!bookForm.id ? (
            <p className="field-hint" style={{ gridColumn: "1 / -1", margin: 0 }}>
              A new title starts fully available with all {bookForm.totalCopies || 0} cop{bookForm.totalCopies === 1 ? "y" : "ies"} on the shelf.
            </p>
          ) : null}
          <button className="button primary auto" type="submit" style={{ gridColumn: "1 / -1", justifySelf: "start" }}>
            {bookForm.id ? "Save book" : "Create book"}
          </button>
        </form>
      </section>

      <section className="panel-card">
        <h2>Catalog overview</h2>
        {books.length === 0 ? (
          <EmptyState icon="catalog" title="Catalog is empty" description="Add your first title above to make it searchable." />
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Author</th>
                  <th>ISBN</th>
                  <th>Category</th>
                  <th>Total</th>
                  <th>Available</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {books.map((book) => (
                  <tr key={book.id}>
                    <td>{book.title}</td>
                    <td>{book.author}</td>
                    <td className="mono">{book.isbn}</td>
                    <td>{book.category}</td>
                    <td className="mono">{book.totalCopies ?? book.availableCopies}</td>
                    <td className="mono">{book.availableCopies}</td>
                    <td><Badge status={book.availableCopies > 0 ? "available" : "unavailable"}>{book.availableCopies > 0 ? "Available" : "Out of stock"}</Badge></td>
                    <td>
                      <button type="button" className="table-action" onClick={() => handleEdit(book)}>Edit</button>
                      <button type="button" className="table-action danger" onClick={() => handleDelete(book.id)}>Delete</button>
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

export default ManageCatalog;
