package edu.cit.becera.lrbms.features.reservation.dto;

import edu.cit.becera.lrbms.entities.Reservation;

import java.time.LocalDate;

public class ReservationResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long resourceId;
    private String resourceTitle;
    private LocalDate reservationDate;
    private String status;

    public static ReservationResponse from(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.id = reservation.getId();
        response.memberId = reservation.getMember().getId();
        response.memberName = reservation.getMember().getFirstName() + " " + reservation.getMember().getLastName();
        response.resourceId = reservation.getBook().getId();
        response.resourceTitle = reservation.getBook().getTitle();
        response.reservationDate = reservation.getReservationDate();
        response.status = reservation.getStatus();
        return response;
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public Long getResourceId() { return resourceId; }
    public String getResourceTitle() { return resourceTitle; }
    public LocalDate getReservationDate() { return reservationDate; }
    public String getStatus() { return status; }
}
