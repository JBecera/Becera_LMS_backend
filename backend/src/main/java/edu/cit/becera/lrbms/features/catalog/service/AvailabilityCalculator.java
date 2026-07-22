package edu.cit.becera.lrbms.features.catalog.service;

import edu.cit.becera.lrbms.entities.Book;
import edu.cit.becera.lrbms.entities.Reservation;
import edu.cit.becera.lrbms.entities.Transaction;

import java.time.LocalDate;
import java.util.List;

/**
 * Pure, side-effect-free availability math shared by the catalog's date filter and the booking
 * create path. Kept static so callers don't need to wire it as a bean (which would otherwise force
 * a constructor change on ReservationService and break its unit test).
 *
 * A copy is committed on date D by an active loan whose [checkOutDate, dueDate] covers D, or by a
 * pending/approved booking whose hold window [pickupDate, pickupDate + holdDays] covers D.
 */
public final class AvailabilityCalculator {

    private AvailabilityCalculator() {
    }

    public static int capacityOf(Book book) {
        if (book.getTotalCopies() != null) {
            return book.getTotalCopies();
        }
        return book.getAvailableCopies() != null ? book.getAvailableCopies() : 0;
    }

    public static int availableOnDate(Book book, LocalDate date,
                                       List<Transaction> activeLoans, List<Reservation> holds, int holdDays) {
        int committed = 0;

        for (Transaction loan : activeLoans) {
            if (covers(loan.getCheckOutDate(), loan.getDueDate(), date)) {
                committed++;
            }
        }
        for (Reservation hold : holds) {
            LocalDate pickup = hold.getPickupDate();
            if (pickup != null && covers(pickup, pickup.plusDays(holdDays), date)) {
                committed++;
            }
        }

        return Math.max(0, capacityOf(book) - committed);
    }

    private static boolean covers(LocalDate start, LocalDate end, LocalDate date) {
        if (start == null || end == null) {
            return false;
        }
        return !date.isBefore(start) && !date.isAfter(end);
    }
}
