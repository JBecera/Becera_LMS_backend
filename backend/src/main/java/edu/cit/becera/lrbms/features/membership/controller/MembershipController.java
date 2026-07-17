package edu.cit.becera.lrbms.features.membership.controller;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.features.membership.dto.CreateMemberRequest;
import edu.cit.becera.lrbms.features.membership.service.MembershipService;
import edu.cit.becera.lrbms.security.CurrentUser;
import edu.cit.becera.lrbms.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MembershipController {
    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping
    public List<Member> getAllMembers() {
        return membershipService.getAllMembers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMember(@PathVariable Long id) {
        CurrentUser currentUser = SecurityUtils.currentUser();
        if (currentUser == null || (!currentUser.owns(id) && !currentUser.isStaff())) {
            throw new AccessDeniedException("Not allowed to view this account");
        }
        return membershipService.getMember(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/role/{role}")
    public List<Member> getMembersByRole(@PathVariable String role) {
        return membershipService.getMembersByRole(role);
    }

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody CreateMemberRequest request) {
        try {
            CurrentUser currentUser = SecurityUtils.currentUser();
            String creatorRole = currentUser != null ? currentUser.role() : null;
            Member saved = membershipService.createMember(request, creatorRole);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMember(@PathVariable Long id, @RequestBody CreateMemberRequest request) {
        try {
            CurrentUser currentUser = SecurityUtils.currentUser();
            return ResponseEntity.ok(membershipService.updateMember(id, request, currentUser));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        membershipService.deleteMember(id);
        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }
}
