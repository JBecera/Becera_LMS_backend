package edu.cit.becera.lrbms.features.transaction.dto;

import edu.cit.becera.lrbms.entities.Transaction;

import java.time.LocalDate;

public class TransactionResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long resourceId;
    private String resourceTitle;
    private String resourceCategory;
    private LocalDate checkOutDate;
    private LocalDate dueDate;
    private LocalDate checkInDate;
    private String status;

    public static TransactionResponse from(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.id = transaction.getId();
        response.memberId = transaction.getMember().getId();
        response.memberName = transaction.getMember().getFirstName() + " " + transaction.getMember().getLastName();
        response.resourceId = transaction.getBook().getId();
        response.resourceTitle = transaction.getBook().getTitle();
        response.resourceCategory = transaction.getBook().getCategory();
        response.checkOutDate = transaction.getCheckOutDate();
        response.dueDate = transaction.getDueDate();
        response.checkInDate = transaction.getCheckInDate();
        response.status = transaction.getStatus();
        return response;
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public Long getResourceId() { return resourceId; }
    public String getResourceTitle() { return resourceTitle; }
    public String getResourceCategory() { return resourceCategory; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public String getStatus() { return status; }
}
