package edu.cit.becera.lrbms.features.reservation.service;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.entities.Reservation;
import edu.cit.becera.lrbms.features.reservation.dto.CreateReservationRequest;
import edu.cit.becera.lrbms.features.reservation.dto.ReservationResponse;
import edu.cit.becera.lrbms.repositories.BookRepository;
import edu.cit.becera.lrbms.repositories.FineRepository;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import edu.cit.becera.lrbms.repositories.ReservationRepository;
import edu.cit.becera.lrbms.repositories.TransactionRepository;
import edu.cit.becera.lrbms.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final FineRepository fineRepository;
    private final TransactionRepository transactionRepository;

    public ReservationService(ReservationRepository reservationRepository, MemberRepository memberRepository,
                               BookRepository bookRepository, FineRepository fineRepository,
                               TransactionRepository transactionRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
        this.fineRepository = fineRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<ReservationResponse> getReservationsForMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Member not found"));
        return reservationRepository.findByMember(member).stream().map(this::toResponse).toList();
    }

    public ReservationResponse create(Long memberId, CreateReservationRequest request) {
        if (request == null || request.getResourceId() == null) {
            throw new IllegalArgumentException("Resource is required");
        }
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Member not found"));
        Book book = bookRepository.findById(request.getResourceId()).orElseThrow(() -> new IllegalArgumentException("Resource not found"));

        // Reservations are the out-of-stock waitlist only - an in-stock title is borrowed directly.
        if (book.getAvailableCopies() != null && book.getAvailableCopies() > 0) {
            throw new IllegalStateException("This title currently has copies available — borrow it instead of reserving it");
        }
        if (fineRepository.existsByMemberAndPaymentStatus(member, "UNPAID")) {
            throw new IllegalStateException("Member has unpaid fines and cannot make new reservations");
        }
        boolean hasOverdue = transactionRepository.findByMemberAndStatus(member, "ACTIVE").stream()
                .anyMatch(t -> t.getDueDate().isBefore(LocalDate.now()));
        if (hasOverdue) {
            throw new IllegalStateException("Member has overdue items and cannot make new reservations");
        }
        boolean duplicate = !reservationRepository
                .findByMemberAndBookAndStatusIn(member, book, List.of("PENDING", "APPROVED"))
                .isEmpty();
        if (duplicate) {
            throw new IllegalStateException("This title is already reserved for this member");
        }

        Reservation reservation = new Reservation();
        reservation.setMember(member);
        reservation.setBook(book);
        reservation.setReservationDate(LocalDate.now());
        reservation.setStatus("PENDING");
        reservation = reservationRepository.save(reservation);

        return toResponse(reservation);
    }

    public ReservationResponse updateStatus(Long id, String status) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        if (status == null || (!status.equals("APPROVED") && !status.equals("REJECTED"))) {
            throw new IllegalArgumentException("Status must be APPROVED or REJECTED");
        }
        reservation.setStatus(status);
        return toResponse(reservationRepository.save(reservation));
    }

    public void cancel(Long id, CurrentUser requester) {
        Reservation reservation = reservationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        boolean isOwner = requester != null && requester.owns(reservation.getMember().getId());
        boolean isStaff = requester != null && requester.isStaff();
        if (!isOwner && !isStaff) {
            throw new AccessDeniedException("Not allowed to cancel this reservation");
        }
        if (!isStaff && !"PENDING".equals(reservation.getStatus())) {
            throw new IllegalStateException("Only pending reservations can be cancelled");
        }
        reservationRepository.delete(reservation);
    }

    /**
     * Waitlist position among other pending requests for the same title (1-based). Matched by id
     * since reservations loaded across separate repository calls are distinct Java instances.
     */
    private Integer computeQueuePosition(Reservation reservation) {
        if (!"PENDING".equals(reservation.getStatus())) {
            return null;
        }
        List<Reservation> queue = reservationRepository.findByBookAndStatusOrderByIdAsc(reservation.getBook(), "PENDING");
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId().equals(reservation.getId())) {
                return i + 1;
            }
        }
        return null;
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.from(reservation, computeQueuePosition(reservation));
    }
}
