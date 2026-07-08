package edu.cit.becera.lrbms.features.booking.controller;

import edu.cit.becera.lrbms.entities.Booking;
import edu.cit.becera.lrbms.features.booking.dto.CreateBookingRequest;
import edu.cit.becera.lrbms.features.booking.service.BookingSliceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class BookingSliceController {
    private final BookingSliceService bookingSliceService;

    public BookingSliceController(BookingSliceService bookingSliceService) {
        this.bookingSliceService = bookingSliceService;
    }

    @GetMapping
    public List<Booking> getAllBookings() {
        return bookingSliceService.getAllBookings();
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody CreateBookingRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(bookingSliceService.createBooking(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @RequestBody CreateBookingRequest request) {
        try {
            return ResponseEntity.ok(bookingSliceService.updateBooking(id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }
}
