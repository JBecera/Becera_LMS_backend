package edu.cit.becera.lrbms.features.catalog.service;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.features.catalog.dto.BookRequest;
import edu.cit.becera.lrbms.repositories.BookRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CatalogService {
    private final BookRepository bookRepository;

    public CatalogService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> searchBooks(String query) {
        if (query == null || query.isBlank()) {
            return bookRepository.findAll();
        }
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }

    public Book createBook(BookRequest request) {
        validateBook(request);
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setDescription(request.getDescription());
        book.setCoverImage(request.getCoverImage());
        book.setAvailableCopies(request.getAvailableCopies() == null ? 1 : request.getAvailableCopies());
        return bookRepository.save(book);
    }

    public Book updateBook(Long id, BookRequest request) {
        Book existing = bookRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        validateBook(request);
        existing.setTitle(request.getTitle());
        existing.setAuthor(request.getAuthor());
        existing.setIsbn(request.getIsbn());
        existing.setCategory(request.getCategory());
        existing.setDescription(request.getDescription());
        existing.setCoverImage(request.getCoverImage());
        existing.setAvailableCopies(request.getAvailableCopies() == null ? existing.getAvailableCopies() : request.getAvailableCopies());
        return bookRepository.save(existing);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
//Reservation workflow
    public Book reserveBook(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies available for reservation");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return bookRepository.save(book);
    }

    private void validateBook(BookRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Book data is required");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Book title is required");
        }
        if (request.getAuthor() == null || request.getAuthor().isBlank()) {
            throw new IllegalArgumentException("Book author is required");
        }
        if (request.getIsbn() == null || request.getIsbn().isBlank()) {
            throw new IllegalArgumentException("Book ISBN is required");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new IllegalArgumentException("Book category is required");
        }
        if (request.getAvailableCopies() == null || request.getAvailableCopies() < 0) {
            throw new IllegalArgumentException("Available copies must be zero or more");
        }
    }
}
