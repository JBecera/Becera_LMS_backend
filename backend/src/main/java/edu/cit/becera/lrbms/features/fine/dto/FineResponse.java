package edu.cit.becera.lrbms.features.fine.dto;

import edu.cit.becera.lrbms.entities.Fine;

import java.time.LocalDate;

public class FineResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Double amount;
    private String reason;
    private String paymentStatus;
    private LocalDate dateIssued;

    public static FineResponse from(Fine fine) {
        FineResponse response = new FineResponse();
        response.id = fine.getId();
        response.memberId = fine.getMember().getId();
        response.memberName = fine.getMember().getFirstName() + " " + fine.getMember().getLastName();
        response.amount = fine.getAmount();
        response.reason = fine.getReason();
        response.paymentStatus = fine.getPaymentStatus();
        response.dateIssued = fine.getDateIssued();
        return response;
    }

    public Long getId() { return id; }
    public Long getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public Double getAmount() { return amount; }
    public String getReason() { return reason; }
    public String getPaymentStatus() { return paymentStatus; }
    public LocalDate getDateIssued() { return dateIssued; }
}
