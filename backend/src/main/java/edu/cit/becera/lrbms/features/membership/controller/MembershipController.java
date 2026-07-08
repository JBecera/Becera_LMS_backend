package edu.cit.becera.lrbms.features.membership.controller;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.features.membership.dto.CreateMemberRequest;
import edu.cit.becera.lrbms.features.membership.service.MembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
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
    public Optional<Member> getMember(@PathVariable Long id) {
        return membershipService.getMember(id);
    }

    @GetMapping("/role/{role}")
    public List<Member> getMembersByRole(@PathVariable String role) {
        return membershipService.getMembersByRole(role);
    }

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody CreateMemberRequest request) {
        try {
            Member saved = membershipService.createMember(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMember(@PathVariable Long id, @RequestBody CreateMemberRequest request) {
        try {
            return ResponseEntity.ok(membershipService.updateMember(id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        membershipService.deleteMember(id);
        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }
}
