package edu.cit.becera.lrbms.controllers;

import edu.cit.becera.lrbms.entities.Member;
import edu.cit.becera.lrbms.services.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class MemberController {

    private final MemberService service;

    public MemberController(MemberService service) {
        this.service = service;
    }

    @GetMapping
    public List<Member> getAllMembers() {
        return service.getAllMembers();
    }

    @GetMapping("/{id}")
    public Optional<Member> getMember(@PathVariable Long id) {
        return service.getMember(id);
    }

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody Member member) {
        try {
            Member savedMember = service.saveMember(member);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedMember);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public Member updateMember(@PathVariable Long id, @RequestBody Member member) {
        return service.updateMember(id, member);
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable Long id) {
        service.deleteMember(id);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Member member) {
        Member authenticated = service.authenticate(member.getEmail(), member.getPassword());
        if (authenticated == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        return ResponseEntity.ok(Map.of(
                "id", authenticated.getId(),
                "firstName", authenticated.getFirstName(),
                "lastName", authenticated.getLastName(),
                "email", authenticated.getEmail(),
                "role", authenticated.getRole()
        ));
    }
}