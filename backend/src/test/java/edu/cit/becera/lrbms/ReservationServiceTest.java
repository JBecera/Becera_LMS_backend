package edu.cit.becera.lrbms;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.entities.Reservation;
import edu.cit.becera.lrbms.entities.Transaction;
import edu.cit.becera.lrbms.util.AppClock;
import edu.cit.becera.lrbms.features.reservation.dto.CreateReservationRequest;
import edu.cit.becera.lrbms.features.reservation.dto.ReservationResponse;
import edu.cit.becera.lrbms.features.reservation.service.ReservationService;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private BookRepository bookRepository;
    @Mock private FineRepository fineRepository;
    @Mock private TransactionRepository transactionRepository;

    private ReservationService reservationService;
    private Member member;
    private Book book;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(reservationRepository, memberRepository, bookRepository, fineRepository, transactionRepository);

        member = new Member();
        member.setId(1L);
        member.setFirstName("Jane");
        member.setLastName("Doe");

        book = new Book();
        book.setId(2L);
        book.setTitle("Clean Code");
        book.setTotalCopies(1);
        book.setAvailableCopies(1);

        lenient().when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        lenient().when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        lenient().when(fineRepository.existsByMemberAndPaymentStatus(member, "UNPAID")).thenReturn(false);
        lenient().when(transactionRepository.findByMemberAndStatus(member, "ACTIVE")).thenReturn(List.of());
        lenient().when(transactionRepository.findByBookAndStatus(any(), any())).thenReturn(List.of());
        lenient().when(reservationRepository.findByMemberAndBookAndStatusIn(any(), any(), any())).thenReturn(List.of());
        lenient().when(reservationRepository.findByBookAndStatusIn(any(), any())).thenReturn(List.of());
        lenient().when(reservationRepository.findByBookAndStatusOrderByIdAsc(any(), any())).thenAnswer(inv -> List.of());
        lenient().when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            if (r.getId() == null) r.setId(5L);
            return r;
        });
    }

    private CreateReservationRequest request() {
        CreateReservationRequest request = new CreateReservationRequest();
        request.setResourceId(2L);
        return request;
    }

    @Test
    void createShouldSucceedWhenACopyIsFreeOnThePickupDate() {
        book.setTotalCopies(3);

        ReservationResponse response = reservationService.create(1L, request());

        assertEquals("PENDING", response.getStatus());
    }

    @Test
    void createShouldRejectWhenNoCopiesFreeOnThePickupDate() {
        book.setTotalCopies(1);
        // The single copy is out on loan across today (the default pickup date), so nothing is free.
        Transaction loan = new Transaction();
        loan.setCheckOutDate(AppClock.today().minusDays(2));
        loan.setDueDate(AppClock.today().plusDays(5));
        loan.setStatus("ACTIVE");
        when(transactionRepository.findByBookAndStatus(book, "ACTIVE")).thenReturn(List.of(loan));

        assertThrows(IllegalStateException.class, () -> reservationService.create(1L, request()));
    }

    @Test
    void createShouldRejectDuplicateActiveReservation() {
        book.setAvailableCopies(0);
        Reservation existing = new Reservation();
        existing.setStatus("PENDING");
        when(reservationRepository.findByMemberAndBookAndStatusIn(any(), any(), any())).thenReturn(List.of(existing));

        assertThrows(IllegalStateException.class, () -> reservationService.create(1L, request()));
    }

    @Test
    void createShouldReportQueuePositionAmongPendingReservations() {
        book.setAvailableCopies(0);

        Reservation aheadInLine = new Reservation();
        aheadInLine.setId(3L);
        aheadInLine.setStatus("PENDING");

        Reservation newlyCreated = new Reservation();
        newlyCreated.setId(5L);
        newlyCreated.setMember(member);
        newlyCreated.setBook(book);
        newlyCreated.setStatus("PENDING");

        when(reservationRepository.save(any(Reservation.class))).thenReturn(newlyCreated);
        when(reservationRepository.findByBookAndStatusOrderByIdAsc(book, "PENDING"))
                .thenReturn(List.of(aheadInLine, newlyCreated));

        ReservationResponse response = reservationService.create(1L, request());

        assertEquals("PENDING", response.getStatus());
        assertEquals(2, response.getQueuePosition());
    }

    @Test
    void queuePositionShouldBeNullForNonPendingReservations() {
        Reservation reservation = new Reservation();
        reservation.setId(9L);
        reservation.setMember(member);
        reservation.setBook(book);
        reservation.setStatus("APPROVED");
        when(reservationRepository.findById(9L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(reservation)).thenReturn(reservation);

        ReservationResponse response = reservationService.updateStatus(9L, "APPROVED", null);

        assertNull(response.getQueuePosition());
    }

    @Test
    void cancelShouldRejectWhenRequesterIsNotOwnerOrStaff() {
        Reservation reservation = new Reservation();
        reservation.setId(5L);
        reservation.setMember(member);
        reservation.setBook(book);
        reservation.setStatus("PENDING");
        when(reservationRepository.findById(5L)).thenReturn(Optional.of(reservation));

        CurrentUser otherMember = new CurrentUser(99L, "MEMBER");

        assertThrows(AccessDeniedException.class, () -> reservationService.cancel(5L, otherMember));
    }
}
