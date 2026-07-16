package edu.cit.becera.lrbms.features.transaction.dto;

import java.time.LocalDate;

public class CheckoutRequest {
    private Long memberId;
    private Long resourceId;
    private LocalDate dueDate;

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
