package edu.cit.becera.lrbms.services;

import edu.cit.becera.lrbms.entities.Booking;
import edu.cit.becera.lrbms.repositories.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository repository;

    public BookingService(BookingRepository repository) {
        this.repository = repository;
    }

    public List<Booking> getAllBookings() {
        return repository.findAll();
    }

    public Booking saveBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking data is required");
        }
        if (booking.getResource() == null) {
            throw new IllegalArgumentException("Resource is required");
        }
        if (booking.getMember() == null) {
            throw new IllegalArgumentException("Member is required");
        }
        return repository.save(booking);
    }

    public Booking updateBooking(Long id, Booking updatedBooking) {
        Booking booking = repository.findById(id).orElseThrow();
        booking.setStatus(updatedBooking.getStatus());
        return repository.save(booking);
    }
}
