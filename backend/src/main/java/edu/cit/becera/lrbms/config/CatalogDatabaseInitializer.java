package edu.cit.becera.lrbms.config;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.repositories.BookRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogDatabaseInitializer implements ApplicationRunner {

    private record SeedBook(String title, String author, String isbn, String category, String description, int copies) {
    }

    /**
     * Demo/deployment catalog for a BSIT curriculum. Every entry starts fully available (no holds,
     * checkouts, or waitlists pre-seeded) - a freshly launched library where everything is on the shelf.
     */
    private static final List<SeedBook> BSIT_SEED_CATALOG = List.of(
            new SeedBook("Clean Code", "Robert C. Martin", "9780132350884", "Software Engineering",
                    "A handbook of agile software craftsmanship focused on writing readable, maintainable code.", 4),
            new SeedBook("The Pragmatic Programmer", "David Thomas & Andrew Hunt", "9780135957059", "Software Engineering",
                    "Practical, timeless tips for becoming a more effective and adaptable software developer.", 3),
            new SeedBook("Introduction to Algorithms", "Cormen, Leiserson, Rivest, Stein", "9780262046305", "Algorithms",
                    "The standard reference on algorithm design, analysis, and computational complexity.", 3),
            new SeedBook("Computer Networking: A Top-Down Approach", "Kurose & Ross", "9780133594140", "Networking",
                    "An application-first introduction to how the internet and computer networks operate.", 3),
            new SeedBook("Database System Concepts", "Silberschatz, Korth, Sudarshan", "9780078022159", "Databases",
                    "A comprehensive foundation in relational database design, theory, and implementation.", 3),
            new SeedBook("Cryptography and Network Security", "William Stallings", "9780134444284", "Information Assurance",
                    "Core principles of cryptography and how they secure modern networked systems.", 2),
            new SeedBook("Design Patterns: Elements of Reusable Object-Oriented Software",
                    "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides", "9780201633610", "Software Design",
                    "The classic catalog of reusable object-oriented design patterns from the \"Gang of Four\".", 2),
            new SeedBook("Operating System Concepts", "Silberschatz, Galvin, Gagne", "9781118063330", "Systems",
                    "Core concepts behind process management, memory, storage, and operating system design.", 3),
            new SeedBook("Software Engineering", "Ian Sommerville", "9780133943030", "Software Engineering",
                    "A broad survey of software engineering practices across the full development lifecycle.", 3),
            new SeedBook("Artificial Intelligence: A Modern Approach", "Stuart Russell & Peter Norvig", "9780134610993", "Artificial Intelligence",
                    "The definitive introductory text spanning search, knowledge representation, learning, and AI agents.", 2)
    );

    private final JdbcTemplate jdbcTemplate;
    private final BookRepository bookRepository;

    public CatalogDatabaseInitializer(JdbcTemplate jdbcTemplate, BookRepository bookRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        backfillTotalCopies();
        dropUnusedReservationHoldColumn();
        seedBsitCatalog();
    }

    /**
     * "total_copies" is a newer column - rows created before it existed have it NULL. Backfill them
     * from their current available count so nothing shows a blank/zero total after this deploy.
     */
    private void backfillTotalCopies() {
        jdbcTemplate.execute("ALTER TABLE books ADD COLUMN IF NOT EXISTS total_copies INTEGER");
        jdbcTemplate.execute("UPDATE books SET total_copies = available_copies WHERE total_copies IS NULL");
    }

    /**
     * An earlier iteration let members place an instant "hold" on an in-stock title, tracked with a
     * pickup deadline. That model was replaced by direct self-checkout, so the column is now dead -
     * drop it if a prior deploy created it. Reservations aren't "catalog" data, but this runner is
     * the established home for one-off schema fixups beyond Hibernate's ddl-auto=update.
     */
    private void dropUnusedReservationHoldColumn() {
        jdbcTemplate.execute("ALTER TABLE reservations DROP COLUMN IF EXISTS pickup_deadline");
    }

    /**
     * Additive only - never deletes existing rows. Pre-existing placeholder/demo titles are left for
     * a librarian to remove manually via Manage Catalog, since a blind delete here could collide with
     * real reservation/transaction/fine history already tied to those rows in a live deployment.
     */
    private void seedBsitCatalog() {
        for (SeedBook seed : BSIT_SEED_CATALOG) {
            if (bookRepository.existsByIsbn(seed.isbn())) {
                continue;
            }
            Book book = new Book();
            book.setTitle(seed.title());
            book.setAuthor(seed.author());
            book.setIsbn(seed.isbn());
            book.setCategory(seed.category());
            book.setDescription(seed.description());
            book.setTotalCopies(seed.copies());
            book.setAvailableCopies(seed.copies());
            bookRepository.save(book);
        }
    }
}
