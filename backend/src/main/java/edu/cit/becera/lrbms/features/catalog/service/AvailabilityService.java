package edu.cit.becera.lrbms.features.catalog.service;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.entities.Reservation;
import edu.cit.becera.lrbms.entities.Transaction;
import edu.cit.becera.lrbms.features.catalog.dto.BookAvailabilityResponse;
import edu.cit.becera.lrbms.repositories.BookRepository;
import edu.cit.becera.lrbms.repositories.ReservationRepository;
import edu.cit.becera.lrbms.repositories.TransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    /** How long a booking holds a copy from its pickup date; mirrors the standard loan length. */
    public static final int HOLD_DAYS = 14;

    private static final List<String> ACTIVE_HOLD_STATUSES = List.of("PENDING", "APPROVED");

    private final BookRepository bookRepository;
    private final TransactionRepository transactionRepository;
    private final ReservationRepository reservationRepository;

    public AvailabilityService(BookRepository bookRepository, TransactionRepository transactionRepository,
                                ReservationRepository reservationRepository) {
        this.bookRepository = bookRepository;
        this.transactionRepository = transactionRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<BookAvailabilityResponse> availabilityOn(LocalDate date) {
        List<Book> books = bookRepository.findAll();
        Map<Long, List<Transaction>> loansByBook = transactionRepository.findByStatus("ACTIVE").stream()
                .collect(Collectors.groupingBy(t -> t.getBook().getId()));
        Map<Long, List<Reservation>> holdsByBook = reservationRepository.findByStatusIn(ACTIVE_HOLD_STATUSES).stream()
                .collect(Collectors.groupingBy(r -> r.getBook().getId()));

        return books.stream().map(book -> {
            int available = AvailabilityCalculator.availableOnDate(
                    book, date,
                    loansByBook.getOrDefault(book.getId(), List.of()),
                    holdsByBook.getOrDefault(book.getId(), List.of()),
                    HOLD_DAYS);
            return BookAvailabilityResponse.from(book, available);
        }).toList();
    }

    /** Copies free for a single title on a date — used to gate a booking request. */
    public int availableOnDate(Book book, LocalDate date) {
        return AvailabilityCalculator.availableOnDate(
                book, date,
                transactionRepository.findByBookAndStatus(book, "ACTIVE"),
                reservationRepository.findByBookAndStatusIn(book, ACTIVE_HOLD_STATUSES),
                HOLD_DAYS);
    }
}
