package edu.cit.becera.lrbms.features.reservation.dto;

import java.time.LocalDate;

public class CreateReservationRequest {
    private Long resourceId;
    private LocalDate pickupDate;

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }

    public LocalDate getPickupDate() { return pickupDate; }
    public void setPickupDate(LocalDate pickupDate) { this.pickupDate = pickupDate; }
}
