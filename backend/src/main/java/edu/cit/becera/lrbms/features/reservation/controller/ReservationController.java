package edu.cit.becera.lrbms.features.reservation.controller;

import edu.cit.becera.lrbms.features.reservation.dto.CreateReservationRequest;
import edu.cit.becera.lrbms.features.reservation.dto.ReservationResponse;
import edu.cit.becera.lrbms.features.reservation.dto.UpdateReservationRequest;
import edu.cit.becera.lrbms.features.reservation.service.ReservationService;
import edu.cit.becera.lrbms.security.CurrentUser;
import edu.cit.becera.lrbms.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public List<ReservationResponse> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getReservationsForMember(@PathVariable Long memberId) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        if (currentUser == null || (!currentUser.owns(memberId) && !currentUser.isStaff())) {
            throw new AccessDeniedException("Not allowed to view these reservations");
        }
        return ResponseEntity.ok(reservationService.getReservationsForMember(memberId));
    }

    @PostMapping
    public ResponseEntity<?> createReservation(@RequestBody CreateReservationRequest request) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Login required"));
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(currentUser.id(), request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReservation(@PathVariable Long id, @RequestBody UpdateReservationRequest request) {
        try {
            return ResponseEntity.ok(reservationService.updateStatus(id, request.getStatus()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        try {
            reservationService.cancel(id, SecurityUtils.currentUser());
            return ResponseEntity.ok(Map.of("message", "Reservation cancelled"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        }
    }
}
