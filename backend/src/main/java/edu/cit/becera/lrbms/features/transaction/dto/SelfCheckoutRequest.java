package edu.cit.becera.lrbms.features.transaction.dto;

import java.time.LocalDate;

public class SelfCheckoutRequest {
    private Long resourceId;
    private LocalDate dueDate;

    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
