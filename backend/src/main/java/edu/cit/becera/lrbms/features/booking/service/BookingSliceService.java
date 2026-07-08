package edu.cit.becera.lrbms.features.booking.service;

import edu.cit.becera.lrbms.entities.Booking;
import edu.cit.becera.lrbms.features.booking.dto.CreateBookingRequest;
import edu.cit.becera.lrbms.repositories.BookingRepository;
import edu.cit.becera.lrbms.repositories.MemberRepository;
import edu.cit.becera.lrbms.repositories.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingSliceService {
    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final MemberRepository memberRepository;

    public BookingSliceService(BookingRepository bookingRepository, ResourceRepository resourceRepository, MemberRepository memberRepository) {
        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
        this.memberRepository = memberRepository;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking createBooking(CreateBookingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Booking data is required");
        }
        if (request.getResourceId() == null) {
            throw new IllegalArgumentException("Resource is required");
        }
        if (request.getMemberId() == null) {
            throw new IllegalArgumentException("Member is required");
        }

        Booking booking = new Booking();
        booking.setResource(resourceRepository.findById(request.getResourceId()).orElseThrow(() -> new IllegalArgumentException("Resource not found")));
        booking.setMember(memberRepository.findById(request.getMemberId()).orElseThrow(() -> new IllegalArgumentException("Member not found")));
        booking.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "PENDING" : request.getStatus());
        return bookingRepository.save(booking);
    }

    public Booking updateBooking(Long id, CreateBookingRequest request) {
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        booking.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? booking.getStatus() : request.getStatus());
        return bookingRepository.save(booking);
    }
}
