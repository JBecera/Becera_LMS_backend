package edu.cit.becera.lrbms;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.entities.Fine;
import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.entities.Transaction;
import edu.cit.becera.lrbms.features.transaction.dto.CheckoutRequest;
import edu.cit.becera.lrbms.features.transaction.dto.SelfCheckoutRequest;
import edu.cit.becera.lrbms.features.transaction.dto.TransactionResponse;
import edu.cit.becera.lrbms.features.transaction.service.TransactionService;
import edu.cit.becera.lrbms.repositories.BookRepository;
import edu.cit.becera.lrbms.repositories.FineRepository;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import edu.cit.becera.lrbms.repositories.ReservationRepository;
import edu.cit.becera.lrbms.repositories.TransactionRepository;
import edu.cit.becera.lrbms.security.CurrentUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private BookRepository bookRepository;
    @Mock private FineRepository fineRepository;
    @Mock private ReservationRepository reservationRepository;

    private TransactionService transactionService;
    private Member member;
    private Book book;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, memberRepository, bookRepository, fineRepository, reservationRepository);

        member = new Member();
        member.setId(1L);
        member.setFirstName("Jane");
        member.setLastName("Doe");

        book = new Book();
        book.setId(2L);
        book.setTitle("Clean Code");
        book.setCategory("Programming");
        book.setAvailableCopies(1);

        lenient().when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        lenient().when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        lenient().when(fineRepository.existsByMemberAndPaymentStatus(member, "UNPAID")).thenReturn(false);
        lenient().when(reservationRepository.findByMemberAndBookAndStatusIn(any(), any(), any())).thenReturn(List.of());
        lenient().when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            if (t.getId() == null) t.setId(10L);
            return t;
        });
    }

    private CheckoutRequest request(LocalDate dueDate) {
        CheckoutRequest request = new CheckoutRequest();
        request.setMemberId(1L);
        request.setResourceId(2L);
        request.setDueDate(dueDate);
        return request;
    }

    private SelfCheckoutRequest selfRequest(LocalDate dueDate) {
        SelfCheckoutRequest request = new SelfCheckoutRequest();
        request.setResourceId(2L);
        request.setDueDate(dueDate);
        return request;
    }

    private final CurrentUser requester = new CurrentUser(1L, "MEMBER");

    @Test
    void checkoutShouldDecrementAvailabilityAndCreateActiveLoan() {
        when(transactionRepository.findByMemberAndStatus(member, "ACTIVE")).thenReturn(List.of());

        TransactionResponse response = transactionService.checkout(request(LocalDate.now().plusDays(7)));

        assertEquals("ACTIVE", response.getStatus());
        assertEquals(0, book.getAvailableCopies());
    }

    @Test
    void checkoutShouldRejectPastDueDate() {
        assertThrows(IllegalArgumentException.class, () -> transactionService.checkout(request(LocalDate.now().minusDays(1))));
    }

    @Test
    void checkoutShouldRejectWhenMemberHasUnpaidFines() {
        when(fineRepository.existsByMemberAndPaymentStatus(member, "UNPAID")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> transactionService.checkout(request(LocalDate.now().plusDays(7))));
    }

    @Test
    void checkoutShouldRejectAtBorrowingLimit() {
        Transaction a = new Transaction();
        a.setDueDate(LocalDate.now().plusDays(3));
        Transaction b = new Transaction();
        b.setDueDate(LocalDate.now().plusDays(3));
        Transaction c = new Transaction();
        c.setDueDate(LocalDate.now().plusDays(3));
        when(transactionRepository.findByMemberAndStatus(member, "ACTIVE")).thenReturn(List.of(a, b, c));

        assertThrows(IllegalStateException.class, () -> transactionService.checkout(request(LocalDate.now().plusDays(7))));
    }

    @Test
    void checkoutShouldRejectWhenNoCopiesAvailable() {
        book.setAvailableCopies(0);
        when(transactionRepository.findByMemberAndStatus(member, "ACTIVE")).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> transactionService.checkout(request(LocalDate.now().plusDays(7))));
    }

    @Test
    void checkoutShouldRejectWhenMemberAlreadyHasActiveLoanForSameBook() {
        Transaction existingLoan = new Transaction();
        existingLoan.setBook(book);
        existingLoan.setDueDate(LocalDate.now().plusDays(3));
        when(transactionRepository.findByMemberAndStatus(member, "ACTIVE")).thenReturn(List.of(existingLoan));

        assertThrows(IllegalStateException.class, () -> transactionService.checkout(request(LocalDate.now().plusDays(7))));
    }

    @Test
    void selfCheckoutShouldDecrementAvailabilityAndCreateActiveLoan() {
        when(transactionRepository.findByMemberAndStatus(member, "ACTIVE")).thenReturn(List.of());

        TransactionResponse response = transactionService.selfCheckout(requester, selfRequest(LocalDate.now().plusDays(7)));

        assertEquals("ACTIVE", response.getStatus());
        assertEquals(0, book.getAvailableCopies());
    }

    @Test
    void selfCheckoutShouldRejectWhenNoCopiesAvailable() {
        book.setAvailableCopies(0);
        when(transactionRepository.findByMemberAndStatus(member, "ACTIVE")).thenReturn(List.of());

        assertThrows(IllegalStateException.class, () -> transactionService.selfCheckout(requester, selfRequest(LocalDate.now().plusDays(7))));
    }

    @Test
    void selfCheckoutShouldRejectWhenMemberAlreadyHasActiveLoanForSameBook() {
        Transaction existingLoan = new Transaction();
        existingLoan.setBook(book);
        existingLoan.setDueDate(LocalDate.now().plusDays(3));
        when(transactionRepository.findByMemberAndStatus(member, "ACTIVE")).thenReturn(List.of(existingLoan));

        assertThrows(IllegalStateException.class, () -> transactionService.selfCheckout(requester, selfRequest(LocalDate.now().plusDays(7))));
    }

    @Test
    void selfCheckoutShouldRejectReturnDateNotAfterBorrowDate() {
        assertThrows(IllegalArgumentException.class, () -> transactionService.selfCheckout(requester, selfRequest(LocalDate.now())));
    }

    @Test
    void selfCheckoutShouldRejectReturnDateBeyondMaxLoanDuration() {
        assertThrows(IllegalArgumentException.class,
                () -> transactionService.selfCheckout(requester, selfRequest(LocalDate.now().plusDays(TransactionService.MAX_LOAN_DAYS + 1))));
    }

    @Test
    void checkInPastDueDateShouldIncrementAvailabilityAndCreateFine() {
        Transaction transaction = new Transaction();
        transaction.setId(10L);
        transaction.setMember(member);
        transaction.setBook(book);
        transaction.setCheckOutDate(LocalDate.now().minusDays(10));
        transaction.setDueDate(LocalDate.now().minusDays(3));
        transaction.setStatus("ACTIVE");
        book.setAvailableCopies(0);

        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction));

        TransactionResponse response = transactionService.checkIn(10L);

        assertEquals("RETURNED", response.getStatus());
        assertEquals(1, book.getAvailableCopies());
        org.mockito.Mockito.verify(fineRepository).save(any(Fine.class));
    }
}
