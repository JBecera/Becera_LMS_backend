package edu.cit.becera.lrbms.features.booking.dto;

public class CreateBookingRequest {
    private Long resourceId;
    private Long memberId;
    private String status;

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
