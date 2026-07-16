package edu.cit.becera.lrbms.features.fine.controller;

import edu.cit.becera.lrbms.features.fine.dto.FineResponse;
import edu.cit.becera.lrbms.features.fine.dto.UpdateFineRequest;
import edu.cit.becera.lrbms.features.fine.service.FineService;
import edu.cit.becera.lrbms.security.CurrentUser;
import edu.cit.becera.lrbms.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fines")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class FineController {
    private final FineService fineService;

    public FineController(FineService fineService) {
        this.fineService = fineService;
    }

    @GetMapping
    public List<FineResponse> getAllFines() {
        return fineService.getAllFines();
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<?> getFinesForMember(@PathVariable Long memberId) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        if (currentUser == null || (!currentUser.owns(memberId) && !currentUser.isStaff())) {
            throw new AccessDeniedException("Not allowed to view these fines");
        }
        return ResponseEntity.ok(fineService.getFinesForMember(memberId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> settleFine(@PathVariable Long id, @RequestBody UpdateFineRequest request) {
        try {
            return ResponseEntity.ok(fineService.settle(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }
}
