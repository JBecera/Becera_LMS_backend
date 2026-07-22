package edu.cit.becera.lrbms.features.reservation.dto;

public class UpdateReservationRequest {
    private String status;
    private String reason;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
