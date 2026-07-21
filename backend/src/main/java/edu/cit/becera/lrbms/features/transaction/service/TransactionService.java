package edu.cit.becera.lrbms.features.transaction.service;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.entities.Fine;
import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.entities.Transaction;
import edu.cit.becera.lrbms.features.transaction.dto.CheckoutRequest;
import edu.cit.becera.lrbms.features.transaction.dto.SelfCheckoutRequest;
import edu.cit.becera.lrbms.features.transaction.dto.TransactionResponse;
import edu.cit.becera.lrbms.repositories.BookRepository;
import edu.cit.becera.lrbms.repositories.FineRepository;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import edu.cit.becera.lrbms.repositories.ReservationRepository;
import edu.cit.becera.lrbms.repositories.TransactionRepository;
import edu.cit.becera.lrbms.security.CurrentUser;
import edu.cit.becera.lrbms.util.AppClock;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TransactionService {

    public static final int MAX_ACTIVE_LOANS = 3;
    public static final double FINE_PER_DAY_LATE = 5.0;
    /** Members set their own return date when self-checking-out; it can't exceed this many days out. */
    public static final int MAX_LOAN_DAYS = 14;

    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;

    public TransactionService(TransactionRepository transactionRepository, MemberRepository memberRepository,
                               BookRepository bookRepository, FineRepository fineRepository,
                               ReservationRepository reservationRepository) {
        this.transactionRepository = transactionRepository;
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
        this.fineRepository = fineRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll().stream().map(TransactionResponse::from).toList();
    }

    public List<TransactionResponse> getTransactionsForMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Member not found"));
        return transactionRepository.findByMember(member).stream().map(TransactionResponse::from).toList();
    }

    public TransactionResponse checkout(CheckoutRequest request) {
        if (request == null || request.getMemberId() == null || request.getResourceId() == null) {
            throw new IllegalArgumentException("Member and resource are required");
        }
        if (request.getDueDate() == null) {
            throw new IllegalArgumentException("Due date is required");
        }
        if (!request.getDueDate().isAfter(AppClock.today())) {
            throw new IllegalArgumentException("Due date must be after today");
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        Book book = bookRepository.findById(request.getResourceId())
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        validateBorrowEligibility(member, book);
        return performCheckout(member, book, request.getDueDate());
    }

    /**
     * Lets a member book an in-stock title entirely online, without a librarian mediating the
     * checkout - the member picks their own return date, within the library's borrowing rules.
     */
    public TransactionResponse selfCheckout(CurrentUser requester, SelfCheckoutRequest request) {
        if (requester == null) {
            throw new IllegalArgumentException("Login required");
        }
        if (request == null || request.getResourceId() == null) {
            throw new IllegalArgumentException("Resource is required");
        }
        if (request.getDueDate() == null) {
            throw new IllegalArgumentException("Return date is required");
        }
        validateDueDate(request.getDueDate());

        Member member = memberRepository.findById(requester.id())
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        Book book = bookRepository.findById(request.getResourceId())
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        validateBorrowEligibility(member, book);
        return performCheckout(member, book, request.getDueDate());
    }

    private void validateBorrowEligibility(Member member, Book book) {
        if (fineRepository.existsByMemberAndPaymentStatus(member, "UNPAID")) {
            throw new IllegalStateException("Member has unpaid fines and cannot borrow additional items");
        }

        List<Transaction> activeLoans = transactionRepository.findByMemberAndStatus(member, "ACTIVE");
        boolean hasOverdue = activeLoans.stream().anyMatch(t -> t.getDueDate().isBefore(AppClock.today()));
        if (hasOverdue) {
            throw new IllegalStateException("Member has overdue items and cannot borrow additional items");
        }
        if (activeLoans.size() >= MAX_ACTIVE_LOANS) {
            throw new IllegalStateException("Member has reached the borrowing limit of " + MAX_ACTIVE_LOANS + " items");
        }
        boolean alreadyBorrowed = activeLoans.stream().anyMatch(t -> book.equals(t.getBook()));
        if (alreadyBorrowed) {
            throw new IllegalStateException("Member already has an active loan for this title");
        }
        if (book.getAvailableCopies() == null || book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("No copies available for checkout");
        }
    }

    private void validateDueDate(LocalDate dueDate) {
        LocalDate today = AppClock.today();
        if (!dueDate.isAfter(today)) {
            throw new IllegalArgumentException("Return date must be after the borrow date");
        }
        if (dueDate.isAfter(today.plusDays(MAX_LOAN_DAYS))) {
            throw new IllegalArgumentException("Return date cannot be more than " + MAX_LOAN_DAYS + " days from today");
        }
    }

    private TransactionResponse performCheckout(Member member, Book book, LocalDate dueDate) {
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Transaction transaction = new Transaction();
        transaction.setMember(member);
        transaction.setBook(book);
        transaction.setCheckOutDate(AppClock.today());
        transaction.setDueDate(dueDate);
        transaction.setStatus("ACTIVE");
        transaction = transactionRepository.save(transaction);

        reservationRepository.findByMemberAndBookAndStatusIn(member, book, List.of("PENDING", "APPROVED"))
                .forEach(reservation -> {
                    reservation.setStatus("COMPLETED");
                    reservationRepository.save(reservation);
                });

        return TransactionResponse.from(transaction);
    }

    public TransactionResponse checkIn(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        if (!"ACTIVE".equals(transaction.getStatus())) {
            throw new IllegalStateException("This item has already been returned");
        }

        LocalDate today = AppClock.today();
        transaction.setCheckInDate(today);
        transaction.setStatus("RETURNED");
        transaction = transactionRepository.save(transaction);

        Book book = transaction.getBook();
        book.setAvailableCopies((book.getAvailableCopies() == null ? 0 : book.getAvailableCopies()) + 1);
        bookRepository.save(book);

        if (today.isAfter(transaction.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(transaction.getDueDate(), today);
            Fine fine = new Fine();
            fine.setMember(transaction.getMember());
            fine.setTransaction(transaction);
            fine.setAmount(daysLate * FINE_PER_DAY_LATE);
            fine.setReason("Overdue return (" + daysLate + " day" + (daysLate == 1 ? "" : "s") + " late)");
            fine.setPaymentStatus("UNPAID");
            fine.setDateIssued(today);
            fineRepository.save(fine);
        }

        return TransactionResponse.from(transaction);
    }
}
