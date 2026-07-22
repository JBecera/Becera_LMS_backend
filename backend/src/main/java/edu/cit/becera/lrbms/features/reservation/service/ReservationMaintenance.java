package edu.cit.becera.lrbms.features.reservation.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically expires approved bookings whose pickup deadline has passed, so held copies free up
 * on their own without waiting for someone to open the bookings page.
 */
@Component
public class ReservationMaintenance {

    private static final long FIVE_MINUTES_MS = 5 * 60 * 1000L;

    private final ReservationService reservationService;

    public ReservationMaintenance(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(fixedRate = FIVE_MINUTES_MS)
    public void expireOverduePickups() {
        reservationService.expireOverduePickups();
    }
}
