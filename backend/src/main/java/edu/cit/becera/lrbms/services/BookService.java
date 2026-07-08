package edu.cit.becera.lrbms.services;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.repositories.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    public List<Book> searchBooks(String query) {
        if (query == null || query.isBlank()) {
            return repository.findAll();
        }
        return repository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }

    public Book createBook(Book book) {
        validateBook(book);
        return repository.save(book);
    }

    public Book updateBook(Long id, Book updatedBook) {
        Book existingBook = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        validateBook(updatedBook);
        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setIsbn(updatedBook.getIsbn());
        existingBook.setCategory(updatedBook.getCategory());
        existingBook.setDescription(updatedBook.getDescription());
        existingBook.setCoverImage(updatedBook.getCoverImage());
        existingBook.setAvailableCopies(updatedBook.getAvailableCopies());
        return repository.save(existingBook);
    }

    public void deleteBook(Long id) {
        repository.deleteById(id);
    }

    public Book reserveBook(Long id) {
        Book book = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies available for reservation");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return repository.save(book);
    }

    private void validateBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book data is required");
        }
        if (book.getTitle() == null || book.getTitle().isBlank()) {
            throw new IllegalArgumentException("Book title is required");
        }
        if (book.getAuthor() == null || book.getAuthor().isBlank()) {
            throw new IllegalArgumentException("Book author is required");
        }
        if (book.getIsbn() == null || book.getIsbn().isBlank()) {
            throw new IllegalArgumentException("Book ISBN is required");
        }
        if (book.getCategory() == null || book.getCategory().isBlank()) {
            throw new IllegalArgumentException("Book category is required");
        }
        if (book.getAvailableCopies() == null || book.getAvailableCopies() < 0) {
            throw new IllegalArgumentException("Available copies must be zero or more");
        }
    }
}
