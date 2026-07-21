package edu.cit.becera.lrbms.features.catalog.service;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.features.catalog.dto.BookRequest;
import edu.cit.becera.lrbms.repositories.BookRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

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

    public Optional<Book> getBook(Long id) {
        return bookRepository.findById(id);
    }

    public Book createBook(BookRequest request) {
        validateBook(request);
        int totalCopies = request.getTotalCopies() == null ? 1 : request.getTotalCopies();

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setDescription(request.getDescription());
        book.setCoverImage(request.getCoverImage());
        book.setTotalCopies(totalCopies);
        // A freshly added book always starts fully available - nothing can be checked out or on
        // hold yet, regardless of what an availableCopies value the request might have carried.
        book.setAvailableCopies(totalCopies);
        return bookRepository.save(book);
    }

    public Book updateBook(Long id, BookRequest request) {
        Book existing = bookRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        validateBook(request);

        // existing.getTotalCopies() can still be null for a book row created before this field
        // existed and not yet backfilled - fall back to its available count rather than risk an NPE.
        Integer existingTotalCopies = existing.getTotalCopies() != null ? existing.getTotalCopies() : existing.getAvailableCopies();
        int totalCopies = request.getTotalCopies() == null ? existingTotalCopies : request.getTotalCopies();
        int availableCopies = request.getAvailableCopies() == null ? existing.getAvailableCopies() : request.getAvailableCopies();
        if (availableCopies > totalCopies) {
            throw new IllegalArgumentException("Available copies cannot exceed total copies");
        }

        existing.setTitle(request.getTitle());
        existing.setAuthor(request.getAuthor());
        existing.setIsbn(request.getIsbn());
        existing.setCategory(request.getCategory());
        existing.setDescription(request.getDescription());
        existing.setCoverImage(request.getCoverImage());
        existing.setTotalCopies(totalCopies);
        existing.setAvailableCopies(availableCopies);
        return bookRepository.save(existing);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
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
        if (request.getTotalCopies() != null && request.getTotalCopies() < 0) {
            throw new IllegalArgumentException("Total copies must be zero or more");
        }
        if (request.getAvailableCopies() != null && request.getAvailableCopies() < 0) {
            throw new IllegalArgumentException("Available copies must be zero or more");
        }
    }
}
