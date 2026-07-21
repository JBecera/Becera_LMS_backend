package edu.cit.becera.lrbms.features.transaction.controller;

import edu.cit.becera.lrbms.features.transaction.dto.CheckoutRequest;
import edu.cit.becera.lrbms.features.transaction.dto.SelfCheckoutRequest;
import edu.cit.becera.lrbms.features.transaction.dto.TransactionResponse;
import edu.cit.becera.lrbms.features.transaction.service.TransactionService;
import edu.cit.becera.lrbms.security.CurrentUser;
import edu.cit.becera.lrbms.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionResponse> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getTransactionsForMember(@PathVariable Long memberId) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        if (currentUser == null || (!currentUser.owns(memberId) && !currentUser.isStaff())) {
            throw new AccessDeniedException("Not allowed to view these transactions");
        }
        return ResponseEntity.ok(transactionService.getTransactionsForMember(memberId));
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.checkout(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/self-checkout")
    public ResponseEntity<?> selfCheckout(@RequestBody SelfCheckoutRequest request) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Login required"));
        }
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.selfCheckout(currentUser, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/checkin")
    public ResponseEntity<?> checkIn(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(transactionService.checkIn(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }
}
